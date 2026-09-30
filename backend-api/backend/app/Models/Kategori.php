<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Kategori extends Model
{
    public $timestamps = false;

    protected $table = 'kategori';
    protected $primaryKey = 'id_kategori';

    protected $fillable = ['kategori'];

    public function karya(): HasMany
    {
        return $this->hasMany(Karya::class, 'id_kategori', 'id_kategori');
    }

    public function pengguna(): BelongsToMany
    {
        return $this->belongsToMany(Pengguna::class, 'pengguna_kategori', 'id_kategori', 'id_pengguna')
            ->using(PenggunaKategori::class)
            ->withPivot('bobot');
    }
}