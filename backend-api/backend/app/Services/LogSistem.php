<?php

namespace App\Services;

use App\Models\LogAktifitas;
use App\Models\LogKeamanan;
use Illuminate\Support\Str;
use Throwable;

class LogSistem
{
    private static function koneksi(): string
    {
        return config('database.log_connection', 'mysql');
    }

    public static function aktivitas(string $entitas, mixed $idEntitas, string $pesan, string $status = 'berhasil', ?string $idPengguna = null): void
    {
        try {
            LogAktifitas::on(self::koneksi())->create([
                'id_pengguna' => $idPengguna ?? self::aktor(),
                'entitas' => $entitas,

                'id_entitas' => is_string($idEntitas) && Str::isUuid($idEntitas) ? $idEntitas : null,
                'status' => $status,
                'pesan' => mb_substr($pesan, 0, 255),
                'ip_address' => request()?->ip(),
            ]);
        } catch (Throwable $e) {
            report($e);
        }
    }

    public static function keamanan(string $event, ?string $pesan = null, ?int $statusKode = null, ?string $idPengguna = null): void
    {
        try {
            $req = request();

            LogKeamanan::on(self::koneksi())->create([
                'id_pengguna' => $idPengguna ?? self::aktor(),
                'event' => $event,
                'endpoint' => mb_substr('/'.ltrim((string) $req?->path(), '/'), 0, 100),
                'method' => $req?->method() ?? 'CLI',
                'status_kode' => $statusKode ?? 0,
                'ip_address' => $req?->ip() ?? '0.0.0.0',
                'user_agent' => mb_substr((string) $req?->userAgent(), 0, 255) ?: '-',
                'pesan' => $pesan ? mb_substr($pesan, 0, 255) : null,
            ]);
        } catch (Throwable $e) {
            report($e);
        }
    }

    private static function aktor(): ?string
    {
        try {
            return request()?->user('sanctum')?->id_pengguna;
        } catch (Throwable) {
            return null;
        }
    }
}
