<?php
use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('reset_kata_sandi', function (Blueprint $table) {
            $table->bigIncrements('id_reset');
            $table->enum('tujuan', ['registrasi', 'ganti_email', 'reset_kata_sandi']);
            $table->string('email', 254);
            $table->uuid('id_pengguna')->nullable()->index();
            $table->foreign('id_pengguna')->references('id_pengguna')->on('pengguna')->cascadeOnDelete();
            $table->string('otp_hash', 255);
            $table->unsignedTinyInteger('percobaan')->default(0);
            $table->timestamp('expired_at');
            $table->timestamp('used_at')->nullable();
            $table->timestamp('created_at')->useCurrent();
            $table->index(['tujuan', 'email', 'created_at'], 'idx_reset_kata_sandi_tujuan_email');
        });
    }
    public function down(): void { Schema::dropIfExists('reset_kata_sandi'); }
};