<?php

namespace App\Services\Otp;

use App\Contracts\PembuatKodeOtp;

final class PembuatKodeAcak implements PembuatKodeOtp
{
    public function buat(int $panjang): string
    {
        $nilaiMaksimum = (10 ** $panjang) - 1;

        return str_pad((string) random_int(0, $nilaiMaksimum), $panjang, '0', STR_PAD_LEFT);
    }
}
