<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class LogAktifitas extends Model
{
    public $timestamps = false;

    protected $table = 'log_aktifitas';
    protected $primaryKey = 'id_log';

    protected $fillable = [
        'id_pengguna', 'entitas', 'id_entitas', 'status', 'pesan', 'ip_address',
    ];

    protected function casts(): array
    {
        return ['waktu' => 'datetime'];
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