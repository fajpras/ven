<?php

namespace App\Models;

use App\Models\Concerns\HasBinaryUuid;
use Illuminate\Database\Eloquent\Relations\Pivot;

/** Pivot pengguna <-> kategori (primary key komposit). */
class PenggunaKategori extends Pivot
{
    use HasBinaryUuid;

    public $incrementing = false;
    public $timestamps = false;

    protected $table = 'pengguna_kategori';

    protected array $uuidColumns = ['id_pengguna'];

    protected $fillable = ['id_pengguna', 'id_kategori', 'bobot'];

    protected function casts(): array
    {
        return ['bobot' => 'decimal:3'];
    }
}