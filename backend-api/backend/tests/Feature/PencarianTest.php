<?php

namespace Tests\Feature;

use App\Models\Karya;
use App\Models\Kategori;
use App\Models\Model3D;
use App\Models\Pameran;
use App\Models\Pengguna;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

class PencarianTest extends TestCase
{
    private Pengguna $pengguna;
    private Pengguna $targetPengguna;
    private Kategori $kategori;
    private Pameran $pameran;
    private Karya $karya;
    private string $keyword;

    protected function setUp(): void
    {
        parent::setUp();

        $this->assertSame(
            'ven_testing',
            DB::connection('mysql')->getDatabaseName(),
            'Test harus menggunakan database ven_testing.'
        );

        $this->keyword = 'Unik'.uniqid();

        $this->pengguna = Pengguna::factory()->create([
            'email' => 'pencari-'.uniqid().'@ven.test',
        ]);

        $this->targetPengguna = Pengguna::factory()->create([
            'nama' => "Seniman {$this->keyword}",
            'nama_panggilan' => "seniman_{$this->keyword}",
            'status' => 'aktif',
        ]);

        $this->kategori = Kategori::firstOrCreate(['kategori' => "Kategori {$this->keyword}"]);

        $model = Model3D::firstOrCreate(
            ['jenis' => 'hall', 'nama' => 'Template Search Test'],
            ['file' => 'search.glb', 'gambar' => 'search.png', 'kapasitas' => 5]
        );

        $this->pameran = Pameran::create([
            'id_pengguna' => $this->targetPengguna->id_pengguna,
            'id_model' => $model->id_model,
            'judul' => "Pameran Spektakuler {$this->keyword}",
            'tipe' => 'kecil',
            'warna' => '#FFFFFF',
            'banner' => 'banner_search.jpg',
        ]);

        $this->karya = Karya::create([
            'id_pengguna' => $this->targetPengguna->id_pengguna,
            'id_pameran' => $this->pameran->id_pameran,
            'id_kategori' => $this->kategori->id_kategori,
            'judul' => "Mahakarya Seni {$this->keyword}",
            'deskripsi' => "Sebuah karya bertema {$this->keyword}",
            'file' => 'mahakarya.jpg',
            'jenis' => 'poster',
        ]);
    }

    protected function tearDown(): void
    {
        $this->pengguna->tokens()->delete();
        $this->targetPengguna->tokens()->delete();

        $this->karya->forceDelete();
        $this->pameran->forceDelete();

        $this->pengguna->forceDelete();
        $this->targetPengguna->forceDelete();

        $this->kategori->delete();

        DB::setDefaultConnection('mysql');

        parent::tearDown();
    }

    public function test_pencarian_tanpa_parameter_q_mengembalikan_hasil_default(): void
    {
        $this->actingAs($this->pengguna)
            ->getJson('/api/pencarian')
            ->assertOk()
            ->assertJsonStructure([
                'data' => [
                    'karya',
                    'pameran',
                    'pengguna',
                ],
            ]);
    }

    public function test_pencarian_dengan_tipe_tidak_valid_menghasilkan_error(): void
    {
        $this->actingAs($this->pengguna)
            ->getJson('/api/pencarian?tipe=tidak_ada')
            ->assertUnprocessable()
            ->assertJsonValidationErrors('tipe');
    }

    public function test_pencarian_tipe_semua_mengembalikan_ringkasan(): void
    {
        $response = $this->actingAs($this->pengguna)
            ->getJson('/api/pencarian?q='.$this->keyword.'&tipe=semua');

        $response->assertOk()
            ->assertJsonStructure([
                'data' => [
                    'karya',
                    'pameran',
                    'pengguna',
                ],
            ]);

        $karya = $response->json('data.karya');
        $pameran = $response->json('data.pameran');
        $pengguna = $response->json('data.pengguna');

        $this->assertNotEmpty($karya);
        $this->assertNotEmpty($pameran);
        $this->assertNotEmpty($pengguna);

        $this->assertSame($this->karya->id_karya, $karya[0]['id_karya']);
        $this->assertSame($this->pameran->id_pameran, $pameran[0]['id_pameran']);
        $this->assertSame($this->targetPengguna->id_pengguna, $pengguna[0]['id_pengguna']);
        // Pastikan atribut sensitif tidak bocor di search pengguna
        $this->assertArrayNotHasKey('email', $pengguna[0]);
        $this->assertArrayNotHasKey('kata_sandi', $pengguna[0]);
    }

    public function test_pencarian_tipe_karya_dengan_paginasi(): void
    {
        $response = $this->actingAs($this->pengguna)
            ->getJson('/api/pencarian?q='.$this->keyword.'&tipe=karya');

        $response->assertOk()
            ->assertJsonPath('data.data.0.id_karya', $this->karya->id_karya);
    }

    public function test_pencarian_tipe_pameran_dengan_paginasi(): void
    {
        $response = $this->actingAs($this->pengguna)
            ->getJson('/api/pencarian?q='.$this->keyword.'&tipe=pameran');

        $response->assertOk()
            ->assertJsonPath('data.data.0.id_pameran', $this->pameran->id_pameran);
    }

    public function test_pencarian_tipe_pengguna_dengan_paginasi(): void
    {
        $response = $this->actingAs($this->pengguna)
            ->getJson('/api/pencarian?q='.$this->keyword.'&tipe=pengguna');

        $response->assertOk()
            ->assertJsonPath('data.data.0.id_pengguna', $this->targetPengguna->id_pengguna);
    }
}
