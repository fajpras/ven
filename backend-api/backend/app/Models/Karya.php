<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Casts\Attribute;
use Illuminate\Database\Eloquent\Concerns\HasUuids;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\SoftDeletes;
use Illuminate\Support\Facades\URL;

class Karya extends Model
{
    use HasUuids, SoftDeletes;

    protected $table = 'karya';
    protected $primaryKey = 'id_karya';
    protected $keyType = 'string';
    public $incrementing = false;

    protected $fillable = [
        'id_kategori', 'id_pengguna', 'id_pameran', 'id_objek',
        'judul', 'jenis', 'deskripsi', 'file',
    ];

    protected $hidden = ['file'];

    protected $appends = ['file_url'];

    protected function casts(): array
    {
        return [
            'created_at' => 'datetime',
            'updated_at' => 'datetime',
            'deleted_at' => 'datetime',
        ];
    }

    protected function fileUrl(): Attribute
    {
        return Attribute::get(fn () => $this->file && $this->exists
            ? URL::temporarySignedRoute('karya.berkas', now()->addMinutes(30), ['karya' => $this->id_karya])
            : null);
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