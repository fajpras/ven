<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class SanksiAkun extends Model
{
    const UPDATED_AT = null;

    protected $table = 'sanksi_akun';
    protected $primaryKey = 'id_sanksi';

    protected $fillable = ['id_pengguna', 'jenis_sanksi', 'alasan', 'mulai', 'berakhir'];

    protected function casts(): array
    {
        return [
            'mulai'      => 'datetime',
            'berakhir'   => 'datetime',
            'created_at' => 'datetime',
        ];
    }

    public function pengguna(): BelongsTo
    {
        return $this->belongsTo(Pengguna::class, 'id_pengguna', 'id_pengguna');
    }
}