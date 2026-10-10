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
        LogSistem::keamanan('login_berhasil', null, 200, $e->user->id_pengguna);
    }

    public function gagal(Failed $e): void
    {
        LogSistem::keamanan('login_gagal', $e->user ? 'kata sandi salah' : 'identitas tidak dikenal', 401, $e->user?->id_pengguna);
    }

    public function logout(Logout $e): void
    {
        LogSistem::keamanan('logout', null, 200, $e->user?->id_pengguna);
    }

    public function resetSandi(PasswordReset $e): void
    {
        LogSistem::keamanan('reset_kata_sandi', null, 200, $e->user->id_pengguna);
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
