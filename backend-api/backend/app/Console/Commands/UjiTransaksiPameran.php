<?php

namespace App\Console\Commands;

use App\Http\Controllers\Pengguna\PameranController;
use App\Models\Model3D;
use App\Models\Pameran;
use App\Models\Pengguna;
use Illuminate\Console\Command;
use Illuminate\Database\Events\TransactionBeginning;
use Illuminate\Database\Events\TransactionCommitted;
use Illuminate\Database\Events\TransactionRolledBack;
use Illuminate\Http\Request;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Event;
use Illuminate\Support\Facades\Storage;
use RuntimeException;
use Throwable;

class UjiTransaksiPameran extends Command
{
    protected $signature = 'uji:transaksi-pameran
        {--koneksi=mysql_user : Koneksi database yang dipakai untuk INSERT}
        {--skenario=both : both, commit, atau rollback}
        {--keep : Simpan data dummy dari skenario commit}
        {--bersihkan : Hapus semua data dummy lalu keluar}';

    protected $description = 'Uji COMMIT dan ROLLBACK tambah pameran dengan data dummy';

    private const PREFIX_JUDUL = 'DUMMY-TRANSAKSI-';

    private const FILE_MODEL = 'dummy-uji.glb';

    private string $koneksiAsli;

    private string $koneksiUji;

    private ?Pengguna $pengguna = null;

    private ?Model3D $model = null;

    private bool $modelDibuat = false;

    private string $judul = '';

    /** Level transaksi saat baris pameran di-INSERT. */
    private ?int $levelSaatInsert = null;

    /** @var array<int, string> */
    private array $peristiwa = [];

