<?php

namespace App\Http\Controllers\Pengguna;

use App\Http\Controllers\Controller;
use App\Models\Kategori;
use App\Models\Pengguna;
use App\Enums\TujuanOtp;
use App\Http\Requests\Profil\GantiEmailRequest;
use App\Http\Requests\Profil\KirimOtpGantiEmailRequest;
use App\Services\Otp\LayananOtp;
use Illuminate\Database\UniqueConstraintViolationException;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Validation\Rule;
use Illuminate\Validation\Rules\Password;

class ProfilController extends Controller
{
    public function __construct(private readonly LayananOtp $layananOtp) {}

    public function show(Request $request)
    {
        return $this->ok($request->user());
    }

    public function update(Request $request)
    {
        $pengguna = $request->user();

        $validated = $request->validate([
            'nama' => ['sometimes', 'string', 'max:30'],
            'nama_panggilan' => ['nullable', 'string', 'max:40', 'alpha_dash:ascii',
                Rule::unique('pengguna', 'nama_panggilan')->ignore($pengguna->id_pengguna, 'id_pengguna')],
            'deskripsi' => ['nullable', 'string', 'max:1000'],
            'foto_profil' => ['nullable', 'url:https', 'max:255'],
        ]);

        $pengguna->forceFill($validated)->save();

        return $this->ok($pengguna, 'Profil diperbarui.');
    }

    public function kirimOtpGantiEmail(KirimOtpGantiEmailRequest $request): JsonResponse
    {
        $pengguna = $request->user();

        if ($pengguna->kata_sandi && ! Hash::check((string) $request->validated('kata_sandi'), $pengguna->kata_sandi)) {
            return $this->error('Kata sandi salah.', 422);
        }

        $this->layananOtp->terbitkan(TujuanOtp::GantiEmail, $request->validated('email_baru'), $pengguna->id_pengguna);

        return $this->ok(null, 'Kode verifikasi telah dikirim ke email baru.');
    }

    public function gantiEmail(GantiEmailRequest $request): JsonResponse
    {
        $pengguna = $request->user();
        $data = $request->validated();

        $this->layananOtp->verifikasi(TujuanOtp::GantiEmail, $data['email_baru'], $data['otp'], $pengguna->id_pengguna);

        try {
            $pengguna->forceFill(['email' => $data['email_baru']])->save();
        } catch (UniqueConstraintViolationException) {
            return $this->error('Email sudah digunakan akun lain.', 409);
        }

        return $this->ok($pengguna->fresh(), 'Email diperbarui.');
    }

    public function updatePassword(Request $request)
    {
        $pengguna = $request->user();

        $validated = $request->validate([

            'kata_sandi_saat_ini' => [Rule::requiredIf((bool) $pengguna->kata_sandi), 'nullable', 'string'],
            'kata_sandi' => ['required', 'string', 'max:72', 'different:kata_sandi_saat_ini', Password::min(8)->letters()->numbers()],
        ]);

        if ($pengguna->kata_sandi && ! Hash::check($validated['kata_sandi_saat_ini'], $pengguna->kata_sandi)) {
            return $this->error('Kata sandi saat ini salah.', 422);
        }

        $pengguna->forceFill(['kata_sandi' => $validated['kata_sandi']])->save();
        $pengguna->tokens()->where('id', '!=', $pengguna->currentAccessToken()->id)->delete();

        return $this->ok(null, 'Kata sandi diperbarui.');
    }

    public function updateStatus(Request $request)
    {
        $validated = $request->validate(['status' => ['required', 'in:aktif,nonaktifkan']]);

        $pengguna = $request->user();

        if ($validated['status'] === 'nonaktifkan') {
            $pengguna->forceFill(['status' => 'nonaktifkan'])->save();
            $pengguna->tokens()->delete();

            return $this->ok(null, 'Akun dinonaktifkan. Masuk kembali kapan saja untuk mengaktifkan.');
        }

        return $this->ok($pengguna, 'Akun sudah aktif.');
    }

    public function minat(Request $request)
    {
        return $this->ok($request->user()->kategori()->orderByPivot('bobot', 'desc')->get());
    }

    public function updateMinat(Request $request)
    {
        $validated = $request->validate([
            'kategori' => ['required', 'array', 'min:1', 'max:50'],
            'kategori.*.id_kategori' => ['required', 'integer', 'distinct', 'exists:kategori,id_kategori'],
            'kategori.*.bobot' => ['required', 'numeric', 'min:0', 'max:1'],
        ]);

        $sync = collect($validated['kategori'])
            ->keyBy('id_kategori')
            ->map(fn ($item) => ['bobot' => $item['bobot']])
            ->all();

        $request->user()->kategori()->sync($sync);

        return $this->ok($request->user()->kategori()->orderByPivot('bobot', 'desc')->get(), 'Minat kategori disimpan.');
    }

    public function destroyMinat(Request $request, Kategori $kategori)
    {
        $request->user()->kategori()->detach($kategori->id_kategori);

        return $this->ok(null, 'Minat kategori dihapus.');
    }
}
