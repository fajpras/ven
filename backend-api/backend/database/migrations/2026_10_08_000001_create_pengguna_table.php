<?php
use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('pengguna', function (Blueprint $table) {
            $table->uuid('id_pengguna')->primary();
            $table->string('nama', 30);
            $table->string('nama_panggilan', 40)->nullable()->unique('pengguna_nama_panggilan_unique');
            $table->string('kata_sandi', 255)->nullable();
            $table->text('deskripsi')->nullable();
            $table->string('google_id', 255)->nullable();
            $table->string('email', 254)->unique();
            $table->string('foto_profil', 255)->nullable();
            $table->enum('role', ['admin', 'pengguna'])->default('pengguna');
            $table->enum('status', ['aktif', 'nonaktifkan', 'ditanggungkan'])->default('aktif');
            $table->timestamp('created_at')->useCurrent();
            $table->timestamp('updated_at')->nullable()->useCurrentOnUpdate();
            $table->timestamp('deleted_at')->nullable();
        });
    }
    public function down(): void { Schema::dropIfExists('pengguna'); }
};