    public function handle(): int
    {
        $this->koneksiAsli = config('database.default') === 'mysql_dba' ? 'mysql_dba' : 'mysql_dba';
        $this->koneksiUji = (string) $this->option('koneksi');

        try {
            DB::connection($this->koneksiUji)->getPdo();
        } catch (Throwable $e) {
            $this->error("Koneksi '{$this->koneksiUji}' tidak bisa dipakai: {$e->getMessage()}");

            return self::FAILURE;
        }

        if ($this->option('bersihkan')) {
            return $this->bersihkanSemua();
        }

        $skenario = (string) $this->option('skenario');

        if (! in_array($skenario, ['both', 'commit', 'rollback'], true)) {
            $this->error('--skenario harus salah satu dari: both, commit, rollback.');

            return self::FAILURE;
        }

        if ($this->option('keep') && $skenario !== 'commit') {
            $this->error('--keep hanya bisa dipakai dengan --skenario=commit.');

            return self::FAILURE;
        }

        $this->pengguna = Pengguna::query()->where('role', 'pengguna')->first()
            ?? Pengguna::query()->first();
        $this->model = Model3D::query()->where('jenis', 'hall')->first();

        if (! $this->pengguna) {
            $this->error('Database belum punya satu pun pengguna.');

            return self::FAILURE;
        }

        $kode = date('YmdHis').'-'.random_int(1000, 9999);
        $this->judul = self::PREFIX_JUDUL.$kode;

        DB::setDefaultConnection($this->koneksiUji);
        $this->pengguna->setConnection($this->koneksiUji);

        if (! $this->model) {
            $this->model = new Model3D([
                'jenis' => 'hall',
                'file' => self::FILE_MODEL,
                'kapasitas' => '10',
                'nama' => 'Hall Dummy '.$kode,
                'gambar' => 'dummy-uji.jpg',
            ]);
            $this->model->setConnection($this->koneksiUji);
            $this->model->save();
            $this->modelDibuat = true;
        }

        $this->daftarkanEventTransaksi();

        $this->line(str_repeat('=', 72));
        $this->info('UJI TRANSAKSI TAMBAH PAMERAN');
        $this->line(str_repeat('=', 72));
        $this->line('Database    : '.DB::connection($this->koneksiUji)->getDatabaseName());
        $this->line('Koneksi     : '.$this->koneksiUji);
        $this->line('Judul dummy : '.$this->judul);
        $this->line('Pengguna    : '.$this->pengguna->email);
        $this->line('Model hall  : '.$this->model->nama.($this->modelDibuat ? ' (dibuat sementara)' : ''));
        $this->newLine();

        $fileAwal = Storage::disk('public')->allFiles('pameran/banner');
        $logAwal = $this->idLogTerakhir();

        // $this->info('KONDISI AWAL');
        // $this->cetakSnapshot($this->snapshot($fileAwal, $logAwal));
        // $this->newLine();

        try {
            if (in_array($skenario, ['both', 'commit'], true)) {
                $this->jalankanCommit($fileAwal, $logAwal);

                if ($skenario === 'both') {
                    $this->hapusPameran($this->judul);
                    $this->line('Data skenario commit dibersihkan sebelum skenario rollback.');
                    $this->newLine();
                }
            }

            if (in_array($skenario, ['both', 'rollback'], true)) {
                $fileRollback = Storage::disk('public')->allFiles('pameran/banner');
                $logRollback = $this->idLogTerakhir();
                $this->jalankanRollback($fileRollback, $logRollback);
            }
        } finally {
            if ($this->option('keep')) {
                $pameran = Pameran::on($this->koneksiUji)
                    ->where('judul', $this->judul)
                    ->first();

                $this->newLine();
                $this->info('DATA DUMMY DISIMPAN');
                $this->line('  Judul : '.$this->judul);
                $this->line('  ID    : '.($pameran ? '#'.$pameran->id_pameran : '(tidak ada)'));

                if ($this->modelDibuat) {
                    $this->line('  Model : #'.$this->model->id_model.' '.$this->model->nama);
                }

                $this->line('  Hapus : php artisan uji:transaksi-pameran --bersihkan');
            } else {
                $dihapus = $this->hapusPameran($this->judul);

                if ($this->modelDibuat && $this->model) {
                    DB::connection($this->koneksiAsli)
                        ->table('model')
                        ->where('id_model', $this->model->id_model)
                        ->delete();
                }

                $this->newLine();

                if ($dihapus > 0) {
                    $this->line('Data dummy dibersihkan: '.$dihapus.' pameran.');
                } else {
                    $this->line('Tidak ada data dummy tersisa.');
                }
            }

            DB::setDefaultConnection($this->koneksiAsli);
        }

        return self::SUCCESS;
    }

    private function jalankanCommit(array $fileAwal, int $logAwal): void
    {
        $this->peristiwa = [];
        $this->levelSaatInsert = null;

        $this->info('PROSES COMMIT');
        $this->line('  Sebelum:');
        $sebelum = $this->snapshot($fileAwal, $logAwal);
        $this->cetakSnapshot($sebelum, 4);

        $respons = app(PameranController::class)->store($this->buatRequest());

        $this->line('  Sesudah:');
        $sesudah = $this->snapshot($fileAwal, $logAwal);
        $this->cetakSnapshot($sesudah, 4, $respons->getStatusCode());
        $this->line('  Level saat INSERT: '.($this->levelSaatInsert ?? 'tidak terekam'));

        $pameran = Pameran::on($this->koneksiUji)
            ->where('judul', $this->judul)
            ->first();

        if ($pameran) {
            $this->line('  Data  : #'.$pameran->id_pameran.' '.$pameran->judul);
            $this->line('  Banner: '.$pameran->banner);
        }

        $this->cetakPerubahan($sebelum, $sesudah);
        $this->cetakEvent();
        $this->newLine();
    }

