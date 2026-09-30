<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Objek extends Model
{
    public $timestamps = false;

    protected $table = 'objek';
    protected $primaryKey = 'id_objek';

    protected $fillable = ['id_model', 'jenis', 'id_posisi'];

    public function model3d(): BelongsTo
    {
        return $this->belongsTo(Model3D::class, 'id_model', 'id_model');
    }

    public function posisi(): BelongsTo
    {
        return $this->belongsTo(PosisiObjek::class, 'id_posisi', 'id_posisi');
    }

    public function karya(): HasMany
    {
        return $this->hasMany(Karya::class, 'id_objek', 'id_objek');
    }
}