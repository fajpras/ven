<?php

namespace App\Http\Controllers;

use App\Enums\TujuanOtp;
use App\Exceptions\KesalahanOtp;
use App\Http\Requests\Auth\KirimOtpRegistrasiRequest;
use App\Http\Requests\Auth\LupaKataSandiRequest;
use App\Http\Requests\Auth\RegistrasiRequest;
use App\Http\Requests\Auth\ResetKataSandiRequest;
use App\Models\Pengguna;
use App\Services\LogSistem;
use App\Services\Otp\LayananOtp;
use Illuminate\Auth\Events\Failed;
use Illuminate\Auth\Events\Login;
use Illuminate\Auth\Events\Logout;
use Illuminate\Auth\Events\PasswordReset;
use Illuminate\Database\UniqueConstraintViolationException;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Arr;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Str;
use Laravel\Socialite\Facades\Socialite;
use Throwable;

class AuthController extends Controller
{
    public function __construct(private readonly LayananOtp $layananOtp) {}

    public function kirimOtpRegistrasi(KirimOtpRegistrasiRequest $request): JsonResponse
    {
        $email = $request->validated('email');

        if (! Pengguna::where('email', $email)->exists()) {
            $this->layananOtp->terbitkan(TujuanOtp::Registrasi, $email);
        }

        return $this->ok(null, 'Jika email dapat didaftarkan, kode verifikasi telah dikirim.');
    }

    public function register(RegistrasiRequest $request): JsonResponse
    {
        $data = $request->validated();

        $this->layananOtp->verifikasi(TujuanOtp::Registrasi, $data['email'], $data['otp']);

        try {
            $pengguna = Pengguna::create(Arr::except($data, ['otp']));
        } catch (UniqueConstraintViolationException) {
            return $this->error('Email atau nama panggilan sudah terdaftar.', 409);
        }

        return $this->created($this->withToken($pengguna), 'Registrasi berhasil.');
    }

