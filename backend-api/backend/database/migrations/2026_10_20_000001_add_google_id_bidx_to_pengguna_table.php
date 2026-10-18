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
            $table->string('google_id_bidx', 64)
                ->nullable()
                ->after('google_id')
                ->index('pengguna_google_id_bidx_index');
        });

        $this->beriHakKolomBidx();
    }

    public function down(): void
    {
        Schema::table('pengguna', function (Blueprint $table) {
            $table->dropIndex('pengguna_google_id_bidx_index');
            $table->dropColumn('google_id_bidx');
        });
    }

    /**
     * Akun aplikasi menulis google_id_bidx saat login Google, tetapi grant
     * kolom pada migrasi awal belum mencakupnya. Tambahkan hak secara idempotent.
     */
    private function beriHakKolomBidx(): void
    {
        $db = DB::getDatabaseName();
        $host = config('database.ven_client_host', 'localhost');
        $app = "'".env('DB_APP_USERNAME', 'ven_app')."'@'{$host}'";

        try {
            DB::unprepared("GRANT SELECT, INSERT, UPDATE (kata_sandi, google_id, google_id_bidx, foto_profil, status, updated_at) ON `{$db}`.`pengguna` TO {$app}");
        } catch (Throwable $e) {
            // User aplikasi belum dibuat (mis. migrasi dengan root); hak diatur lewat ven:setup-db.
        }
    }
};
