<?php

namespace App\Models;

use App\Models\Concerns\HasBinaryUuid;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class ResetKataSandi extends Model
{
    use HasBinaryUuid;

    const UPDATED_AT = null;

    protected $table = 'reset_kata_sandi';
    protected $primaryKey = 'id_reset';

    protected array $uuidColumns = ['id_pengguna'];

    protected $fillable = ['id_pengguna', 'otp_hash', 'expired_at', 'used_at'];

    protected $hidden = ['otp_hash'];

    protected function casts(): array
    {
        return [
            'expired_at' => 'datetime',
            'used_at'    => 'datetime',
            'created_at' => 'datetime',
        ];
    }

    public function pengguna(): BelongsTo
    {
        return $this->belongsTo(Pengguna::class, 'id_pengguna', 'id_pengguna');
    }
}