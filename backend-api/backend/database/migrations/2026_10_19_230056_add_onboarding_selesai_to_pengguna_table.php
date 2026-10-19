<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::table('pengguna', function (Blueprint $table) {
            $table->boolean('onboarding_selesai')
                ->default(false)
                ->after('status');
        });

        $this->beriHakKolomOnboarding();
    }

    public function down(): void
    {
        Schema::table('pengguna', function (Blueprint $table) {
            $table->dropColumn('onboarding_selesai');
        });
    }

    /**
     * Pengguna memperbarui status onboarding lewat koneksi user_role sehingga
     * grant kolom perlu ditambahkan. Abaikan bila role database belum dibuat.
     */
    private function beriHakKolomOnboarding(): void
    {
        $database = DB::getDatabaseName();

        try {
            DB::unprepared("GRANT SELECT, UPDATE (onboarding_selesai) ON `{$database}`.`pengguna` TO `user_role`");
        } catch (Throwable) {
            // Role dibuat terpisah melalui ven:setup-db; grant akan diterapkan di sana.
        }
    }
};
