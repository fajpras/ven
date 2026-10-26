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
                created_at
            FROM pengguna
        ');
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        DB::statement('DROP VIEW IF EXISTS v_pengguna_publik');
    }
};