    private function jalankanRollback(array $fileAwal, int $logAwal): void
    {
        $this->peristiwa = [];
        $this->levelSaatInsert = null;

        $this->info('PROSES ROLLBACK');
        $this->line('  Sebelum:');
        $sebelum = $this->snapshot($fileAwal, $logAwal);
        $this->cetakSnapshot($sebelum, 4);
        $gagal = false;

        Event::listen('eloquent.created: '.Pameran::class, function () use (&$gagal): void {
            if ($gagal) {
                throw new RuntimeException('Gagal sengaja setelah INSERT pameran.');
            }
        });

        $gagal = true;

        try {
            app(PameranController::class)->store($this->buatRequest());
            $this->warn('  Tidak terduga: proses tidak gagal.');
        } catch (Throwable $e) {
            $this->line('  Error : '.$e->getMessage());
        } finally {
            $gagal = false;
        }

        $this->line('  Sesudah:');
        $sesudah = $this->snapshot($fileAwal, $logAwal);
        $this->cetakSnapshot($sesudah, 4);
        $this->line('  Level saat INSERT: '.($this->levelSaatInsert ?? 'tidak terekam'));

        $this->cetakPerubahan($sebelum, $sesudah);
        $this->cetakEvent();
        $this->newLine();
    }

    private function buatRequest(): Request
    {
        $request = Request::create('/api/pameran', 'POST', [
            'judul' => $this->judul,
            'tipe' => 'kecil',
            'id_model' => $this->model->id_model,
        ]);

        $request->setUserResolver(fn () => $this->pengguna);
        $request->files->set('banner', UploadedFile::fake()->image('dummy.jpg', 640, 480));

        return $request;
    }

    /**
     * @param  array<int, string>  $fileAwal
     * @return array<string, mixed>
     */
    private function snapshot(array $fileAwal, int $logAwal): array
    {
        $pameran = Pameran::on($this->koneksiUji)
            ->where('judul', $this->judul)
            ->get(['id_pameran', 'judul', 'banner', 'id_model']);

        $fileSekarang = Storage::disk('public')->allFiles('pameran/banner');
        $fileBaru = array_values(array_diff($fileSekarang, $fileAwal));

        $logBaru = DB::connection($this->koneksiAsli)
            ->table('log_aktivitas')
            ->where('id_log', '>', $logAwal)
            ->where('entitas', 'pameran')
            ->get(['id_log', 'pesan', 'status']);

        return [
            'pameran' => $pameran,
            'file_total' => count($fileSekarang),
            'file_baru' => count($fileBaru),
            'log' => $logBaru,
            'log_baru' => $logBaru->count(),
            'level' => DB::transactionLevel(),
        ];
    }

    /**
     * @param  array<string, mixed>  $snapshot
     */
    private function cetakSnapshot(array $snapshot, int $indent = 2, ?int $status = null): void
    {
        $spasi = str_repeat(' ', $indent);

        if ($status !== null) {
            $this->line($spasi.'response HTTP : '.$status);
        }

        $this->line($spasi.'pameran dummy : '.count($snapshot['pameran']));

        foreach ($snapshot['pameran'] as $pameran) {
            $this->line($spasi.'  #'.$pameran->id_pameran.' '.$pameran->judul.' | '.$pameran->banner);
        }

        $this->line($spasi.'file banner   : total '.$snapshot['file_total'].', baru '.$snapshot['file_baru']);
        $this->line($spasi.'log pameran   : baru '.$snapshot['log_baru']);

        foreach ($snapshot['log'] as $log) {
            $this->line($spasi.'  #'.$log->id_log.' '.$log->pesan.' ('.$log->status.')');
        }

        $this->line($spasi.'level transaksi: '.$snapshot['level']);
    }

