<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;

class LogAktivitas extends Model
{
    public const JENIS = [
        'pameran', 'karya', 'model', 'profil', 'pengguna',
        'ruangan', 'undangan', 'kategori', 'objek', 'sanksi',
    ];

    public const AKSI = ['tambah', 'lihat', 'ubah', 'edit', 'hapus', 'pulih'];

    public $timestamps = false;

    protected $table = 'log_aktivitas';

    protected $primaryKey = 'id_log';

    protected $fillable = [
        'id_pengguna', 'entitas', 'jenis', 'aksi', 'status', 'pesan', 'ip_address',
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
