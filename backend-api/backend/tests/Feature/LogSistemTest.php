<?php

namespace Tests\Feature;

use App\Models\LogAktivitas;
use App\Models\LogKeamanan;
use App\Models\Pameran;
use App\Models\Pengguna;
use App\Services\LogSistem;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Tests\TestCase;

class LogSistemTest extends TestCase
{
    use DatabaseTransactions;

    public function test_mencatat_log_aktivitas_secara_lengkap(): void
    {
        $pengguna = Pengguna::factory()->create(['nama' => 'Tester VEN']);

        LogSistem::aktivitas(
            entitas: $pengguna->nama,
            jenis: 'karya',
            pesan: 'Menambah karya "Lukisan Alam"',
            status: 'berhasil',
            idPengguna: $pengguna->id_pengguna,
            aksi: 'tambah',
        );

        $log = LogAktivitas::where('id_pengguna', $pengguna->id_pengguna)->latest('id_log')->first();

        $this->assertNotNull($log);
        $this->assertSame('Tester VEN', $log->entitas);
        $this->assertSame('karya', $log->jenis);
        $this->assertSame('tambah', $log->aksi);
        $this->assertSame('Menambah karya "Lukisan Alam"', $log->pesan);
        $this->assertSame('berhasil', $log->status);
    }

    public function test_mencatat_log_keamanan_secara_lengkap(): void
    {
        $pengguna = Pengguna::factory()->create();

        LogSistem::keamanan(
            event: 'login_berhasil',
            pesan: 'Pengguna berhasil login',
            statusKode: 200,
            idPengguna: $pengguna->id_pengguna,
        );

        $log = LogKeamanan::where('id_pengguna', $pengguna->id_pengguna)->latest('id_log')->first();

        $this->assertNotNull($log);
        $this->assertSame('login_berhasil', $log->event);
        $this->assertSame(200, $log->status_kode);
        $this->assertSame(200, $log->status);
        $this->assertSame('Pengguna berhasil login', $log->pesan);
    }

    public function test_observer_mencatat_aktivitas_otomatis(): void
    {
        $pengguna = Pengguna::factory()->create(['nama' => 'User Baru']);

        $log = LogAktivitas::where('id_pengguna', $pengguna->id_pengguna)
            ->where('aksi', 'tambah')
            ->first();

        $this->assertNotNull($log);
        $this->assertSame('User Baru', $log->entitas);
        $this->assertSame('pengguna', $log->jenis);
        $this->assertSame('tambah', $log->aksi);
        $this->assertStringContainsString('Menambah pengguna', $log->pesan);
    }

    public function test_observer_mencatat_aktivitas_registrasi(): void
    {
        // Simulasi request pendaftaran akun
        $this->app->instance('request', \Illuminate\Http\Request::create('/api/auth/register', 'POST'));

        $pengguna = Pengguna::factory()->create(['nama' => 'User Registrasi']);

        $log = LogAktivitas::where('id_pengguna', $pengguna->id_pengguna)
            ->where('aksi', 'registrasi')
            ->first();

        $this->assertNotNull($log);
        $this->assertSame('User Registrasi', $log->entitas);
        $this->assertSame('pengguna', $log->jenis);
        $this->assertSame('registrasi', $log->aksi);
        $this->assertStringContainsString('Registrasi akun pengguna', $log->pesan);
    }
}
