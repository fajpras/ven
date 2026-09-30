<?php

namespace App\Models;

use App\Models\Concerns\HasBinaryUuid;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

/** Log append-only: user DB hanya punya hak SELECT + INSERT. */
class LogKeamanan extends Model
{
    use HasBinaryUuid;

    public $timestamps = false;

    protected $table = 'log_keamanan';
    protected $primaryKey = 'id_log';

    protected array $uuidColumns = ['id_pengguna'];

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