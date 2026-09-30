<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('log_aktifitas', function (Blueprint $table) {
            $table->bigIncrements('id_log');
            $table->binary('id_pengguna', 16, true)->nullable()->index();
            $table->foreign('id_pengguna')->references('id_pengguna')->on('pengguna')->nullOnDelete();
            $table->string('entitas', 60);
            $table->binary('id_entitas', 16, true)->nullable();
            $table->string('status', 20);
            $table->string('pesan', 255)->nullable();
            $table->string('ip_address', 100)->nullable();
            $table->timestamp('waktu')->useCurrent()->index();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('log_aktifitas');
    }
};