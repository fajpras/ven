<?php

namespace App\Enums;

enum TujuanOtp: string
{
    case Registrasi = 'registrasi';
    case GantiEmail = 'ganti_email';
    case ResetKataSandi = 'reset_kata_sandi';

    public function masaBerlakuMenit(): int
    {
        return (int) config("otp.tujuan.{$this->value}.berlaku");
    }

    public function subjekEmail(): string
    {
        return (string) config("otp.tujuan.{$this->value}.subjek");
    }

    public function peruntukan(): string
    {
        return (string) config("otp.tujuan.{$this->value}.peruntukan");
    }

    public function panjangKode(): int
    {
        return (int) config('otp.panjang');
    }

    public function jedaKirimUlangDetik(): int
    {
        return (int) config('otp.jeda_kirim_ulang');
    }

    public function maksimumPercobaan(): int
    {
        return (int) config('otp.maksimum_percobaan');
    }

    public function maksimumPermintaanPerJam(): int
    {
        return (int) config('otp.maksimum_permintaan_per_jam');
    }

    public function memerlukanPengguna(): bool
    {
        return $this !== self::Registrasi;
    }
}
