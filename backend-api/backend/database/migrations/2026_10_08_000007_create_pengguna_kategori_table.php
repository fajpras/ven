<?php
use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('pengguna_kategori', function (Blueprint $table) {
            $table->uuid('id_pengguna')->index();
            $table->foreign('id_pengguna')->references('id_pengguna')->on('pengguna')->cascadeOnDelete();
            $table->unsignedBigInteger('id_kategori')->index();
            $table->foreign('id_kategori')->references('id_kategori')->on('kategori')->cascadeOnDelete();
            $table->decimal('bobot', 4, 3);
            
            $table->primary(['id_pengguna', 'id_kategori']);
        });
    }
    public function down(): void { Schema::dropIfExists('pengguna_kategori'); }
};