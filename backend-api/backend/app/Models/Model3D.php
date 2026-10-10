<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Model3D extends Model
{
    public $timestamps = false;

    protected $table = 'model';
    protected $primaryKey = 'id_model';

    protected $fillable = ['jenis', 'file', 'kapasitas', 'nama', 'gambar'];

    public function objek(): HasMany
    {
        return $this->hasMany(Objek::class, 'id_model', 'id_model');
    }

    public function pameran(): HasMany
    {
        return $this->hasMany(Pameran::class, 'id_model', 'id_model');
    }
}