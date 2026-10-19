<?php

namespace Tests\Feature;

use App\Models\Kategori;
use App\Models\Pengguna;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

class ProfilMinatTest extends TestCase
{
    private Pengguna $pengguna;

    /** @var list<int> */
    private array $idKategori = [];

    protected function setUp(): void
    {
        parent::setUp();

        $this->assertSame(
            'ven_testing',
            DB::connection('mysql')->getDatabaseName(),
            'Test harus memakai database ven_testing, bukan database aplikasi.'
        );

        $kategoriPertama = Kategori::firstOrCreate(['kategori' => 'Uji Minat Satu']);
        $kategoriKedua = Kategori::firstOrCreate(['kategori' => 'Uji Minat Dua']);

        $this->idKategori = [$kategoriPertama->id_kategori, $kategoriKedua->id_kategori];

        $this->pengguna = Pengguna::factory()->create([
            'email' => 'uji-minat-'.uniqid().'@ven.test',
            'nama_panggilan' => 'uji_minat_'.uniqid(),
            'onboarding_selesai' => false,
        ]);
    }

    protected function tearDown(): void
    {
        $this->pengguna->tokens()->delete();

        DB::connection('mysql')->table('pengguna_kategori')
            ->where('id_pengguna', $this->pengguna->id_pengguna)
            ->delete();

        $this->pengguna->forceDelete();

        Kategori::whereIn('id_kategori', $this->idKategori)->delete();

        DB::setDefaultConnection('mysql');

        parent::tearDown();
    }

    public function test_tamu_tidak_dapat_mengakses_kategori_dan_minat(): void
    {
        $this->getJson('/api/profil/kategori')->assertUnauthorized();
        $this->getJson('/api/profil/minat')->assertUnauthorized();
    }

    public function test_login_mengirim_status_onboarding_pengguna(): void
    {
        $this->postJson('/api/auth/masuk', [
            'identitas' => $this->pengguna->email,
            'kata_sandi' => 'password',
        ])
            ->assertOk()
            ->assertJsonPath('data.pengguna.onboarding_selesai', false)
            ->assertJsonPath('data.pengguna.role', 'pengguna');
    }

    public function test_pengguna_dapat_melihat_daftar_kategori(): void
    {
        $response = $this->actingAs($this->pengguna)->getJson('/api/profil/kategori');

        $response->assertOk()
            ->assertJsonPath('message', 'OK')
            ->assertJsonFragment(['id_kategori' => $this->idKategori[0]]);
    }

    public function test_pengguna_dapat_menyimpan_minat_dan_onboarding_selesai(): void
    {
        $payload = [
            'kategori' => [
                ['id_kategori' => $this->idKategori[0], 'bobot' => 1],
                ['id_kategori' => $this->idKategori[1], 'bobot' => 1],
            ],
        ];

        $this->actingAs($this->pengguna)
            ->putJson('/api/profil/minat', $payload)
            ->assertOk()
            ->assertJsonPath('message', 'Minat kategori disimpan.');

        $this->assertSame(2, $this->pengguna->kategori()->count());
        $this->assertTrue($this->pengguna->fresh()->onboarding_selesai);

        foreach ($this->idKategori as $idKategori) {
            $this->assertDatabaseHas('pengguna_kategori', [
                'id_pengguna' => $this->pengguna->id_pengguna,
                'id_kategori' => $idKategori,
                'bobot' => 1,
            ], 'mysql');
        }
    }

    public function test_maybe_later_menghapus_minat_dan_tetap_menandai_selesai(): void
    {
        $this->pengguna->kategori()->attach($this->idKategori[0], ['bobot' => 1]);

        $this->actingAs($this->pengguna)
            ->putJson('/api/profil/minat', ['kategori' => []])
            ->assertOk();

        $this->assertSame(0, $this->pengguna->kategori()->count());
        $this->assertTrue($this->pengguna->fresh()->onboarding_selesai);
    }

    public function test_kategori_yang_tidak_ada_ditolak(): void
    {
        $payload = [
            'kategori' => [
                ['id_kategori' => 999999, 'bobot' => 1],
            ],
        ];

        $this->actingAs($this->pengguna)
            ->putJson('/api/profil/minat', $payload)
            ->assertUnprocessable()
            ->assertJsonValidationErrors('kategori.0.id_kategori');
    }
}
