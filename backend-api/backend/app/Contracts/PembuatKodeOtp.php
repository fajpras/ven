<?php

namespace App\Contracts;

interface PembuatKodeOtp
{
    public function buat(int $panjang): string;
}
