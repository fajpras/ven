<?php

namespace App\Console\Commands;

use Illuminate\Console\Command;
use PDO;
use Throwable;

class SetupDatabase extends Command
{
    protected $signature = 'ven:setup-db {--fresh : Drop database dan buat ulang dari nol}';
    protected $description = 'Membuat database, user, dan role MySQL serta menyelaraskan hak akses granular menggunakan akun ROOT';

    public function handle(): int
    {
        // -------------------------------------------------------------
        // 1️⃣ Gather connection info
        // -------------------------------------------------------------
        $host = env('DB_HOST', '127.0.0.1');
        $host = $host === 'localhost' ? '127.0.0.1' : $host;
        $port = env('DB_PORT', '3306');
        $db   = env('DB_DATABASE');
        $clientHost = env('VEN_DB_CLIENT_HOST', env('DB_CLIENT_HOST', 'localhost'));

        // ROOT account – used to create databases, users, roles, and assign privileges
        $rootUser = env('MYSQL_ROOT_USER', 'root');
        $rootPass = (string) $this->secret("Password MySQL untuk ROOT '{$rootUser}' (kosongkan bila tidak ada)");

        if (! $db || ! preg_match('/^[A-Za-z0-9_]+$/', $db)) {
            $this->error('DB_DATABASE kosong atau berisi karakter tidak valid.');
            return self::FAILURE;
        }

        // -------------------------------------------------------------
        // 2️⃣ Build PDO connection (ROOT)
        // -------------------------------------------------------------
        try {
            $pdo = new PDO(
                "mysql:host={$host};port={$port};charset=utf8mb4",
                $rootUser,
                $rootPass,
                [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION]
            );
        } catch (Throwable $e) {
            $this->error('Gagal terhubung ke MySQL sebagai root: ' . $e->getMessage());
            return self::FAILURE;
        }

        // -------------------------------------------------------------
        // 3️⃣ Collect application accounts (APP, USER, ADMIN, DBA)
        // -------------------------------------------------------------
        $accounts = [];
        foreach (['APP', 'USER', 'ADMIN', 'DBA'] as $key) {
            $user = env("DB_{$key}_USERNAME");
            $pass = env("DB_{$key}_PASSWORD");
            if (! $user || ! $pass) {
                $this->error("DB_{$key}_USERNAME / DB_{$key}_PASSWORD belum diisi di .env");
                return self::FAILURE;
            }
            $accounts[$key] = [$user, $pass];
        }

        // -------------------------------------------------------------
        // 4️⃣ Initialize database, roles, users & default assignments (ROOT)
        // -------------------------------------------------------------
        if ($this->option('fresh')) {
            $this->warn("Menghapus database `{$db}` untuk setup ulang dari nol...");
            $pdo->exec("DROP DATABASE IF EXISTS `{$db}`");
        }

        $quote = fn (string $value) => $pdo->quote($value);
        $hostQuoted = $quote($clientHost);
        $accountString = fn (string $key) => $quote($accounts[$key][0]) . '@' . $hostQuoted;

        $initialQueries = [
            "CREATE DATABASE IF NOT EXISTS `{$db}` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci",
            "CREATE ROLE IF NOT EXISTS 'admin_role', 'user_role'",
        ];

        foreach ($accounts as $key => [$user, $pass]) {
            $initialQueries[] = "CREATE USER IF NOT EXISTS {$accountString($key)} IDENTIFIED BY {$quote($pass)}";
            $initialQueries[] = "ALTER USER {$accountString($key)} IDENTIFIED BY {$quote($pass)}";
        }

        // Grant ALL to DBA so DBA can manage tables, run migrations, and backups
        $initialQueries[] = "GRANT ALL PRIVILEGES ON `{$db}`.* TO {$accountString('DBA')} WITH GRANT OPTION";
        // Grant PROCESS to DBA so mysqldump can dump tablespaces/status without error
        $initialQueries[] = "GRANT PROCESS ON *.* TO {$accountString('DBA')}";

        $initialQueries[] = "GRANT 'user_role' TO {$accountString('USER')}";
        $initialQueries[] = "GRANT 'admin_role' TO {$accountString('ADMIN')}";
        $initialQueries[] = "SET DEFAULT ROLE 'user_role' TO {$accountString('USER')}";
        $initialQueries[] = "SET DEFAULT ROLE 'admin_role' TO {$accountString('ADMIN')}";

        foreach ($initialQueries as $sql) {
            try {
                $pdo->exec($sql);
            } catch (Throwable $e) {
                $this->error("Gagal mengeksekusi query [{$sql}]: " . $e->getMessage());
                return self::FAILURE;
            }
        }

        // -------------------------------------------------------------
        // 5️⃣ Revoke global privileges from non-DBA roles & app
        // -------------------------------------------------------------
        foreach (['`user_role`', '`admin_role`', $accountString('APP')] as $grantee) {
            try {
                $pdo->exec("REVOKE ALL PRIVILEGES ON `{$db}`.* FROM {$grantee}");
            } catch (Throwable) {
                // Ignore if not previously granted
            }
        }

        // -------------------------------------------------------------
        // 6️⃣ Apply granular least-privilege grants if tables exist
        // -------------------------------------------------------------
        $tableCheck = $pdo->query(
            "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = {$quote($db)} AND TABLE_NAME = 'pengguna'"
        )->fetch();

        if ($tableCheck) {
            $this->applyGrants($pdo, $db, $accountString('APP'), $accountString('ADMIN'), $accountString('USER'));
            $this->info('Hak akses granular per tabel/kolom berhasil diselaraskan.');
        } else {
            $this->info('Database, user, dan role berhasil dibuat. Tabel belum ada.');
            $this->info('Jalankan: php artisan migrate --database=mysql_dba untuk membuat tabel & menerapkan hak akses.');
        }

        $pdo->exec('FLUSH PRIVILEGES');
        return self::SUCCESS;
    }

