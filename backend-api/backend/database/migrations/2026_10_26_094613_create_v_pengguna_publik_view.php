<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Support\Facades\DB;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        DB::statement('
            CREATE OR REPLACE VIEW v_pengguna_publik AS
            SELECT
                id_pengguna,
                nama,
                nama_panggilan,
                nomor_hp,
                deskripsi,
                onboarding_selesai,
                email,
                foto_profil,
                status,
                created_at,
                updated_at,
                deleted_at
            FROM pengguna
        ');

        $db = DB::getDatabaseName();
        $host = config('database.ven_client_host', 'localhost');
        $app = "'".env('DB_APP_USERNAME', 'ven_app')."'@'{$host}'";

        foreach ([$app, '`user_role`', '`admin_role`'] as $penerima) {
            try {
                DB::unprepared("GRANT SELECT ON `{$db}`.`v_pengguna_publik` TO {$penerima}");
            } catch (Throwable) {
            }
        }
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        DB::statement('DROP VIEW IF EXISTS v_pengguna_publik');
    }
};
