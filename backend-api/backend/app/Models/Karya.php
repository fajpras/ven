<?php

namespace App\Models;

use App\Models\Concerns\HasBinaryUuid;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\SoftDeletes;

class Karya extends Model
{
    use HasBinaryUuid, SoftDeletes;

    protected $table = 'karya';
    protected $primaryKey = 'id_karya';
    public $incrementing = false;
    protected $keyType = 'string';

    protected array $uuidColumns = ['id_karya', 'id_pengguna'];

    protected $fillable = [
        'id_kategori', 'id_pengguna', 'id_pameran', 'id_objek',
        'judul', 'jenis', 'deskripsi', 'file',
    ];

    protected function casts(): array
    {
        return [
            'created_at' => 'datetime',
            'updated_at' => 'datetime',
            'deleted_at' => 'datetime',
        ];
    }

    public function kategori(): BelongsTo
    {
        return $this->belongsTo(Kategori::class, 'id_kategori', 'id_kategori');
    }

    public function pengguna(): BelongsTo
    {
        return $this->belongsTo(Pengguna::class, 'id_pengguna', 'id_pengguna');
    }

    public function pameran(): BelongsTo
    {
        return $this->belongsTo(Pameran::class, 'id_pameran', 'id_pameran');
    }

    public function objek(): BelongsTo
    {
        return $this->belongsTo(Objek::class, 'id_objek', 'id_objek');
    }
}