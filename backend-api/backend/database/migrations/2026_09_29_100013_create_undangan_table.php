<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('undangan', function (Blueprint $table) {
            $table->bigIncrements('id_undangan');
            $table->binary('id_pengguna', 16, true)->index();
            $table->foreign('id_pengguna')->references('id_pengguna')->on('pengguna')->cascadeOnDelete();
            $table->unsignedBigInteger('id_ruangan')->index();
            $table->foreign('id_ruangan')->references('id_ruangan')->on('ruangan')->cascadeOnDelete();
            $table->enum('status', ['diproses', 'disetujui', 'ditolak']);
            $table->timestamp('waktu');
            $table->string('catatan', 255)->nullable();
            $table->timestamp('timestamp')->useCurrent();
            $table->string('tautan', 255)->nullable()->unique();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('undangan');
    }
};