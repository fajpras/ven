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

    /**
     * Mengambil potret data untuk bukti sebelum/sesudah request.
     *
     * @return array<string, mixed>
     */
    protected function snapshotData(int $idLogSejak = 0): array
    {
        return [
            'pameran' => Pameran::query()
                ->where('judul', $this->judul)
                ->get(['id_pameran', 'judul', 'banner', 'tipe', 'id_model'])
                ->toArray(),
            'file_banner' => $this->fileBanner(),
            'log_pameran' => DB::table('log_aktivitas')
                ->where('id_log', '>', $idLogSejak)
                ->where('entitas', 'pameran')
                ->get(['id_log', 'pesan', 'status'])
                ->all(),
            'level_transaksi' => DB::transactionLevel(),
            'event' => $this->peristiwaTransaksi,
        ];
    }

    /**
     * Mencetak bukti ke terminal dan menyimpannya sebagai file Markdown
     * agar mudah dilampirkan pada laporan.
     *
     * @param  array<string, mixed>  $sebelum
     * @param  array<string, mixed>  $sesudah
     */
    protected function cetakBukti(string $skenario, array $sebelum, array $sesudah, int $status): void
    {
        $teks = implode(PHP_EOL, [
            str_repeat('=', 70),
            "BUKTI TRANSAKSI — {$skenario}",
            str_repeat('=', 70),
            $this->formatSnapshot('SEBELUM REQUEST', $sebelum),
            '',
            $this->formatSnapshot('SESUDAH REQUEST', $sesudah, $status),
            str_repeat('=', 70),
        ]).PHP_EOL;

        fwrite(STDERR, PHP_EOL.$teks);

        $folder = storage_path('app/testing');

        if (! is_dir($folder)) {
            mkdir($folder, 0777, true);
        }

        $nama = str_contains($skenario, 'SUKSES')
            ? 'bukti-transaksi-sukses.md'
            : 'bukti-transaksi-gagal.md';

        file_put_contents($folder.DIRECTORY_SEPARATOR.$nama, $teks);
    }

    /**
     * @param  array<string, mixed>  $data
     */
    private function formatSnapshot(string $judul, array $data, ?int $status = null): string
    {
        $baris = ["{$judul}:"];

        if ($status !== null) {
            $baris[] = "  response HTTP            : {$status}";
        }

        $baris[] = '  jumlah pameran           : '.count($data['pameran']);

        foreach ($data['pameran'] as $pameran) {
            $baris[] = "    - id_pameran={$pameran['id_pameran']}, judul={$pameran['judul']}, banner={$pameran['banner']}";
        }

        $baris[] = '  file banner              : '.count($data['file_banner']);

        foreach ($data['file_banner'] as $file) {
            $baris[] = "    - {$file}";
        }

        $baris[] = '  log aktivitas pameran    : '.count($data['log_pameran']);

        foreach ($data['log_pameran'] as $log) {
            $baris[] = "    - {$log->pesan} ({$log->status})";
        }

        $baris[] = '  level transaksi          : '.$data['level_transaksi'];
        $baris[] = '  event transaksi          : '.(empty($data['event']) ? '(belum ada)' : implode(' -> ', $data['event']));

        return implode(PHP_EOL, $baris);
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
