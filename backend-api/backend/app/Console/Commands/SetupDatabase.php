<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;
use PDO;
use Throwable;

class SetupDatabase extends Command
{
    protected $signature = 'ven:setup-db';
    protected $description = 'Membuat database, user, dan role MySQL serta menyelaraskan hak akses granular';

    public function handle(): int
    {
        $host = env('DB_HOST', '127.0.0.1');
        $host = $host === 'localhost' ? '127.0.0.1' : $host;
        $port = env('DB_PORT', '3306');
        $db   = env('DB_DATABASE');
        $ch   = env('VEN_DB_CLIENT_HOST', env('DB_CLIENT_HOST', 'localhost'));
        $root = env('MYSQL_ROOT_USER', 'root');

        if (! $db || ! preg_match('/^[A-Za-z0-9_]+$/', $db)) {
            $this->error('DB_DATABASE kosong atau berisi karakter tidak valid.');
            return self::FAILURE;
        }

        $akun = [];
        foreach (['APP', 'USER', 'ADMIN', 'DBA'] as $k) {
            $u = env("DB_{$k}_USERNAME");
            $p = env("DB_{$k}_PASSWORD");
            if (! $u || ! $p) {
                $this->error("DB_{$k}_USERNAME / DB_{$k}_PASSWORD belum diisi di .env");
                return self::FAILURE;
            }
            $akun[$k] = [$u, $p];
        }

        $rootPass = (string) $this->secret("Password MySQL untuk '{$root}' (kosongkan jika tidak ada)");

        try {
            $pdo = new PDO("mysql:host={$host};port={$port};charset=utf8mb4", $root, $rootPass, [
                PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION,
            ]);

            $q = fn (string $v) => $pdo->quote($v);
            $h = $q($ch);
            $acc = fn (string $k) => $q($akun[$k][0]).'@'.$h;

            $inisialisasi = [
                "CREATE DATABASE IF NOT EXISTS `{$db}` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
                "CREATE ROLE IF NOT EXISTS 'admin_role', 'user_role'",
            ];

            foreach ($akun as $k => [$u, $p]) {
                $inisialisasi[] = "CREATE USER IF NOT EXISTS ".$acc($k)." IDENTIFIED BY ".$q($p);
                $inisialisasi[] = "ALTER USER ".$acc($k)." IDENTIFIED BY ".$q($p);
            }

            $inisialisasi[] = "GRANT ALL PRIVILEGES ON `{$db}`.* TO ".$acc('DBA')." WITH GRANT OPTION";
            $inisialisasi[] = "GRANT 'user_role' TO ".$acc('USER');
            $inisialisasi[] = "GRANT 'admin_role' TO ".$acc('ADMIN');
            $inisialisasi[] = "SET DEFAULT ROLE 'user_role' TO ".$acc('USER');
            $inisialisasi[] = "SET DEFAULT ROLE 'admin_role' TO ".$acc('ADMIN');

            foreach ($inisialisasi as $sql) {
                $pdo->exec($sql);
            }

            // Cabut hak global database agar hak akses granular per tabel/kolom berlaku
            foreach (['user_role', 'admin_role', $acc('APP')] as $penerima) {
                try {
                    $pdo->exec("REVOKE ALL PRIVILEGES ON `{$db}`.* FROM {$penerima}");
                } catch (Throwable) {
                }
            }

            // Cek apakah tabel sudah terbentuk di database
            $cekTabel = $pdo->query("SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = {$q($db)} AND TABLE_NAME = 'pengguna'")->fetch();

            if ($cekTabel) {
                $this->terapkanHakAkses($pdo, $db, $acc('APP'));
                $this->info("Hak akses per tabel/kolom berhasil diselaraskan dengan 2026_10_08_000016_setup_database_grants.");
            } else {
                $this->info("Database, user, dan role berhasil dibuat. Tabel belum ada.");
                $this->info("Jalankan: php artisan migrate --database=mysql_dba untuk membuat tabel & menerapkan hak akses.");
            }

            $pdo->exec("FLUSH PRIVILEGES");
        } catch (Throwable $e) {
            $this->error('Gagal: '.$e->getMessage());
            return self::FAILURE;
        }

        return self::SUCCESS;
    }

