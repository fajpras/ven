<?php

namespace App\Models;

use App\Models\Concerns\HasBinaryUuid;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\SoftDeletes;

class Pameran extends Model
{
    use HasBinaryUuid, SoftDeletes;

    public $timestamps = false;

    protected $table = 'pameran';
    protected $primaryKey = 'id_pameran';

    protected array $uuidColumns = ['id_pengguna'];

    protected $fillable = ['id_pengguna', 'id_model', 'judul', 'banner', 'tipe'];

    protected function casts(): array
    {
        return ['deleted_at' => 'datetime'];
    }

    public function pengguna(): BelongsTo
    {
        return $this->belongsTo(Pengguna::class, 'id_pengguna', 'id_pengguna');
    }

    public function model3d(): BelongsTo
    {
        return $this->belongsTo(Model3D::class, 'id_model', 'id_model');
    }

    public function ruangan(): HasMany
    {
        return $this->hasMany(Ruangan::class, 'id_pameran', 'id_pameran');
    }

    public function karya(): HasMany
    {
        return $this->hasMany(Karya::class, 'id_pameran', 'id_pameran');
    }
}