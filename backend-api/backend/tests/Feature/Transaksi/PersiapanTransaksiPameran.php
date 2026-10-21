<?php

namespace Tests\Feature\Transaksi;

use App\Models\Model3D;
use App\Models\Pameran;
use App\Models\Pengguna;
use Illuminate\Database\Events\TransactionBeginning;
use Illuminate\Database\Events\TransactionCommitted;
use Illuminate\Database\Events\TransactionRolledBack;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Event;
use Illuminate\Support\Facades\Storage;
use Illuminate\Support\Str;

/**
 * Persiapan bersama untuk modul pengujian transaksi "tambah pameran".
 *
 * Data dibuat unik per test supaya aman dijalankan berulang di ven_testing
 * tanpa memakai RefreshDatabase. Dengan begitu COMMIT yang diuji adalah
 * COMMIT sungguhan, bukan savepoint dari transaksi pembungkus PHPUnit.
 */
trait PersiapanTransaksiPameran
{
    protected ?Pengguna $pengguna = null;

    protected ?Model3D $model = null;

    protected string $judul = '';

    /** @var array<int, string> */
    protected array $peristiwaTransaksi = [];

    /** Level transaksi saat baris pameran di-INSERT. */
    protected ?int $levelSaatInsert = null;

    protected function siapkanData(): void
    {
        Storage::fake('public');

        $this->pengguna = Pengguna::factory()->create();

        $this->model = Model3D::create([
            'jenis' => 'hall',
            'file' => 'model/hall-uji-transaksi.glb',
            'kapasitas' => '10',
            'nama' => 'Hall Uji Transaksi',
            'gambar' => 'model/hall-uji-transaksi.png',
        ]);

        $this->judul = 'Pameran Uji Transaksi '.Str::uuid();

        $this->catatPeristiwaTransaksi();
    }

    /**
     * Merekam event bawaan Laravel untuk membuktikan kapan transaksi dimulai,
     * di-commit, atau di-rollback.
     */
    protected function catatPeristiwaTransaksi(): void
    {
        Event::listen(TransactionBeginning::class, function (): void {
            $this->peristiwaTransaksi[] = 'BEGIN';
        });

        Event::listen(TransactionCommitted::class, function (): void {
            $this->peristiwaTransaksi[] = 'COMMIT';
        });

        Event::listen(TransactionRolledBack::class, function (): void {
            $this->peristiwaTransaksi[] = 'ROLLBACK';
        });

        // Membuktikan INSERT benar-benar berjalan di dalam transaksi.
        Event::listen('eloquent.creating: '.Pameran::class, function (): void {
            $this->levelSaatInsert = DB::transactionLevel();
        });
    }

    /**
     * Payload yang sama untuk skenario sukses maupun gagal.
     */
    protected function dataPameran(): array
    {
        return [
            'judul' => $this->judul,
            'tipe' => 'kecil',
            'id_model' => $this->model->id_model,
            'banner' => UploadedFile::fake()->image('banner-transaksi.jpg', 640, 480),
        ];
    }

    protected function kirimTambahPameran()
    {
        return $this->post('/api/pameran', $this->dataPameran(), [
            'Accept' => 'application/json',
        ]);
    }

    protected function jumlahPameran(): int
    {
        return Pameran::query()->where('judul', $this->judul)->count();
    }

    /**
     * @return array<int, string>
     */
    protected function fileBanner(): array
    {
        return Storage::disk('public')->allFiles('pameran/banner');
    }

    protected function bersihkanData(): void
    {
        if ($this->judul !== '') {
            $idPameran = Pameran::withTrashed()
                ->where('judul', $this->judul)
                ->pluck('id_pameran')
                ->all();

            foreach ($idPameran as $id) {
                DB::table('log_aktivitas')
                    ->where('pesan', 'like', "%pameran #{$id}%")
                    ->delete();
            }

            Pameran::withTrashed()
                ->where('judul', $this->judul)
                ->forceDelete();
        }

        if ($this->pengguna) {
            DB::table('log_aktivitas')
                ->where('id_pengguna', $this->pengguna->id_pengguna)
                ->delete();
        }

        $this->model?->delete();
        $this->pengguna?->forceDelete();
    }
}
