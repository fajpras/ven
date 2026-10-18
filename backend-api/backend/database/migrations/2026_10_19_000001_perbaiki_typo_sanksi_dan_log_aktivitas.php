<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    private const MIGRASI_LOG_LAMA = '2026_10_08_000013_create_log_aktifitas_table';

    private const MIGRASI_LOG_BARU = '2026_10_08_000013_create_log_aktivitas_table';

    /**
     * Database yang sudah dimigrasi sebelum perbaikan typo masih memakai nilai
     * enum "ditanggungkan" dan tabel "log_aktifitas". Migrasi ini menyelaraskan
     * skema tersebut tanpa menghapus data yang sudah ada.
     */
    public function up(): void
    {
        $this->perbaikiTabelLog();
        $this->perbaikiEnumSanksi();
        $this->perbaikiEnumStatusPengguna();
        $this->beriHakLogAktivitas();
        $this->rapikanCatatanMigrasi();
    }

    public function down(): void
    {
        DB::table('migrations')
            ->where('migration', self::MIGRASI_LOG_BARU)
            ->update(['migration' => self::MIGRASI_LOG_LAMA]);

        if (Schema::hasTable('log_aktivitas') && ! Schema::hasTable('log_aktifitas')) {
            Schema::rename('log_aktivitas', 'log_aktifitas');
        }
    }

    private function perbaikiTabelLog(): void
    {
        $adaLama = Schema::hasTable('log_aktifitas');
        $adaBaru = Schema::hasTable('log_aktivitas');

        if ($adaLama && ! $adaBaru) {
            Schema::rename('log_aktifitas', 'log_aktivitas');

            return;
        }

        // Bila migrasi lama dan baru sempat berjalan berdampingan, pindahkan
        // data lama lalu buang tabel sisa.
        if ($adaLama && $adaBaru) {
            if (DB::table('log_aktivitas')->doesntExist()) {
                DB::table('log_aktivitas')->insertUsing(
                    ['id_log', 'id_pengguna', 'entitas', 'jenis', 'status', 'pesan', 'ip_address', 'waktu', 'aksi'],
                    DB::table('log_aktifitas')->select(['id_log', 'id_pengguna', 'entitas', 'jenis', 'status', 'pesan', 'ip_address', 'waktu', 'aksi']),
                );
            }

            Schema::drop('log_aktifitas');
        }
    }

    private function perbaikiEnumSanksi(): void
    {
        if (! $this->punyaNilaiEnum('sanksi_akun', 'jenis_sanksi', 'ditanggungkan')) {
            return;
        }

        DB::statement("ALTER TABLE `sanksi_akun` MODIFY `jenis_sanksi` ENUM('ditanggungkan', 'ditangguhkan', 'diblokir') NOT NULL");
        DB::table('sanksi_akun')->where('jenis_sanksi', 'ditanggungkan')->update(['jenis_sanksi' => 'ditangguhkan']);
        DB::statement("ALTER TABLE `sanksi_akun` MODIFY `jenis_sanksi` ENUM('ditangguhkan', 'diblokir') NOT NULL");
    }

    private function perbaikiEnumStatusPengguna(): void
    {
        if (! $this->punyaNilaiEnum('pengguna', 'status', 'ditanggungkan')) {
            return;
        }

        DB::statement("ALTER TABLE `pengguna` MODIFY `status` ENUM('aktif', 'nonaktifkan', 'ditanggungkan', 'ditangguhkan') NOT NULL DEFAULT 'aktif'");
        DB::table('pengguna')->where('status', 'ditanggungkan')->update(['status' => 'ditangguhkan']);
        DB::statement("ALTER TABLE `pengguna` MODIFY `status` ENUM('aktif', 'nonaktifkan', 'ditangguhkan') NOT NULL DEFAULT 'aktif'");
    }

    private function punyaNilaiEnum(string $tabel, string $kolom, string $nilai): bool
    {
        $tipe = DB::selectOne(
            'SELECT COLUMN_TYPE AS tipe FROM information_schema.COLUMNS
             WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?',
            [$tabel, $kolom],
        )?->tipe;

        return is_string($tipe) && str_contains($tipe, "'{$nilai}'");
    }

    private function beriHakLogAktivitas(): void
    {
        if (! Schema::hasTable('log_aktivitas')) {
            return;
        }

        $db = DB::getDatabaseName();
        $host = config('database.ven_client_host', 'localhost');
        $app = "'".env('DB_APP_USERNAME', 'ven_app')."'@'{$host}'";

        foreach (['`user_role`', '`admin_role`'] as $role) {
            try {
                DB::unprepared("REVOKE INSERT ON `{$db}`.`log_aktivitas` FROM {$role}");
            } catch (Throwable $e) {
                // Role belum ada pada instalasi yang belum menjalankan setup MySQL.
            }
        }

        // Bersihkan hak yang tertinggal pada nama tabel lama yang sudah dibuang.
        foreach ([$app, '`user_role`', '`admin_role`'] as $penerima) {
            try {
                DB::unprepared("REVOKE ALL PRIVILEGES ON `{$db}`.`log_aktifitas` FROM {$penerima}");
            } catch (Throwable $e) {
                // Hak tidak pernah diberikan, tidak ada yang perlu dibersihkan.
            }
        }

        foreach ([
            "GRANT INSERT ON `{$db}`.`log_aktivitas` TO {$app}",
            "GRANT SELECT ON `{$db}`.`log_aktivitas` TO `admin_role`",
        ] as $sql) {
            try {
                DB::unprepared($sql);
            } catch (Throwable $e) {
                // Abaikan bila user/role belum dibuat; hak dapat diatur lewat ven:setup-db.
            }
        }
    }

    private function rapikanCatatanMigrasi(): void
    {
        $adaLama = DB::table('migrations')->where('migration', self::MIGRASI_LOG_LAMA)->exists();

        if (! $adaLama) {
            return;
        }

        if (DB::table('migrations')->where('migration', self::MIGRASI_LOG_BARU)->exists()) {
            DB::table('migrations')->where('migration', self::MIGRASI_LOG_LAMA)->delete();

            return;
        }

        DB::table('migrations')
            ->where('migration', self::MIGRASI_LOG_LAMA)
            ->update(['migration' => self::MIGRASI_LOG_BARU]);
    }
};
