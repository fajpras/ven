<?php

namespace Tests\Feature;

use App\Contracts\PembuatKodeOtp;
use App\Enums\TujuanOtp;
use App\Mail\KodeOtpMail;
use App\Models\ResetKataSandi;
use App\Services\Otp\LayananOtp;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Mail;
use Illuminate\Support\Facades\Schema;
use Tests\TestCase;

class LayananOtpRegistrasiTest extends TestCase
{
    protected function setUp(): void
    {
        parent::setUp();

        Schema::create('reset_kata_sandi', function (Blueprint $table): void {
            $table->bigIncrements('id_reset');
            $table->string('tujuan');
            $table->string('email', 254);
            $table->uuid('id_pengguna')->nullable();
            $table->string('otp_hash');
            $table->unsignedTinyInteger('percobaan')->default(0);
            $table->timestamp('expired_at');
            $table->timestamp('used_at')->nullable();
            $table->timestamp('created_at')->useCurrent();
        });
    }

    protected function tearDown(): void
    {
        Schema::dropIfExists('reset_kata_sandi');

        parent::tearDown();
    }

    public function test_registration_otp_can_be_issued_and_verified(): void
    {
        Mail::fake();

        $pembuatKode = new class implements PembuatKodeOtp
        {
            public function buat(int $panjang): string
            {
                return str_pad('123456', $panjang, '0');
            }
        };

        $layananOtp = new LayananOtp($pembuatKode, 'test-secret');
        $email = 'new-user@example.com';

        $layananOtp->terbitkan(TujuanOtp::Registrasi, $email);

        Mail::assertSent(KodeOtpMail::class, fn (KodeOtpMail $mail): bool => $mail->kode === '123456');

        $hasil = $layananOtp->verifikasi(TujuanOtp::Registrasi, $email, '123456');

        $this->assertSame($email, $hasil->email);
        $this->assertNotNull(
            ResetKataSandi::query()->whereKey($hasil->getKey())->value('used_at')
        );
    }
}
