<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Ruangan extends Model
{
    public $timestamps = false;

    protected $table = 'ruangan';
    protected $primaryKey = 'id_ruangan';

    protected $fillable = ['id_pameran', 'jenis'];

    public function pameran(): BelongsTo
    {
        return $this->belongsTo(Pameran::class, 'id_pameran', 'id_pameran');
    }

    public function undangan(): HasMany
    {
        return $this->hasMany(Undangan::class, 'id_ruangan', 'id_ruangan');
    }
}