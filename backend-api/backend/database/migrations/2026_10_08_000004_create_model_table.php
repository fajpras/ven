<?php
use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        Schema::create('model', function (Blueprint $table) {
            $table->bigIncrements('id_model');
            $table->enum('jenis', ['hall', 'objek']);
            $table->string('file', 255);
            $table->string('kapasitas', 2);
            $table->string('nama', 60);
            $table->string('gambar', 255);
        });
    }
    public function down(): void { Schema::dropIfExists('model'); }
};