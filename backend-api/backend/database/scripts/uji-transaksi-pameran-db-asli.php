<?php

/**
 * Uji transaksi TAMBAH PAMERAN langsung di database asli (.env).
 *
 * Cara pakai:
 *   php artisan tinker database/scripts/uji-transaksi-pameran-db-asli.php
 *
 * Script memanggil PameranController@store() yang asli, sehingga
 * DB::transaction(), penyimpanan banner, dan rollback-nya benar-benar
 * dijalankan seperti aplikasi.
 */

use App\Http\Controllers\Pengguna\PameranController;
use App\Models\Model3D;
use App\Models\Pameran;
use App\Models\Pengguna;
use Illuminate\Database\Events\TransactionBeginning;
use Illuminate\Database\Events\TransactionCommitted;
use Illuminate\Database\Events\TransactionRolledBack;
use Illuminate\Http\Request;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Event;
use Illuminate\Support\Facades\Storage;

$koneksiAsli = DB::getDefaultConnection();
$koneksiUji = 'mysql_user'; // role yang punya izin INSERT ke tabel pameran

$user = Pengguna::query()->where('role', 'pengguna')->first()
    ?? Pengguna::query()->first();
$model = Model3D::query()->where('jenis', 'hall')->first();

if (! $user) {
    echo PHP_EOL.'DB asli belum punya satu pun pengguna.'.PHP_EOL;
    exit(1);
}

$judul = 'Pameran Uji Tinker '.date('YmdHis');
$peristiwa = [];

Event::listen(TransactionBeginning::class, function () use (&$peristiwa): void {
    $peristiwa[] = 'BEGIN';
});
Event::listen(TransactionCommitted::class, function () use (&$peristiwa): void {
    $peristiwa[] = 'COMMIT';
});
Event::listen(TransactionRolledBack::class, function () use (&$peristiwa): void {
    $peristiwa[] = 'ROLLBACK';
});

DB::setDefaultConnection($koneksiUji);
$user->setConnection($koneksiUji);

$modelDibuat = false;
if (! $model) {
    $model = new Model3D([
        'jenis' => 'hall',
        'file' => 'uji-tinker.glb',
        'kapasitas' => '10',
        'nama' => 'Hall Uji '.date('His'),
        'gambar' => 'uji-tinker.jpg',
    ]);
    $model->setConnection($koneksiUji);
    $model->save();
    $modelDibuat = true;
}

$controller = app(PameranController::class);

$buatRequest = function () use ($judul, $model, $user): Request {
    $request = Request::create('/api/pameran', 'POST', [
        'judul' => $judul,
        'tipe' => 'kecil',
        'id_model' => $model->id_model,
    ]);

    $request->setUserResolver(fn () => $user);
    $request->files->set('banner', UploadedFile::fake()->image('banner-tinker.jpg', 640, 480));

    return $request;
};

$jumlahPameran = fn (): int => Pameran::on($koneksiUji)
    ->where('judul', $judul)
    ->count();

$hapusDataUji = function () use ($judul, $koneksiAsli): void {
    $daftar = Pameran::on($koneksiAsli)
        ->withTrashed()
        ->where('judul', $judul)
        ->get();

    foreach ($daftar as $pameran) {
        $path = str_replace('/storage/', '', (string) parse_url($pameran->banner, PHP_URL_PATH));

        if ($path !== '') {
            Storage::disk('public')->delete($path);
        }

        $pameran->forceDelete();

        DB::connection($koneksiAsli)
            ->table('log_aktivitas')
            ->where('pesan', 'like', "%pameran #{$pameran->id_pameran}%")
            ->delete();
    }
};

echo PHP_EOL;
echo 'Database : '.DB::connection($koneksiUji)->getDatabaseName().PHP_EOL;
echo 'Koneksi  : '.$koneksiUji.PHP_EOL;
echo 'Judul    : '.$judul.PHP_EOL;
echo 'User     : '.$user->email.PHP_EOL;
echo 'Model    : '.$model->nama.($modelDibuat ? ' (dibuat sementara)' : '').PHP_EOL;

try {
    echo PHP_EOL.'=== 1. TAMBAH PAMERAN — COMMIT ==='.PHP_EOL;
    echo 'Sebelum  : '.$jumlahPameran().' baris pameran'.PHP_EOL;

    $respons = $controller->store($buatRequest());

    echo 'Response : '.$respons->getStatusCode().PHP_EOL;
    echo 'Sesudah  : '.$jumlahPameran().' baris pameran'.PHP_EOL;

    $pameran = Pameran::on($koneksiUji)->where('judul', $judul)->firstOrFail();
    $pathBanner = str_replace('/storage/', '', (string) parse_url($pameran->banner, PHP_URL_PATH));

    echo 'Data     : #'.$pameran->id_pameran.' '.$pameran->judul.PHP_EOL;
    echo 'Banner   : '.$pameran->banner.PHP_EOL;
    echo 'File ada : '.(Storage::disk('public')->exists($pathBanner) ? 'ya' : 'tidak').PHP_EOL;
    echo 'Log      : '.DB::connection($koneksiUji)->table('log_aktivitas')
        ->where('pesan', 'like', "%pameran #{$pameran->id_pameran}%")
        ->count().' baris'.PHP_EOL;
    echo 'Event    : '.implode(' -> ', $peristiwa).PHP_EOL;

    // Bersihkan hasil skenario COMMIT sebelum menguji ROLLBACK.
    $hapusDataUji();
    $peristiwa = [];

    echo PHP_EOL.'=== 2. TAMBAH PAMERAN — ROLLBACK ==='.PHP_EOL;
    echo 'Sebelum  : '.$jumlahPameran().' baris pameran'.PHP_EOL;

    $gagal = false;

    Event::listen('eloquent.created: '.Pameran::class, function () use (&$gagal): void {
        if ($gagal) {
            throw new RuntimeException('Gagal sengaja setelah INSERT pameran.');
        }
    });

    $gagal = true;

    try {
        $controller->store($buatRequest());
        echo 'Response : tidak terduga (seharusnya gagal)'.PHP_EOL;
    } catch (Throwable $e) {
        echo 'Error    : '.$e->getMessage().PHP_EOL;
    } finally {
        $gagal = false;
    }

    echo 'Sesudah  : '.$jumlahPameran().' baris pameran'.PHP_EOL;
    echo 'Event    : '.implode(' -> ', $peristiwa).PHP_EOL;
} finally {
    try {
        $hapusDataUji();
    } catch (Throwable $e) {
        echo PHP_EOL.'Bersih pameran gagal: '.$e->getMessage().PHP_EOL;
    }

    try {
        if ($modelDibuat) {
            DB::connection($koneksiAsli)
                ->table('model')
                ->where('id_model', $model->id_model)
                ->delete();
        }
    } catch (Throwable $e) {
        echo 'Bersih model gagal: '.$e->getMessage().PHP_EOL;
    }

    DB::setDefaultConnection($koneksiAsli);

    echo PHP_EOL.'Bersih   : data uji dihapus.'.PHP_EOL;
    echo 'Selesai.'.PHP_EOL.PHP_EOL;
}

exit;
