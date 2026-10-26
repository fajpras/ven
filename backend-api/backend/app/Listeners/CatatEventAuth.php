<?php

namespace App\Listeners;

use App\Services\LogSistem;
use Illuminate\Auth\Events\Failed;
use Illuminate\Auth\Events\Login;
use Illuminate\Auth\Events\Logout;
use Illuminate\Auth\Events\PasswordReset;
use Illuminate\Events\Dispatcher;

class CatatEventAuth
{
    public function login(Login $e): void
    {
        LogSistem::keamanan('login_berhasil', "Pengguna {$e->user->nama} ({$e->user->email}) berhasil login", 200, $e->user->id_pengguna);
    }

    public function gagal(Failed $e): void
    {
        $alasan = $e->user ? 'kata sandi salah' : 'identitas akun tidak ditemukan';
        LogSistem::keamanan('login_gagal', "Percobaan login gagal: {$alasan}", 401, $e->user?->id_pengguna);
    }

    public function logout(Logout $e): void
    {
        $nama = $e->user?->nama ?? 'Pengguna';
        LogSistem::keamanan('logout', "{$nama} keluar (logout) dari sistem", 200, $e->user?->id_pengguna);
    }

    public function resetSandi(PasswordReset $e): void
    {
        LogSistem::keamanan('reset_kata_sandi', "Kata sandi akun {$e->user->email} berhasil direset", 200, $e->user->id_pengguna);
    }

    public function subscribe(Dispatcher $events): array
    {
        return [
            Login::class => 'login',
            Failed::class => 'gagal',
            Logout::class => 'logout',
            PasswordReset::class => 'resetSandi',
        ];
    }
}
