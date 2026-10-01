<?php

namespace App\Http\Controllers;

use App\Models\Pengguna;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Mail;

class PasswordResetController extends Controller
{
    public function forgot(Request $request)
    {
        $validated = $request->validate([
            'email' => ['required', 'email', 'max:254'],
        ]);

        $pengguna = Pengguna::where('email', $validated['email'])->first();

        if ($pengguna) {
            $otp = str_pad((string) random_int(0, 999999), 6, '0', STR_PAD_LEFT);

            $pengguna->resetKataSandi()->create([
                'otp_hash' => Hash::make($otp),
                'expired_at' => now()->addMinutes(10),
            ]);

            Mail::raw("Kode OTP reset kata sandi Anda: {$otp} (berlaku 10 menit).", function ($message) use ($pengguna) {
                $message->to($pengguna->email)->subject('Reset Kata Sandi');
            });
        }

        return $this->ok(null, 'Jika email terdaftar, OTP telah dikirim.');
    }

    public function reset(Request $request)
    {
        $validated = $request->validate([
            'email' => ['required', 'email', 'max:254'],
            'otp' => ['required', 'string', 'size:6'],
            'kata_sandi' => ['required', 'string', 'min:8', 'max:72'],
        ]);

        $pengguna = Pengguna::where('email', $validated['email'])->firstOrFail();

        $reset = $pengguna->resetKataSandi()
            ->whereNull('used_at')
            ->where('expired_at', '>', now())
            ->latest('id_reset')
            ->firstOrFail();

        if (! Hash::check($validated['otp'], $reset->otp_hash)) {
            return $this->error('OTP tidak valid.', 422);
        }

        $pengguna->forceFill(['kata_sandi' => $validated['kata_sandi']])->save();
        $reset->forceFill(['used_at' => now()])->save();

        return $this->ok(null, 'Kata sandi berhasil diubah.');
    }
}