    /**
     * Menampilkan selisih data antara kondisi sebelum dan sesudah proses.
     *
     * @param  array<string, mixed>  $sebelum
     * @param  array<string, mixed>  $sesudah
     */
    private function cetakPerubahan(array $sebelum, array $sesudah): void
    {
        $jumlahSebelum = count($sebelum['pameran']);
        $jumlahSesudah = count($sesudah['pameran']);
        $selisih = $jumlahSesudah - $jumlahSebelum;

        $this->line('  Perubahan:');
        $this->line('    pameran : '.$jumlahSebelum.' -> '.$jumlahSesudah.' ('.($selisih > 0 ? '+' : '').$selisih.')');
        $this->line('    banner  : +'.$sesudah['file_baru'].' file baru');
        $this->line('    log     : +'.$sesudah['log_baru'].' baris baru');

        $idPameranSebelum = $sebelum['pameran']->pluck('id_pameran')->all();

        foreach ($sesudah['pameran']->whereNotIn('id_pameran', $idPameranSebelum) as $pameran) {
            $this->line('    + pameran #'.$pameran->id_pameran.' '.$pameran->judul);
        }

        $idLogSebelum = $sebelum['log']->pluck('id_log')->all();

        foreach ($sesudah['log']->whereNotIn('id_log', $idLogSebelum) as $log) {
            $this->line('    + log #'.$log->id_log.' '.$log->pesan.' ('.$log->status.')');
        }

        if ($selisih === 0 && $sesudah['file_baru'] === 0 && $sesudah['log_baru'] === 0) {
            $this->line('    (tidak ada perubahan)');
        }
    }

    private function cetakEvent(): void
    {
        $this->line('  Event : '.(empty($this->peristiwa) ? '(belum ada)' : implode(' -> ', $this->peristiwa)));
    }

    private function daftarkanEventTransaksi(): void
    {
        Event::listen(TransactionBeginning::class, function (): void {
            $this->peristiwa[] = 'BEGIN';
        });

        Event::listen(TransactionCommitted::class, function (): void {
            $this->peristiwa[] = 'COMMIT';
        });

        Event::listen(TransactionRolledBack::class, function (): void {
            $this->peristiwa[] = 'ROLLBACK';
        });

        Event::listen('eloquent.creating: '.Pameran::class, function (): void {
            $this->levelSaatInsert = DB::transactionLevel();
        });
    }

    private function idLogTerakhir(): int
    {
        return (int) DB::connection($this->koneksiAsli)
            ->table('log_aktivitas')
            ->max('id_log');
    }

    private function hapusPameran(string $judul): int
    {
        $daftar = Pameran::on($this->koneksiAsli)
            ->withTrashed()
            ->where('judul', $judul)
            ->get();

        foreach ($daftar as $pameran) {
            $path = str_replace('/storage/', '', (string) parse_url($pameran->banner, PHP_URL_PATH));

            if ($path !== '') {
                Storage::disk('public')->delete($path);
            }

            $pameran->forceDelete();

            DB::connection($this->koneksiAsli)
                ->table('log_aktivitas')
                ->where('pesan', 'like', "%pameran #{$pameran->id_pameran}%")
                ->delete();
        }

        return $daftar->count();
    }

    private function bersihkanSemua(): int
    {
        $daftar = Pameran::on($this->koneksiAsli)
            ->withTrashed()
            ->where('judul', 'like', self::PREFIX_JUDUL.'%')
            ->get();

        foreach ($daftar as $pameran) {
            $path = str_replace('/storage/', '', (string) parse_url($pameran->banner, PHP_URL_PATH));

            if ($path !== '') {
                Storage::disk('public')->delete($path);
            }

            $pameran->forceDelete();

            DB::connection($this->koneksiAsli)
                ->table('log_aktivitas')
                ->where('pesan', 'like', "%pameran #{$pameran->id_pameran}%")
                ->delete();
        }

        $model = DB::connection($this->koneksiAsli)
            ->table('model')
            ->where('file', self::FILE_MODEL)
            ->where('nama', 'like', 'Hall Dummy %')
            ->pluck('id_model');

        DB::connection($this->koneksiAsli)
            ->table('model')
            ->whereIn('id_model', $model)
            ->delete();

        $this->info('Data dummy dibersihkan.');
        $this->line('  Pameran : '.$daftar->count());
        $this->line('  Model   : '.$model->count());

        return self::SUCCESS;
    }
}
