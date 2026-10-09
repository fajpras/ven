<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Relations\Pivot;

class PenggunaKategori extends Pivot
{
    public $incrementing = false;
    public $timestamps = false;

    protected $table = 'pengguna_kategori';

    protected $fillable = ['id_pengguna', 'id_kategori', 'bobot'];

    protected function casts(): array
    {
        return ['bobot' => 'decimal:3'];
    }
}