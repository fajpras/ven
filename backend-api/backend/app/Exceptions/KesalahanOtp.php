<?php

namespace App\Exceptions;

use Illuminate\Http\JsonResponse;
use RuntimeException;

final class KesalahanOtp extends RuntimeException
{
    private function __construct(string $pesan, private readonly int $kodeStatus)
    {
        parent::__construct($pesan);
    }

    public static function tidakValid(): self
    {
        return new self('Kode verifikasi tidak valid atau sudah kedaluwarsa.', 422);
    }

    public static function terkunci(): self
    {
        return new self('Terlalu banyak percobaan. Minta kode verifikasi baru.', 429);
    }

    public static function terlaluSering(int $sisaDetik): self
    {
        return new self("Tunggu {$sisaDetik} detik sebelum meminta kode verifikasi lagi.", 429);
    }

    public static function melampauiBatasPerJam(): self
    {
        return new self('Batas permintaan kode verifikasi per jam tercapai. Coba lagi nanti.', 429);
    }

    public static function gagalKirim(): self
    {
        return new self('Kode verifikasi gagal dikirim. Coba lagi beberapa saat.', 503);
    }

    public function kodeStatus(): int
    {
        return $this->kodeStatus;
    }

    public function render(): JsonResponse
    {
        return response()->json(['data' => null, 'message' => $this->getMessage()], $this->kodeStatus);
    }
}
