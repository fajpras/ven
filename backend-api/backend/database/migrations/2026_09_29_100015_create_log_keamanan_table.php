<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('log_keamanan', function (Blueprint $table) {
            $table->bigIncrements('id_log');
            $table->binary('id_pengguna', 16, true)->nullable()->index();
            $table->foreign('id_pengguna')->references('id_pengguna')->on('pengguna')->nullOnDelete();
            $table->string('event', 60);
            $table->string('endpoint', 100)->nullable();
            $table->string('method', 60)->nullable();
            $table->integer('status_kode')->nullable();
            $table->string('ip_address', 100)->nullable();
            $table->text('user_agent')->nullable();
            $table->string('pesan', 255)->nullable();
            $table->timestamp('waktu')->nullable()->useCurrent()->index();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('log_keamanan');
    }
};