<?php

namespace Tests\Feature;

use App\Models\Pengguna;
use Illuminate\Foundation\Testing\DatabaseTransactions;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

/**
 * Audit & Test Kepatuhan:
 * 1. Pembatasan Akses dengan View (v_pengguna_publik)
 * 2. Hashing dan Salting (Bcrypt)
 */
class AuditKeamananDanViewTest extends TestCase
{
    use DatabaseTransactions;

    /**
     * Audit 1: Pembatasan Akses dengan View (v_pengguna_publik)
     * Memastikan:
     * - View v_pengguna_publik hanya mengekspos kolom publik yang aman:
     *   id_pengguna, nama, nama_panggilan, nomor_hp, deskripsi, onboarding_selesai,
     *   email, foto_profil, status, created_at, updated_at, deleted_at.
     * - Kolom sensitif (kata_sandi, google_id, google_id_bidx, role) TIDAK ADA di view.
     */
    public function test_view_v_pengguna_publik_membatasi_akses_kolom_sensitif(): void
    {
        $pengguna = Pengguna::factory()->create([
            'nama' => 'User Audit View',
            'kata_sandi' => Hash::make('rahasia123'),
            'google_id' => 'google-sensitive-id-12345',
            'role' => 'pengguna',
            'status' => 'aktif',
        ]);

        // Query melalui View publik
        $dataView = DB::table('v_pengguna_publik')
            ->where('id_pengguna', $pengguna->id_pengguna)
            ->first();

        $this->assertNotNull($dataView, 'Data pengguna harus dapat diakses melalui view v_pengguna_publik.');

        // Verifikasi kolom publik tersedia
        $this->assertSame('User Audit View', $dataView->nama);
        $this->assertSame($pengguna->email, $dataView->email);
        $this->assertSame('aktif', $dataView->status);

        $viewColumns = array_keys((array) $dataView);

        // Kolom yang WAJIB ada di view
        $kolomWajib = [
            'id_pengguna', 'nama', 'nama_panggilan', 'nomor_hp', 'deskripsi',
            'onboarding_selesai', 'email', 'foto_profil', 'status',
            'created_at', 'updated_at', 'deleted_at',
        ];
        foreach ($kolomWajib as $kolom) {
            $this->assertContains($kolom, $viewColumns, "Kolom {$kolom} harus ada di v_pengguna_publik.");
        }

        // Verifikasi kolom sensitif TIDAK BOCOR di dalam view
        $this->assertNotContains('kata_sandi', $viewColumns, 'Kolom kata_sandi tidak boleh ada di view publik.');
        $this->assertNotContains('google_id', $viewColumns, 'Kolom google_id tidak boleh ada di view publik.');
        $this->assertNotContains('google_id_bidx', $viewColumns, 'Kolom google_id_bidx tidak boleh ada di view publik.');
        $this->assertNotContains('role', $viewColumns, 'Kolom internal role tidak boleh diekspos di view publik.');
    }

    /**
     * Memastikan respon endpoint login mengembalikan data pengguna dari view v_pengguna_publik
     * tanpa kolom sensitif (kata_sandi, google_id, google_id_bidx, role).
     */
    public function test_login_mengembalikan_view_pengguna_publik_tanpa_kolom_sensitif(): void
    {
        $password = 'PasswordAudit123!';
        $pengguna = Pengguna::factory()->create([
            'email' => 'login_view_test@ven.test',
            'kata_sandi' => Hash::make($password),
            'role' => 'pengguna',
            'status' => 'aktif',
        ]);

        $response = $this->postJson('/api/auth/masuk', [
            'identitas' => 'login_view_test@ven.test',
            'kata_sandi' => $password,
        ]);

        $response->assertStatus(200)
            ->assertJsonStructure([
                'data' => [
                    'token',
                    'pengguna' => [
                        'id_pengguna', 'nama', 'nama_panggilan', 'nomor_hp',
                        'deskripsi', 'onboarding_selesai', 'email', 'foto_profil',
                        'status', 'created_at', 'updated_at', 'deleted_at',
                    ],
                ],
                'message',
            ]);

        $dataPengguna = $response->json('data.pengguna');

        // Pastikan kolom sensitif tidak ada di objek respon login
        $this->assertArrayNotHasKey('kata_sandi', $dataPengguna);
        $this->assertArrayNotHasKey('google_id', $dataPengguna);
        $this->assertArrayNotHasKey('google_id_bidx', $dataPengguna);
        $this->assertArrayNotHasKey('role', $dataPengguna);
    }

    /**
     * Audit 2: Hashing dan Salting
     * Memastikan:
     * - Password di-hash menggunakan algoritma Bcrypt yang aman (dimulai dengan $2y$).
     * - Menggunakan salt unik otomatis (dua password yang sama menghasilkan hash berbeda).
     * - Hash::check berhasil mencocokkan password asli dan menolak password yang salah.
     */
    public function test_hashing_dan_salting_password(): void
    {
        $passwordPlain = 'SandiKuat@2026!';

        // Hash password menggunakan Hash::make (Bcrypt)
        $hash1 = Hash::make($passwordPlain);
        $hash2 = Hash::make($passwordPlain);

        // 1. Format Bcrypt standard verification ($2y$)
        $this->assertStringStartsWith('$2y$', $hash1, 'Format hash harus menggunakan algoritma Bcrypt ($2y$).');

        // 2. Pembuktian Salting Unik: Input sama harus menghasilkan hash berbeda karena salt acak
        $this->assertNotEquals($hash1, $hash2, 'Salting harus menghasilkan hash yang berbeda meskipun password-nya sama.');

        // 3. Verifikasi Keabsahan Hash::check
        $this->assertTrue(Hash::check($passwordPlain, $hash1), 'Password asli harus lolos validasi Hash::check.');
        $this->assertTrue(Hash::check($passwordPlain, $hash2), 'Password asli harus lolos validasi Hash::check pada salt berbeda.');
        $this->assertFalse(Hash::check('PasswordSalah', $hash1), 'Password yang salah harus ditolak oleh Hash::check.');
    }
}
