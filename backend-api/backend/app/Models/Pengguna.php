<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Concerns\HasUuids;
use Illuminate\Database\Eloquent\Relations\BelongsToMany;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\SoftDeletes;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Support\Carbon;
use Laravel\Sanctum\HasApiTokens;

class Pengguna extends Authenticatable
{
    use HasApiTokens, HasUuids, SoftDeletes;

    protected $table = 'pengguna';
    protected $primaryKey = 'id_pengguna';
    protected $keyType = 'string';
    public $incrementing = false;

    protected $fillable = [
        'nama', 'nama_panggilan', 'kata_sandi', 'deskripsi', 'google_id',
        'email', 'foto_profil', 'role', 'status',
    ];

    protected $hidden = ['kata_sandi', 'google_id'];

    protected $attributes = ['status' => 'aktif'];

    protected function casts(): array
    {
        return [
            'kata_sandi' => 'hashed',
            'created_at' => 'datetime',
            'updated_at' => 'datetime',
            'deleted_at' => 'datetime',
        ];
    }

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

    public function sanksiAktif(): ?SanksiAkun
    {
        return $this->sanksi()
            ->where('mulai', '<=', now())
            ->where(fn ($q) => $q->whereNull('berakhir')->orWhere('berakhir', '>', now()))
            ->orderByRaw("jenis_sanksi = 'diblokir' desc")
            ->latest('id_sanksi')
            ->first();
    }

    public function sinkronStatusSanksi(): void
    {
        $sanksi = $this->sanksiAktif();

        $status = match ($sanksi?->jenis_sanksi) {
            'diblokir' => 'nonaktifkan',
            'ditanggungkan' => 'ditanggungkan',
            default => 'aktif',
        };

        if ($this->status !== $status) {
            $this->forceFill(['status' => $status])->save();
        }

        if ($sanksi) {
            $this->tokens()->delete();
        }
    }

    public function cekAksesLogin(): ?string
    {
        if ($sanksi = $this->sanksiAktif()) {
            $this->sinkronStatusSanksi();

            $durasi = $sanksi->berakhir
                ? 'sampai '.Carbon::parse($sanksi->berakhir)->translatedFormat('d M Y H:i')
                : 'secara permanen';

            return "Akun Anda dikenai sanksi {$durasi}. Alasan: {$sanksi->alasan}";
        }

        if ($this->status !== 'aktif') {
            $this->forceFill(['status' => 'aktif'])->save();
        }

        return null;
    }
}