    public function login(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'identitas' => ['required', 'string', 'max:254'],
            'kata_sandi' => ['required', 'string', 'max:72'],
        ]);

        $identitas = trim($validated['identitas']);
        $pengguna = str_contains($identitas, '@')
            ? Pengguna::where('email', Str::lower($identitas))->first()
            : Pengguna::where('nama_panggilan', $identitas)->first();

        if (! $pengguna || ! $pengguna->kata_sandi || ! Hash::check($validated['kata_sandi'], $pengguna->kata_sandi)) {
            if (! $pengguna) {
                Hash::make($validated['kata_sandi']);
            }

            event(new Failed('sanctum', $pengguna, ['identitas' => '[disamarkan]']));

            return $this->error('Identitas atau kata sandi salah.', 401);
        }

        return $this->masuk($pengguna, 'Login berhasil.');
    }

    public function logout(Request $request): JsonResponse
    {
        $pengguna = $request->user();
        $pengguna->currentAccessToken()->delete();

        event(new Logout('sanctum', $pengguna));

        return $this->ok(null, 'Logout berhasil.');
    }

    public function registerGoogle(Request $request): JsonResponse
    {
        $google = $this->profilGoogle($request);

        if ($google instanceof JsonResponse) {
            return $google;
        }

        $sudahAda = Pengguna::where('google_id', $google['sub'])->orWhere('email', $google['email'])->exists();

        if ($sudahAda) {
            return $this->error('Akun sudah terdaftar, silakan masuk.', 409);
        }

        $pengguna = Pengguna::create([
            'nama' => mb_substr($google['name'] ?: 'Pengguna', 0, 30),
            'email' => $google['email'],
            'google_id' => $google['sub'],
            'foto_profil' => $google['picture'],
        ]);

        event(new Login('sanctum', $pengguna, false));

        return $this->created($this->withToken($pengguna), 'Registrasi Google berhasil.');
    }

    public function loginGoogle(Request $request): JsonResponse
    {
        $google = $this->profilGoogle($request);

        if ($google instanceof JsonResponse) {
            return $google;
        }

        $pengguna = Pengguna::where('google_id', $google['sub'])->first()
            ?? Pengguna::where('email', $google['email'])->first();

        if (! $pengguna) {
            return $this->notFound('Akun belum terdaftar, silakan daftar terlebih dahulu.');
        }

        if (! $pengguna->google_id) {
            $pengguna->forceFill([
                'google_id' => $google['sub'],
                'foto_profil' => $pengguna->foto_profil ?: $google['picture'],
            ])->save();
        }

        return $this->masuk($pengguna, 'Login Google berhasil.');
    }

    public function lupaKataSandi(LupaKataSandiRequest $request): JsonResponse
    {
        $email = $request->validated('email');
        $pengguna = Pengguna::where('email', $email)->first();

        if ($pengguna) {
            $this->layananOtp->terbitkan(TujuanOtp::ResetKataSandi, $email, $pengguna->id_pengguna);
        }

        return $this->ok(null, 'Jika email terdaftar, kode verifikasi telah dikirim.');
    }

    public function resetKataSandi(ResetKataSandiRequest $request): JsonResponse
    {
        $data = $request->validated();
        $pengguna = Pengguna::where('email', $data['email'])->first();

        if (! $pengguna) {
            throw KesalahanOtp::tidakValid();
        }

        $this->layananOtp->verifikasi(TujuanOtp::ResetKataSandi, $data['email'], $data['otp'], $pengguna->id_pengguna);

        $pengguna->forceFill(['kata_sandi' => $data['kata_sandi']])->save();
        $pengguna->tokens()->delete();

        event(new PasswordReset($pengguna));

        return $this->ok(null, 'Kata sandi berhasil diubah. Silakan login kembali.');
    }

    private function masuk(Pengguna $pengguna, string $pesan): JsonResponse
    {
        if ($alasan = $pengguna->cekAksesLogin()) {
            LogSistem::keamanan('login_ditolak', 'akun tersanksi', 403, $pengguna->id_pengguna);

            return $this->forbidden($alasan);
        }

        event(new Login('sanctum', $pengguna, false));

        return $this->ok($this->withToken($pengguna), $pesan);
    }

    private function withToken(Pengguna $pengguna): array
    {
        return [
            'pengguna' => $pengguna->fresh(),
            'token' => $pengguna->createToken('mobile', ['*'], now()->addDays(30))->plainTextToken,
        ];
    }

    private function profilGoogle(Request $request): array|JsonResponse
    {
        $validated = $request->validate([
            'access_token' => ['required_without:id_token', 'nullable', 'string', 'max:4096'],
            'id_token' => ['required_without:access_token', 'nullable', 'string', 'max:4096'],
        ]);

        $idKlienDiizinkan = array_filter((array) config('services.google.client_ids'));

        try {
            $profil = ! empty($validated['access_token'])
                ? $this->profilDariAccessToken($validated['access_token'], $idKlienDiizinkan)
                : $this->profilDariIdToken($validated['id_token'], $idKlienDiizinkan);
        } catch (Throwable) {
            $profil = null;
        }

        if (! $profil || ! $profil['verified'] || $profil['sub'] === '' || $profil['email'] === '') {
            LogSistem::keamanan('google_token_tidak_valid', null, 401);

            return $this->unauthorized('Token Google tidak valid.');
        }

        $profil['email'] = Str::lower($profil['email']);

        return $profil;
    }

    private function profilDariAccessToken(string $token, array $idKlienDiizinkan): ?array
    {
        $info = Http::timeout(10)->get('https://oauth2.googleapis.com/tokeninfo', ['access_token' => $token])->json() ?? [];

        if (! in_array($info['aud'] ?? null, $idKlienDiizinkan, true)) {
            return null;
        }

        $pengguna = Socialite::driver('google')->stateless()->userFromToken($token);

        return [
            'sub' => (string) $pengguna->getId(),
            'email' => (string) $pengguna->getEmail(),
            'name' => $pengguna->getName(),
            'picture' => $pengguna->getAvatar(),
            'verified' => filter_var($pengguna->user['email_verified'] ?? false, FILTER_VALIDATE_BOOLEAN),
        ];
    }

    private function profilDariIdToken(string $token, array $idKlienDiizinkan): ?array
    {
        $respons = Http::timeout(10)->get('https://oauth2.googleapis.com/tokeninfo', ['id_token' => $token]);
        $info = $respons->json() ?? [];

        $valid = $respons->successful()
            && in_array($info['aud'] ?? null, $idKlienDiizinkan, true)
            && in_array($info['iss'] ?? '', ['accounts.google.com', 'https://accounts.google.com'], true);

        return $valid ? [
            'sub' => (string) ($info['sub'] ?? ''),
            'email' => (string) ($info['email'] ?? ''),
            'name' => $info['name'] ?? null,
            'picture' => $info['picture'] ?? null,
            'verified' => filter_var($info['email_verified'] ?? false, FILTER_VALIDATE_BOOLEAN),
        ] : null;
    }
}
