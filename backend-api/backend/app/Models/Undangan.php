<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class Undangan extends Model
{
    public $timestamps = false;

    protected $table = 'undangan';
    protected $primaryKey = 'id_undangan';

    protected $fillable = ['id_pengguna', 'id_ruangan', 'status', 'waktu', 'catatan', 'tautan'];

    protected function casts(): array
    {
        return [
            'waktu'     => 'datetime',
            'timestamp' => 'datetime',
        ];
    }

    public function pengguna(): BelongsTo
    {
        return $this->belongsTo(Pengguna::class, 'id_pengguna', 'id_pengguna');
    }

    public function ruangan(): BelongsTo
    {
        return $this->belongsTo(Ruangan::class, 'id_ruangan', 'id_ruangan');
    }
}