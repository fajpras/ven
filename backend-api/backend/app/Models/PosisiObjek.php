<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasOne;

class PosisiObjek extends Model
{
    public $timestamps = false;

    protected $table = 'posisi_objek';
    protected $primaryKey = 'id_posisi';

    protected $fillable = [
        'posisi_x', 'posisi_y', 'posisi_z',
        'rotasi_x', 'rotasi_y', 'rotasi_z',
    ];

    protected function casts(): array
    {
        return [
            'posisi_x' => 'double', 'posisi_y' => 'double', 'posisi_z' => 'double',
            'rotasi_x' => 'double', 'rotasi_y' => 'double', 'rotasi_z' => 'double',
        ];
    }

    public function objek(): HasOne
    {
        return $this->hasOne(Objek::class, 'id_posisi', 'id_posisi');
    }
}