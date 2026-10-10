<?php

namespace App\Services\Otp;

use App\Contracts\PembuatKodeOtp;
use App\Enums\HasilVerifikasiOtp;
use App\Enums\TujuanOtp;
use App\Exceptions\KesalahanOtp;
use App\Mail\KodeOtpMail;
use App\Models\ResetKataSandi;
use App\Services\LogSistem;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Mail;
use Throwable;

final class LayananOtp
{
    public function __construct(
        private readonly PembuatKodeOtp $pembuatKode,
        private readonly string $kunciPenyidik,
    ) {}

    public function terbitkan(TujuanOtp $tujuan, string $email, ?string $idPengguna = null): void
    {
        $this->pastikanBolehMeminta($tujuan, $email);

        $kode = $this->pembuatKode->buat($tujuan->panjangKode());

        $otp = DB::transaction(function () use ($tujuan, $email, $idPengguna, $kode): ResetKataSandi {
            ResetKataSandi::query()->untuk($tujuan, $email)->belumDipakai()->update(['used_at' => now()]);

            return ResetKataSandi::query()->create([
                'tujuan' => $tujuan,
                'email' => $email,
                'id_pengguna' => $tujuan->memerlukanPengguna() ? $idPengguna : null,
                'otp_hash' => $this->buatSidik($tujuan, $email, $kode),
                'expired_at' => now()->addMinutes($tujuan->masaBerlakuMenit()),
            ]);
        });

        $this->kirimEmail($otp, $tujuan, $email, $kode);

        if (app()->environment('local')) {
            \Illuminate\Support\Facades\Log::info("Kode OTP [{$tujuan->value}] untuk {$email}: {$kode}");
        }

        LogSistem::keamanan('otp_terbit', $tujuan->value, 200, $idPengguna);
    }

    public function verifikasi(TujuanOtp $tujuan, string $email, string $kode, ?string $idPengguna = null): ResetKataSandi
    {
        [$hasil, $otp] = $this->periksa($tujuan, $email, $kode, $idPengguna);

        if ($hasil === HasilVerifikasiOtp::Berhasil) {
            LogSistem::keamanan('otp_terverifikasi', $tujuan->value, 200, $idPengguna);

            return $otp;
        }

        LogSistem::keamanan(
            $hasil === HasilVerifikasiOtp::Terkunci ? 'otp_terkunci' : 'otp_gagal',
            $tujuan->value,
            $hasil === HasilVerifikasiOtp::Terkunci ? 429 : 422,
            $idPengguna,
        );

        throw match ($hasil) {
            HasilVerifikasiOtp::Terkunci => KesalahanOtp::terkunci(),
            default => KesalahanOtp::tidakValid(),
        };
    }

    private function periksa(TujuanOtp $tujuan, string $email, string $kode, ?string $idPengguna): array
    {
        $otp = ResetKataSandi::query()
            ->untuk($tujuan, $email)
            ->belumDipakai()
            ->belumKedaluwarsa()
            ->when($idPengguna !== null, fn ($query) => $query->where('id_pengguna', $idPengguna))
            ->latest('id_reset')
            ->first();

        if ($otp === null) {
            return [HasilVerifikasiOtp::TidakDitemukan, null];
        }

        $percobaanDicatat = ResetKataSandi::query()
            ->whereKey($otp->getKey())
            ->whereNull('used_at')
            ->where('percobaan', '<', $tujuan->maksimumPercobaan())
            ->increment('percobaan');

        if ($percobaanDicatat === 0) {
            return [HasilVerifikasiOtp::Terkunci, $otp];
        }

        if (! hash_equals($otp->otp_hash, $this->buatSidik($tujuan, $email, $kode))) {
            return [HasilVerifikasiOtp::KodeSalah, $otp];
        }

        $dikonsumsi = $this->tandaiTerpakai($otp);

        return [$dikonsumsi === 1 ? HasilVerifikasiOtp::Berhasil : HasilVerifikasiOtp::TidakDitemukan, $otp];
    }

    private function pastikanBolehMeminta(TujuanOtp $tujuan, string $email): void
    {
        $terakhir = ResetKataSandi::query()->untuk($tujuan, $email)->latest('id_reset')->first();

        if ($terakhir !== null) {
            $bolehLagiPada = $terakhir->created_at->copy()->addSeconds($tujuan->jedaKirimUlangDetik());

            if ($bolehLagiPada->isFuture()) {
                throw KesalahanOtp::terlaluSering(max(1, $bolehLagiPada->getTimestamp() - now()->getTimestamp()));
            }
        }

        $jumlahSejamTerakhir = ResetKataSandi::query()
            ->untuk($tujuan, $email)
            ->where('created_at', '>=', now()->subHour())
            ->count();

        if ($jumlahSejamTerakhir >= $tujuan->maksimumPermintaanPerJam()) {
            throw KesalahanOtp::melampauiBatasPerJam();
        }
    }

    private function kirimEmail(ResetKataSandi $otp, TujuanOtp $tujuan, string $email, string $kode): void
    {
        try {
            Mail::to($email)->send(new KodeOtpMail($tujuan, $kode, $tujuan->masaBerlakuMenit()));
        } catch (Throwable $kesalahan) {
            report($kesalahan);
            $this->tandaiTerpakai($otp);

            throw KesalahanOtp::gagalKirim();
        }
    }

    private function tandaiTerpakai(ResetKataSandi $otp): int
    {
        return ResetKataSandi::query()
            ->whereKey($otp->getKey())
            ->whereNull('used_at')
            ->update(['used_at' => now()]);
    }

    private function buatSidik(TujuanOtp $tujuan, string $email, string $kode): string
    {
        return hash_hmac('sha256', "{$tujuan->value}|{$email}|{$kode}", $this->kunciPenyidik);
    }
}
