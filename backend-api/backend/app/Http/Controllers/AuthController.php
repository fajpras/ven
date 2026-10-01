<?php

namespace App\Http\Controllers;

use App\Models\Pengguna;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Laravel\Socialite\Facades\Socialite;

class AuthController extends Controller
{
    public function register(Request $request)
    {
        $validated = $request->validate([
            'nama' => ['required', 'string', 'max:30'],
            'nama_panggilan' => ['nullable', 'string', 'max:40'],
            'email' => ['required', 'email', 'max:254', 'unique:pengguna,email'],
            'kata_sandi' => ['required', 'string', 'min:8', 'max:72'],
        ]);

        $pengguna = Pengguna::create($validated);

        return $this->created($this->withToken($pengguna), 'Registrasi berhasil.');
    }

    public function login(Request $request)
    {
        $validated = $request->validate([
            'email' => ['required', 'email'],
            'kata_sandi' => ['required', 'string'],
        ]);

        $pengguna = Pengguna::where('email', $validated['email'])->first();

        if (! $pengguna
            || ! $pengguna->kata_sandi
            || ! Hash::check($validated['kata_sandi'], $pengguna->kata_sandi)) {
            return $this->error('Email atau kata sandi salah.', 401);
        }

        if ($pengguna->status !== 'aktif') {
            return $this->error('Akun Anda tidak aktif. Hubungi admin.', 403);
        }

        return $this->ok($this->withToken($pengguna), 'Login berhasil.');
    }

    public function googleRedirect()
    {
        return $this->ok([
            'url' => Socialite::driver('google')->stateless()->redirect()->getTargetUrl(),
        ]);
    }

    public function googleCallback(Request $request)
    {
        $google = Socialite::driver('google')->stateless()->user();

        return $this->ok($this->withToken($this->loginOrCreateGoogle($google)), 'Login Google berhasil.');
    }

    public function registerGoogle(Request $request)
    {
        $validated = $request->validate([
            'id_token' => ['required', 'string'],
        ]);

        $google = Socialite::driver('google')->stateless()->userFromToken($validated['id_token']);

        return $this->ok($this->withToken($this->loginOrCreateGoogle($google)), 'Registrasi Google berhasil.');
    }

    public function loginGoogle(Request $request)
    {
        $validated = $request->validate([
            'id_token' => ['required', 'string'],
        ]);

        $google = Socialite::driver('google')->stateless()->userFromToken($validated['id_token']);

        return $this->ok($this->withToken($this->loginOrCreateGoogle($google)), 'Login Google berhasil.');
    }

    public function logout(Request $request)
    {
        $request->user()->currentAccessToken()->delete();

        return $this->ok(null, 'Logout berhasil.');
    }

    private function loginOrCreateGoogle($google): Pengguna
    {
        $pengguna = Pengguna::where('google_id', $google->getId())
            ->orWhere('email', $google->getEmail())
            ->first();

        if (! $pengguna) {
            $pengguna = Pengguna::create([
                'nama' => mb_substr($google->getName() ?? $google->getNickname() ?? 'Pengguna', 0, 30),
                'email' => $google->getEmail(),
                'google_id' => $google->getId(),
                'foto_profil' => $google->getAvatar(),
            ]);
        } elseif (! $pengguna->google_id) {
            $pengguna->forceFill([
                'google_id' => $google->getId(),
                'foto_profil' => $pengguna->foto_profil ?: $google->getAvatar(),
            ])->save();
        }

        if ($pengguna->status !== 'aktif') {
            abort(403, 'Akun Anda tidak aktif. Hubungi admin.');
        }

        return $pengguna;
    }

    private function withToken(Pengguna $pengguna): array
    {
        return [
            'pengguna' => $pengguna,
            'token' => $pengguna->createToken('auth')->plainTextToken,
        ];
    }
}
