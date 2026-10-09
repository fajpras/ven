<?php

namespace App\Models;

use App\Enums\TujuanOtp;
use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class ResetKataSandi extends Model
{
    const UPDATED_AT = null;

    protected $table = 'reset_kata_sandi';
    protected $primaryKey = 'id_reset';

    protected $fillable = ['tujuan', 'email', 'id_pengguna', 'otp_hash', 'expired_at', 'used_at'];

    protected $hidden = ['otp_hash'];

    protected function casts(): array
    {
        return [
            'tujuan' => TujuanOtp::class,
            'percobaan' => 'integer',
            'expired_at' => 'datetime',
            'used_at' => 'datetime',
            'created_at' => 'datetime',
        ];
    }

    public function scopeUntuk(Builder $query, TujuanOtp $tujuan, string $email): Builder
    {
        return $query->where('tujuan', $tujuan->value)->where('email', $email);
    }

    public function scopeBelumDipakai(Builder $query): Builder
    {
        return $query->whereNull('used_at');
    }

    public function scopeBelumKedaluwarsa(Builder $query): Builder
    {
        return $query->where('expired_at', '>', now());
    }

    public function pengguna(): BelongsTo
    {
        return $this->belongsTo(Pengguna::class, 'id_pengguna', 'id_pengguna');
    }
}
