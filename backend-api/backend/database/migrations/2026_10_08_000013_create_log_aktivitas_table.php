<?php
use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('log_aktivitas', function (Blueprint $table) {
            $table->bigIncrements('id_log');
            $table->uuid('id_pengguna')->nullable()->index();
            $table->foreign('id_pengguna')->references('id_pengguna')->on('pengguna')->nullOnDelete();
            $table->string('entitas', 100);
            $table->string('jenis', 50)->nullable();
            $table->string('status', 20)->default('berhasil');
            $table->string('pesan', 255)->nullable();
            $table->string('ip_address', 100)->nullable();
            $table->timestamp('waktu')->useCurrent()->index();
            $table->string('aksi', 20)->nullable();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('log_aktivitas');
    }
};