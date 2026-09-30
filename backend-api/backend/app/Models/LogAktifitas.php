<?php

namespace App\Models;

use App\Models\Concerns\HasBinaryUuid;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

/** Log append-only: user DB hanya punya hak SELECT + INSERT. */
class LogAktifitas extends Model
{
    use HasBinaryUuid;

    public $timestamps = false;

    protected $table = 'log_aktifitas';
    protected $primaryKey = 'id_log';

    protected array $uuidColumns = ['id_pengguna', 'id_entitas'];

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