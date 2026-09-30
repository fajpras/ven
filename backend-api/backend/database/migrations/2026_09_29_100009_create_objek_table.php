<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('objek', function (Blueprint $table) {
            $table->bigIncrements('id_objek');
            $table->unsignedBigInteger('id_model')->index();
            $table->foreign('id_model')->references('id_model')->on('model')->restrictOnDelete();
            $table->enum('jenis', ['panel', 'aset']);
            $table->unsignedBigInteger('id_posisi')->nullable()->index();
            $table->foreign('id_posisi')->references('id_posisi')->on('posisi_objek')->nullOnDelete();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('objek');
    }
};