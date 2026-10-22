<?php

use App\Models\Pameran;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Storage;

$daftar = Pameran::on('mysql')
    ->withTrashed()
    ->where('judul', 'like', 'Pameran Uji Tinker %')
    ->get();

foreach ($daftar as $pameran) {
    $path = str_replace('/storage/', '', (string) parse_url($pameran->banner, PHP_URL_PATH));

    if ($path !== '') {
        Storage::disk('public')->delete($path);
    }

    $pameran->forceDelete();

    DB::connection('mysql')
        ->table('log_aktivitas')
        ->where('pesan', 'like', "%pameran #{$pameran->id_pameran}%")
        ->delete();

    echo 'Hapus pameran #'.$pameran->id_pameran.' '.$pameran->judul.PHP_EOL;
}

$model = DB::connection('mysql')
    ->table('model')
    ->where('file', 'uji-tinker.glb')
    ->where('nama', 'like', 'Hall Uji %')
    ->get();

foreach ($model as $baris) {
    DB::connection('mysql')
        ->table('model')
        ->where('id_model', $baris->id_model)
        ->delete();

    echo 'Hapus model #'.$baris->id_model.' '.$baris->nama.PHP_EOL;
}

echo 'Selesai.'.PHP_EOL;

exit;