    private function applyGrants(PDO $pdo, string $db, string $appAccount, string $adminAccount, string $userAccount): void
    {
        $grants = [
            $appAccount   => $this->grantApp(),
            '`user_role`' => $this->grantUser(),
            $userAccount  => $this->grantUser(),
            '`admin_role`'=> $this->grantAdmin(),
            $adminAccount => $this->grantAdmin(),
        ];

        foreach ($grants as $grantee => $tables) {
            foreach ($tables as $table => $privileges) {
                $sql = 'GRANT ' . implode(', ', $privileges) . " ON `{$db}`.`{$table}` TO {$grantee}";
                try {
                    $pdo->exec($sql);
                } catch (Throwable $e) {
                    $this->warn("Peringatan saat menerapkan hak ke {$table}: " . $e->getMessage());
                }
            }
        }

        // Grant SELECT on public view if it exists
        $viewExists = $pdo->query(
            "SELECT TABLE_NAME FROM information_schema.VIEWS WHERE TABLE_SCHEMA = " . $pdo->quote($db) .
            " AND TABLE_NAME = 'v_pengguna_publik'"
        )->fetch();

        if ($viewExists) {
            foreach ([$appAccount, '`user_role`', '`admin_role`', $adminAccount, $userAccount] as $grantee) {
                try {
                    $pdo->exec("GRANT SELECT ON `{$db}`.`v_pengguna_publik` TO {$grantee}");
                } catch (Throwable) {
                    // Ignore
                }
            }
        }
    }

    private function grantApp(): array
    {
        $read = ['SELECT'];
        return [
            'pengguna'               => ['SELECT', 'INSERT', 'UPDATE (kata_sandi, google_id, google_id_bidx, foto_profil, status, updated_at)'],
            'reset_kata_sandi'       => ['SELECT', 'INSERT', 'UPDATE (percobaan, used_at)'],
            'personal_access_tokens' => ['SELECT', 'INSERT', 'UPDATE (last_used_at, updated_at)', 'DELETE'],
            'sanksi_akun'            => $read,
            'kategori'               => $read,
            'model'                  => $read,
            'objek'                  => $read,
            'posisi_objek'           => $read,
            'pameran'                => $read,
            'ruangan'                => $read,
            'karya'                  => $read,
            'undangan'               => $read,
            'pengguna_kategori'      => $read,
            'log_aktivitas'          => ['INSERT'],
            'log_keamanan'           => ['INSERT'],
        ];
    }

    private function grantUser(): array
    {
        $read = ['SELECT'];
        return [
            'pengguna'               => ['SELECT', 'UPDATE (nama, nama_panggilan, deskripsi, foto_profil, email, kata_sandi, status, onboarding_selesai, updated_at)'],
            'pengguna_kategori'      => ['SELECT', 'INSERT', 'UPDATE', 'DELETE'],
            'pameran'                => ['SELECT', 'INSERT', 'UPDATE (id_model, judul, banner, warna, tipe, updated_at, deleted_at)'],
            'ruangan'                => ['SELECT', 'INSERT'],
            'karya'                  => ['SELECT', 'INSERT', 'UPDATE (id_kategori, id_pameran, id_objek, judul, jenis, deskripsi, file, updated_at, deleted_at)'],
            'objek'                  => ['SELECT', 'INSERT', 'UPDATE (id_model, jenis, id_posisi)'],
            'posisi_objek'           => ['SELECT', 'INSERT', 'DELETE'],
            'undangan'               => ['SELECT', 'INSERT', 'UPDATE (status, catatan, tautan)'],
            'reset_kata_sandi'       => ['SELECT', 'INSERT', 'UPDATE (percobaan, used_at)'],
            'personal_access_tokens' => ['SELECT'],
            'kategori'               => $read,
            'model'                  => $read,
            'sanksi_akun'            => $read,
        ];
    }

    private function grantAdmin(): array
    {
        $read = ['SELECT'];
        return [
            'pengguna'      => ['SELECT', 'UPDATE (status, updated_at, deleted_at)'],
            'sanksi_akun'   => ['SELECT', 'INSERT', 'UPDATE'],
            'karya'         => ['SELECT', 'UPDATE (deleted_at, updated_at)'],
            'pameran'       => ['SELECT', 'UPDATE (deleted_at, updated_at)'],
            'personal_access_tokens' => ['SELECT', 'DELETE'],
            'log_aktivitas' => ['SELECT'],
            'log_keamanan'  => ['SELECT'],
            'kategori'      => $read,
            'model'         => $read,
            'objek'         => $read,
            'posisi_objek'  => $read,
            'ruangan'       => $read,
            'pengguna_kategori' => $read,
            'undangan'      => $read,
        ];
    }
}