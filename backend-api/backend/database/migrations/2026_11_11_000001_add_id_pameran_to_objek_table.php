<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    public function up(): void
    {
        if (!Schema::hasColumn('objek', 'id_pameran')) {
            Schema::table('objek', function (Blueprint $table) {
                $table->unsignedBigInteger('id_pameran')->nullable()->after('id_objek')->index();
                $table->foreign('id_pameran')->references('id_pameran')->on('pameran')->cascadeOnDelete();
            });
        }
    }

    public function down(): void
    {
        if (Schema::hasColumn('objek', 'id_pameran')) {
            Schema::table('objek', function (Blueprint $table) {
                $table->dropForeign(['id_pameran']);
                $table->dropColumn('id_pameran');
            });
        }
    }
};
