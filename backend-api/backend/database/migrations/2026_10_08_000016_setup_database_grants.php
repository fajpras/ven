<?php
use Illuminate\Database\Migrations\Migration;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    private function hakApp(): array
    {
        $baca = ['SELECT'];
        return [
            'pengguna' => ['SELECT', 'INSERT', 'UPDATE (kata_sandi, google_id, foto_profil, status, updated_at)'],
            'reset_kata_sandi' => ['SELECT', 'INSERT', 'UPDATE (percobaan, used_at)'],
            'personal_access_tokens' => ['SELECT', 'INSERT', 'UPDATE (last_used_at, updated_at)', 'DELETE'],
            'sanksi_akun' => $baca, 'kategori' => $baca, 'model' => $baca, 'objek' => $baca,
            'posisi_objek' => $baca, 'pameran' => $baca, 'ruangan' => $baca, 'karya' => $baca,
            'undangan' => $baca, 'pengguna_kategori' => $baca,
            'log_aktifitas' => ['INSERT'], 'log_keamanan' => ['INSERT'],
        ];
    }

    private function hakUser(): array
    {
        $baca = ['SELECT'];
        return [
            'pengguna' => ['SELECT', 'UPDATE (nama, nama_panggilan, deskripsi, foto_profil, email, kata_sandi, status, updated_at)'],
            'pengguna_kategori' => ['SELECT', 'INSERT', 'UPDATE', 'DELETE'],
            'pameran' => ['SELECT', 'INSERT', 'UPDATE (id_model, judul, banner, tipe, deleted_at)'],
            'ruangan' => ['SELECT', 'INSERT'],
            'karya' => ['SELECT', 'INSERT', 'UPDATE (id_kategori, id_pameran, id_objek, judul, jenis, deskripsi, file, updated_at, deleted_at)'],
            'objek' => ['SELECT', 'INSERT', 'UPDATE (id_model, jenis, id_posisi)'],
            'posisi_objek' => ['SELECT', 'INSERT', 'DELETE'],
            'undangan' => ['SELECT', 'INSERT', 'UPDATE (status, catatan, tautan)'],
            'reset_kata_sandi' => ['SELECT', 'INSERT', 'UPDATE (percobaan, used_at)'],
            'personal_access_tokens' => ['SELECT', 'DELETE'],
            'kategori' => $baca, 'model' => $baca, 'sanksi_akun' => $baca,
        ];
    }

    private function hakAdmin(): array
    {
        $baca = ['SELECT'];
        return [
            'pengguna' => ['SELECT', 'UPDATE (status, updated_at)'],
            'sanksi_akun' => ['SELECT', 'INSERT', 'UPDATE'],
            'karya' => ['SELECT', 'UPDATE (deleted_at, updated_at)'],
            'pameran' => ['SELECT', 'UPDATE (deleted_at)'],
            'personal_access_tokens' => ['SELECT', 'DELETE'],
            'log_aktifitas' => ['SELECT'],
            'log_keamanan' => ['SELECT'],
            'kategori' => $baca, 'model' => $baca, 'objek' => $baca, 'posisi_objek' => $baca,
            'ruangan' => $baca, 'pengguna_kategori' => $baca, 'undangan' => $baca,
        ];
    }

    private function terapkan(array $hak, string $penerima): void
    {
        $db = DB::getDatabaseName();
        foreach ($hak as $tabel => $privs) {
            $this->jalankan("GRANT ".implode(', ', $privs)." ON `{$db}`.`{$tabel}` TO {$penerima}", $penerima);
        }
    }

    private function jalankan(string $sql, string $penerima): void
    {
        try {
            DB::unprepared($sql);
        } catch (\Throwable $e) {
            throw new RuntimeException(
                "Gagal memberi hak ke {$penerima}. Pastikan database/setup/setup-mysql.sh sudah dijalankan "
                ."dan migrasi dijalankan dengan --database=mysql_dba. Detail: ".$e->getMessage(),
                0,
                $e,
            );
        }
    }

    public function up(): void
    {
        $host = config('database.ven_client_host', 'localhost');
        $app = "'".env('DB_APP_USERNAME', 'ven_app')."'@'{$host}'";

        $db = DB::getDatabaseName();
        foreach (['log_aktifitas', 'log_keamanan'] as $tabel) {
            foreach (['`user_role`', '`admin_role`'] as $role) {
                try { DB::unprepared("REVOKE INSERT ON `{$db}`.`{$tabel}` FROM {$role}"); } catch (\Throwable $e) {}
            }
        }

        $this->terapkan($this->hakApp(), $app);
        $this->terapkan($this->hakUser(), "`user_role`");
        $this->terapkan($this->hakAdmin(), "`admin_role`");
    }

    public function down(): void
    {
        $db = DB::getDatabaseName();
        $host = config('database.ven_client_host', 'localhost');
        $app = "'".env('DB_APP_USERNAME', 'ven_app')."'@'{$host}'";

        foreach ([[$this->hakApp(), $app], [$this->hakUser(), '`user_role`'], [$this->hakAdmin(), '`admin_role`']] as [$hak, $penerima]) {
            foreach (array_keys($hak) as $tabel) {
                try {
                    DB::unprepared("REVOKE ALL PRIVILEGES ON `{$db}`.`{$tabel}` FROM {$penerima}");
                } catch (\Throwable) {
                }
            }
        }
    }
};