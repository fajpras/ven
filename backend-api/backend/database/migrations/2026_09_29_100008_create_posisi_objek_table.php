<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('posisi_objek', function (Blueprint $table) {
            $table->bigIncrements('id_posisi');
            $table->double('posisi_x')->nullable();
            $table->double('posisi_y')->nullable();
            $table->double('posisi_z')->nullable();
            $table->double('rotasi_x')->nullable();
            $table->double('rotasi_y')->nullable();
            $table->double('rotasi_z')->nullable();
        });
    }

    public function down(): void
    {
        Schema::dropIfExists('posisi_objek');
    }
};