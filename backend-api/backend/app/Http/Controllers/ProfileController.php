<?php

namespace App\Http\Controllers;

use App\Models\Pengguna;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;

class ProfileController extends Controller
{
    public function show(Request $request)
    {
        return $this->ok($request->user());
    }

    public function update(Request $request)
    {
        $validated = $request->validate([
            'nama' => ['sometimes', 'string', 'max:30'],
            'nama_panggilan' => ['nullable', 'string', 'max:40'],
            'deskripsi' => ['nullable', 'string'],
            'foto_profil' => ['nullable', 'string', 'max:255'],
        ]);

        /** @var Pengguna $pengguna */
        $pengguna = $request->user();
        $pengguna->forceFill($validated)->save();

        return $this->ok($pengguna, 'Profil diperbarui.');
    }

    public function updateEmail(Request $request)
    {
        $validated = $request->validate([
            'email' => ['required', 'email', 'max:254', 'unique:pengguna,email'],
        ]);

        /** @var Pengguna $pengguna */
        $pengguna = $request->user();
        $pengguna->forceFill(['email' => $validated['email']])->save();

        return $this->ok($pengguna, 'Email diperbarui.');
    }

    public function updatePassword(Request $request)
    {
        $validated = $request->validate([
            'kata_sandi_saat_ini' => ['required', 'string'],
            'kata_sandi' => ['required', 'string', 'min:8', 'max:72', 'different:kata_sandi_saat_ini'],
        ]);

        /** @var Pengguna $pengguna */
        $pengguna = $request->user();

        if (! Hash::check($validated['kata_sandi_saat_ini'], $pengguna->kata_sandi)) {
            return $this->error('Kata sandi saat ini salah.', 422);
        }

        $pengguna->forceFill(['kata_sandi' => $validated['kata_sandi']])->save();

        return $this->ok(null, 'Kata sandi diperbarui.');
    }

    public function updateStatus(Request $request)
    {
        $validated = $request->validate([
            'status' => ['required', 'string', 'in:aktif,nonaktifkan'],
        ]);

        /** @var Pengguna $pengguna */
        $pengguna = $request->user();
        $pengguna->forceFill(['status' => $validated['status']])->save();

        return $this->ok($pengguna, 'Status akun diperbarui.');
    }
}
