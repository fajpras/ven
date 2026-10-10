<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class LogKeamanan extends Model
{
    public $timestamps = false;

    protected $table = 'log_keamanan';
    protected $primaryKey = 'id_log';

    protected $fillable = [
        'id_pengguna', 'event', 'endpoint', 'method',
        'status_kode', 'ip_address', 'user_agent', 'pesan',
    ];

    protected function casts(): array
    {
        return [
            'status_kode' => 'integer',
            'waktu'       => 'datetime',
        ];
    }

    protected static function booted(): void
    {
        static::updating(fn () => false);
        static::deleting(fn () => false);
    }

    public function pengguna(): BelongsTo
    {
        return $this->belongsTo(Pengguna::class, 'id_pengguna', 'id_pengguna');
    }
}