<?php

/**
 * Uji transaksi langsung di database asli (.env).
 *
 * Cara pakai:
 *   php artisan tinker database/scripts/uji-transaksi-db-asli.php
 *
 * Atau dari dalam tinker:
 *   require 'database/scripts/uji-transaksi-db-asli.php';
 *
 * Script ini hanya memakai tabel log_aktivitas, memberi penanda unik,
 * dan menghapus datanya sendiri di akhir.
 */

use Illuminate\Support\Facades\DB;

$marker = 'UJI-TINKER-'.date('YmdHis').'-'.substr(md5((string) mt_rand()), 0, 6);

$jumlah = static fn (string $awalan): int => DB::table('log_aktivitas')
    ->where('pesan', 'like', $awalan.'%')
    ->count();

echo PHP_EOL;
echo 'Database : '.DB::connection()->getDatabaseName().PHP_EOL;
echo 'Marker   : '.$marker.PHP_EOL;

try {
    echo PHP_EOL.'=== 1. COMMIT ==='.PHP_EOL;
    echo 'Sebelum  : '.$jumlah($marker).' baris'.PHP_EOL;

    DB::transaction(function () use ($marker): void {
        DB::table('log_aktivitas')->insert([
            ['entitas' => 'uji_transaksi', 'status' => 'berhasil', 'pesan' => $marker.' commit-1'],
            ['entitas' => 'uji_transaksi', 'status' => 'berhasil', 'pesan' => $marker.' commit-2'],
        ]);
    });

    echo 'Sesudah  : '.$jumlah($marker).' baris'.PHP_EOL;

    foreach (DB::table('log_aktivitas')->where('pesan', 'like', $marker.'%')->get(['id_log', 'pesan']) as $baris) {
        echo "  #{$baris->id_log} {$baris->pesan}".PHP_EOL;
    }

    $markerRollback = $marker.'-rollback';

    echo PHP_EOL.'=== 2. ROLLBACK ==='.PHP_EOL;
    echo 'Sebelum  : '.$jumlah($markerRollback).' baris'.PHP_EOL;

    try {
        DB::transaction(function () use ($markerRollback): void {
            DB::table('log_aktivitas')->insert([
                'entitas' => 'uji_transaksi',
                'status' => 'berhasil',
                'pesan' => $markerRollback.' baris-1',
            ]);

            throw new RuntimeException('Gagal sengaja setelah INSERT pertama.');
        });
    } catch (Throwable $e) {
        echo 'Error    : '.$e->getMessage().PHP_EOL;
    }

    echo 'Sesudah  : '.$jumlah($markerRollback).' baris'.PHP_EOL;
} finally {
    $dihapus = DB::table('log_aktivitas')->where('pesan', 'like', $marker.'%')->delete();

    echo PHP_EOL.'Bersih   : '.$dihapus.' baris dihapus.'.PHP_EOL;
    echo 'Selesai.'.PHP_EOL.PHP_EOL;
}

exit;
