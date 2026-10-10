<?php
use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('sanksi_akun', function (Blueprint $table) {
            $table->bigIncrements('id_sanksi');
            $table->uuid('id_pengguna')->index();
            $table->foreign('id_pengguna')->references('id_pengguna')->on('pengguna')->cascadeOnDelete();
            $table->enum('jenis_sanksi', ['ditanggungkan', 'diblokir']);
            $table->string('alasan', 255);
            $table->timestamp('mulai');
            $table->timestamp('berakhir')->nullable();
            $table->timestamp('created_at')->useCurrent();
        });
        
        DB::statement('ALTER TABLE sanksi_akun ADD CONSTRAINT ck_sanksi_periode
                       CHECK (berakhir IS NULL OR berakhir > mulai)');
    }
    public function down(): void { Schema::dropIfExists('sanksi_akun'); }
};