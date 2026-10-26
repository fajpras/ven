<?php

namespace App\Services;

use App\Models\LogAktivitas;
use App\Models\LogKeamanan;
use App\Models\Pengguna;
use Throwable;

class LogSistem
{
    private static function koneksi(): string
    {
        return config('database.log_connection', 'mysql');
    }

    public static function aktivitas(
        ?string $entitas = null,
        ?string $jenis = null,
        string $pesan = '',
        string $status = 'berhasil',
        ?string $idPengguna = null,
        ?string $aksi = null
    ): void {
        try {
            $idAktor = $idPengguna ?? self::aktorId();
            $namaAktor = $entitas ?: self::namaAktor($idAktor);

            LogAktivitas::on(self::koneksi())->create([
                'id_pengguna' => $idAktor,
                'entitas'     => mb_substr($namaAktor, 0, 100),
                'jenis'       => $jenis ? mb_substr($jenis, 0, 50) : null,
                'aksi'        => $aksi ? mb_substr($aksi, 0, 20) : null,
                'status'      => $status,
                'pesan'       => mb_substr($pesan, 0, 255),
                'ip_address'  => request()?->ip() ?? '127.0.0.1',
            ]);
        } catch (Throwable $e) {
            report($e);
        }
    }

    public static function keamanan(
        string $event,
        ?string $pesan = null,
        ?int $statusKode = null,
        ?string $idPengguna = null
    ): void {
        try {
            $req = request();
            $status = $statusKode ?? 200;
            $keterangan = $pesan ?: self::defaultPesanKeamanan($event, $status);

            LogKeamanan::on(self::koneksi())->create([
                'id_pengguna' => $idPengguna ?? self::aktorId(),
                'event'       => $event,
                'endpoint'    => mb_substr('/'.ltrim((string) $req?->path(), '/'), 0, 100),
                'method'      => $req?->method() ?? 'CLI',
                'status_kode' => $status,
                'ip_address'  => $req?->ip() ?? '127.0.0.1',
                'user_agent'  => mb_substr((string) ($req?->userAgent() ?: 'CLI/Internal'), 0, 255),
                'pesan'       => mb_substr($keterangan, 0, 255),
            ]);
        } catch (Throwable $e) {
            report($e);
        }
    }

    public static function lihat(string $jenis, string $namaAtauId, ?string $detail = null): void
    {
        self::aktivitas(
            entitas: null,
            jenis: $jenis,
            pesan: "Melihat {$jenis} \"{$namaAtauId}\"".($detail ? " ({$detail})" : ''),
            status: 'berhasil',
            aksi: 'lihat',
        );
    }

    private static function defaultPesanKeamanan(string $event, int $status): string
    {
        return match ($event) {
            'login_berhasil'          => 'Pengguna berhasil login ke dalam sistem',
            'login_gagal'             => 'Percobaan login gagal karena identitas atau kata sandi tidak valid',
            'login_ditolak'           => 'Login ditolak oleh sistem karena akun memiliki sanksi aktif',
            'logout'                  => 'Pengguna keluar (logout) dari sistem',
            'reset_kata_sandi'        => 'Pengguna berhasil mereset kata sandi akun',
            'google_token_tidak_valid'=> 'Otentikasi Google OAuth gagal karena token tidak valid',
            'tidak_terautentikasi'    => 'Percobaan akses endpoint terproteksi tanpa otentikasi valid',
            'akses_ditolak'           => 'Akses ditolak karena pengguna tidak memiliki izin (hak akses)',
            'terlalu_banyak_permintaan'=> 'Permintaan dibatasi karena melebihi batas laju (rate limit)',
            'otp_terbit'              => 'Kode OTP baru berhasil diterbitkan',
            'otp_terverifikasi'       => 'Kode OTP berhasil divalidasi dan terverifikasi',
            'otp_gagal'               => 'Verifikasi kode OTP gagal',
            default                   => "Peristiwa keamanan {$event} tercatat (kode HTTP: {$status})",
        };
    }

    private static function aktorId(): ?string
    {
        try {
            return request()?->user('sanctum')?->id_pengguna;
        } catch (Throwable) {
            return null;
        }
    }

    private static function namaAktor(?string $idAktor): string
    {
        try {
            if ($user = request()?->user('sanctum')) {
                return (string) $user->nama;
            }

            if ($idAktor && ($pengguna = Pengguna::find($idAktor))) {
                return (string) $pengguna->nama;
            }
        } catch (Throwable) {
        }

        return request() ? 'Tamu' : 'Sistem';
    }
}
