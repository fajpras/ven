<?php

namespace App\Models;

use App\Models\Concerns\HasBinaryUuid;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Laravel\Sanctum\HasApiTokens;

class Pengguna extends Authenticatable
{
    use HasApiTokens, HasBinaryUuid;

    protected $table = 'pengguna';
    protected $primaryKey = 'id_pengguna';
    public $incrementing = false;
    protected $keyType = 'string';

    protected array $uuidColumns = ['id_pengguna'];

    protected $fillable = [
        'nama', 'nama_panggilan', 'kata_sandi', 'deskripsi', 'google_id',
        'email', 'foto_profil', 'role', 'status',
    ];

    protected $hidden = ['kata_sandi', 'google_id'];

    protected function casts(): array
    {
        return [
            'kata_sandi' => 'hashed',
            'created_at' => 'datetime',
            'updated_at' => 'datetime',
        ];
    }

    /** Kolom password bukan "password". */
    public function getAuthPasswordName(): string
    {
        return 'kata_sandi';
    }

    public function isAdmin(): bool
    {
        return $this->role === 'admin';
    }

    public function resetKataSandi(): HasMany
    {
        return $this->hasMany(ResetKataSandi::class, 'id_pengguna', 'id_pengguna');
    }

    public function sanksi(): HasMany
    {
        return $this->hasMany(SanksiAkun::class, 'id_pengguna', 'id_pengguna');
    }

    public function kategori(): BelongsToMany
    {
        return $this->belongsToMany(Kategori::class, 'pengguna_kategori', 'id_pengguna', 'id_kategori')
            ->using(PenggunaKategori::class)
            ->withPivot('bobot');
    }

    public function pameran(): HasMany
    {
        return $this->hasMany(Pameran::class, 'id_pengguna', 'id_pengguna');
    }

    public function karya(): HasMany
    {
        return $this->hasMany(Karya::class, 'id_pengguna', 'id_pengguna');
    }

    public function undangan(): HasMany
    {
        return $this->hasMany(Undangan::class, 'id_pengguna', 'id_pengguna');
    }

    public function logAktifitas(): HasMany
    {
        return $this->hasMany(LogAktifitas::class, 'id_pengguna', 'id_pengguna');
    }

    public function logKeamanan(): HasMany
    {
        return $this->hasMany(LogKeamanan::class, 'id_pengguna', 'id_pengguna');
    }
}