<?php
use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('ruangan', function (Blueprint $table) {
            $table->bigIncrements('id_ruangan');
            $table->unsignedBigInteger('id_pameran')->index()->nullable();
            $table->foreign('id_pameran')->references('id_pameran')->on('pameran')->cascadeOnDelete();
            $table->enum('jenis', ['privat', 'umum']);
        });
    }
    public function down(): void { Schema::dropIfExists('ruangan'); }
};