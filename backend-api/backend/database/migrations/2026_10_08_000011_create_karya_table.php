<?php
use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration {
    public function up(): void
    {
        Schema::create('karya', function (Blueprint $table) {
            $table->uuid('id_karya')->primary();
            $table->unsignedBigInteger('id_kategori');
            $table->foreign('id_kategori')->references('id_kategori')->on('kategori')->restrictOnDelete();
            $table->uuid('id_pengguna');
            $table->foreign('id_pengguna')->references('id_pengguna')->on('pengguna')->restrictOnDelete();
            $table->unsignedBigInteger('id_pameran')->nullable();
            $table->foreign('id_pameran')->references('id_pameran')->on('pameran')->nullOnDelete();
            $table->unsignedBigInteger('id_objek')->nullable();
            $table->foreign('id_objek')->references('id_objek')->on('objek')->nullOnDelete();
            $table->string('judul', 255);
            $table->enum('jenis', ['poster', 'video']);
            $table->text('deskripsi');
            $table->string('file', 255);
            $table->timestamp('created_at')->useCurrent();
            $table->timestamp('updated_at')->nullable()->useCurrentOnUpdate();
            $table->softDeletes();
        });
    }
    public function down(): void { Schema::dropIfExists('karya'); }
};