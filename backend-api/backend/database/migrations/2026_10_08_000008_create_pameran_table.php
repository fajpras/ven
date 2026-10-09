<?php
use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('pameran', function (Blueprint $table) {
            $table->bigIncrements('id_pameran');
            $table->uuid('id_pengguna')->index();
            $table->foreign('id_pengguna')->references('id_pengguna')->on('pengguna')->restrictOnDelete();
            $table->unsignedBigInteger('id_model')->index();
            $table->foreign('id_model')->references('id_model')->on('model')->restrictOnDelete();
            $table->string('judul', 255);
            $table->string('banner', 255);
            $table->enum('tipe', ['kecil', 'sedang', 'besar', 'acara']);
            $table->timestamp('created_at')->useCurrent();
            $table->timestamp('updated_at')->nullable()->useCurrentOnUpdate();
            $table->timestamp('deleted_at')->nullable();
        });
    }
    public function down(): void { Schema::dropIfExists('pameran'); }
};