    private function terapkanHakAkses(PDO $pdo, string $db, string $appAcc): void
    {
        $daftarHak = [
            $appAcc        => $this->hakApp(),
            '`user_role`'  => $this->hakUser(),
            '`admin_role`' => $this->hakAdmin(),
        ];

        foreach ($daftarHak as $penerima => $tabelHak) {
            foreach ($tabelHak as $tabel => $privs) {
                $sql = 'GRANT '.implode(', ', $privs)." ON `{$db}`.`{$tabel}` TO {$penerima}";
                try {
                    $pdo->exec($sql);
                } catch (Throwable $e) {
                    $this->warn("Peringatan saat menerapkan hak ke {$tabel}: ".$e->getMessage());
                }
            }
        }

        // View publik jika sudah ada
        $cekView = $pdo->query("SELECT TABLE_NAME FROM information_schema.VIEWS WHERE TABLE_SCHEMA = ".$pdo->quote($db)." AND TABLE_NAME = 'v_pengguna_publik'")->fetch();
        if ($cekView) {
            foreach ([$appAcc, '`user_role`', '`admin_role`'] as $penerima) {
                try {
                    $pdo->exec("GRANT SELECT ON `{$db}`.`v_pengguna_publik` TO {$penerima}");
                } catch (Throwable) {
                }
            }
        }
    }

    private function hakApp(): array
    {
        $baca = ['SELECT'];

        return [
            'pengguna' => ['SELECT', 'INSERT', 'UPDATE (kata_sandi, google_id, google_id_bidx, foto_profil, status, updated_at)'],
            'reset_kata_sandi' => ['SELECT', 'INSERT', 'UPDATE (percobaan, used_at)'],
            'personal_access_tokens' => ['SELECT', 'INSERT', 'UPDATE (last_used_at, updated_at)', 'DELETE'],
            'sanksi_akun' => $baca, 'kategori' => $baca, 'model' => $baca, 'objek' => $baca,
            'posisi_objek' => $baca, 'pameran' => $baca, 'ruangan' => $baca, 'karya' => $baca,
            'undangan' => $baca, 'pengguna_kategori' => $baca,
            'log_aktivitas' => ['INSERT'], 'log_keamanan' => ['INSERT'],
        ];
    }

    private function hakUser(): array
    {
        $baca = ['SELECT'];

        return [
            'pengguna' => ['SELECT', 'UPDATE (nama, nama_panggilan, deskripsi, foto_profil, email, kata_sandi, status, onboarding_selesai, updated_at)'],
            'pengguna_kategori' => ['SELECT', 'INSERT', 'UPDATE', 'DELETE'],
            'pameran' => ['SELECT', 'INSERT', 'UPDATE (id_model, judul, banner, warna, tipe, updated_at, deleted_at)'],
            'ruangan' => ['SELECT', 'INSERT'],
            'karya' => ['SELECT', 'INSERT', 'UPDATE (id_kategori, id_pameran, id_objek, judul, jenis, deskripsi, file, updated_at, deleted_at)'],
            'objek' => ['SELECT', 'INSERT', 'UPDATE (id_model, jenis, id_posisi)'],
            'posisi_objek' => ['SELECT', 'INSERT', 'DELETE'],
            'undangan' => ['SELECT', 'INSERT', 'UPDATE (status, catatan, tautan)'],
            'reset_kata_sandi' => ['SELECT', 'INSERT', 'UPDATE (percobaan, used_at)'],
            'personal_access_tokens' => ['SELECT'],
            'kategori' => $baca, 'model' => $baca, 'sanksi_akun' => $baca,
        ];
    }

    private function hakAdmin(): array
    {
        $baca = ['SELECT'];

        return [
            'pengguna' => ['SELECT', 'UPDATE (status, updated_at, deleted_at)'],
            'sanksi_akun' => ['SELECT', 'INSERT', 'UPDATE'],
            'karya' => ['SELECT', 'UPDATE (deleted_at, updated_at)'],
            'pameran' => ['SELECT', 'UPDATE (deleted_at)'],
            'personal_access_tokens' => ['SELECT'],
            'log_aktivitas' => ['SELECT'],
            'log_keamanan' => ['SELECT'],
            'kategori' => $baca, 'model' => $baca, 'objek' => $baca, 'posisi_objek' => $baca,
            'ruangan' => $baca, 'pengguna_kategori' => $baca, 'undangan' => $baca,
        ];
    }
}
