<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Support\Facades\DB;

/**
 * Memberi hak akses ke user aplikasi MySQL (app_db).
 * User app_db harus dibuat dulu lewat skrip setup (CREATE USER).
 * Jika user tidak ada / tidak bisa dibaca, migrasi ini dilewati.
 *
 * MySQL tidak bisa REVOKE hak tingkat-tabel dari GRANT tingkat-database,
 * jadi hak diberikan per tabel: tabel log hanya SELECT + INSERT (append-only).
 * Tabel bawaan Laravel (migrations, dll.) sengaja tidak diberi hak.
 */
return new class extends Migration
{
    private const USER = 'app_db';

    private const TABEL = [
        'pengguna', 'reset_kata_sandi', 'sanksi_akun', 'kategori', 'pengguna_kategori',
        'model', 'posisi_objek', 'objek', 'pameran', 'ruangan', 'karya', 'undangan',
    ];

    private const TABEL_LOG = ['log_aktifitas', 'log_keamanan'];

    private function hosts(): array
    {
        try {
            return array_map(
                fn ($r) => $r->Host,
                DB::select('SELECT Host FROM mysql.user WHERE User = ?', [self::USER])
            );
        } catch (\Throwable) {
            return [];
        }
    }

    public function up(): void
    {
        $db = DB::getDatabaseName();

        foreach ($this->hosts() as $host) {
            $akun = "'" . self::USER . "'@'{$host}'";

            foreach (self::TABEL as $t) {
                DB::unprepared("GRANT SELECT, INSERT, UPDATE, DELETE ON `{$db}`.`{$t}` TO {$akun}");
            }
            foreach (self::TABEL_LOG as $t) {
                DB::unprepared("GRANT SELECT, INSERT ON `{$db}`.`{$t}` TO {$akun}");
            }
        }
    }

    public function down(): void
    {
        $db = DB::getDatabaseName();

        foreach ($this->hosts() as $host) {
            $akun = "'" . self::USER . "'@'{$host}'";

            foreach (array_merge(self::TABEL, self::TABEL_LOG) as $t) {
                try {
                    DB::unprepared("REVOKE ALL PRIVILEGES ON `{$db}`.`{$t}` FROM {$akun}");
                } catch (\Throwable) {
                    // hak memang belum ada
                }
            }
        }
    }
};