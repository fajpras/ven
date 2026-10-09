<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class PosisiObjek extends Model
{
    public $timestamps = false;

    protected $table = 'posisi_objek';
    protected $primaryKey = 'id_posisi';

    protected $fillable = [
        'posisi_x', 'posisi_y', 'posisi_z',
        'rotasi_x', 'rotasi_y', 'rotasi_z',
    ];

    public function objek(): HasMany
    {
        return $this->hasMany(Objek::class, 'id_posisi', 'id_posisi');
    }
}