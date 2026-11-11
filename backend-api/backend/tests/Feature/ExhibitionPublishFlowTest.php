<?php

namespace Tests\Feature;

use App\Models\LogKeamanan;
use App\Models\Model3D;
use App\Models\Pengguna;
use Illuminate\Http\UploadedFile;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Storage;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

class ExhibitionPublishFlowTest extends TestCase
{
    protected Pengguna $user;

    protected function setUp(): void
    {
        parent::setUp();

        Storage::fake('public');

        \App\Models\Kategori::firstOrCreate(['kategori' => 'General']);

        // Buat atau ambil pengguna aktif untuk pengujian
        $this->user = Pengguna::firstOrCreate(
            ['email' => 'user_test_publish@example.com'],
            [
                'nama' => 'Test User Publish',
                'nama_panggilan' => 'Tester',
                'kata_sandi' => bcrypt('password123'),
                'status_akun' => 'aktif',
            ]
        );

        Sanctum::actingAs($this->user);
    }

    protected function tearDown(): void
    {
        DB::table('karya')->where('id_pengguna', $this->user->id_pengguna)->delete();
        DB::table('ruangan')->whereIn('id_pameran', function ($q) {
            $q->select('id_pameran')->from('pameran')->where('id_pengguna', $this->user->id_pengguna);
        })->delete();
        DB::table('pameran')->where('id_pengguna', $this->user->id_pengguna)->delete();

        parent::tearDown();
    }

    public function test_upload_banner_berhasil_dan_mengembalikan_url(): void
    {
        $file = UploadedFile::fake()->image('banner_pameran.jpg', 800, 600);

        $response = $this->postJson('/api/pameran/banner', [
            'banner' => $file,
        ]);

        $response->assertOk()
            ->assertJsonStructure([
                'message',
                'data' => ['url', 'path'],
            ]);
    }

    public function test_kata_kasar_menghasilkan_422_mengandung_kata_kasar(): void
    {
        $response = $this->postJson('/api/exhibitions', [
            'templateId' => 'template01',
            'title' => 'Pameran Anjing Liar',
            'description' => 'Deskripsi biasa',
        ]);

        $response->assertStatus(422)
            ->assertJson([
                'message' => 'mengandung kata kasar',
            ]);
    }

    public function test_panel_melebihi_batas_menghasilkan_422(): void
    {
        // Template01 maksimal 9 panel
        $items = [];
        for ($i = 0; $i < 10; $i++) {
            $items[] = [
                'prefabId' => 'Artwork Panel',
                'surface' => 'wall',
                'x' => 1.0,
                'y' => 1.0,
                'z' => 1.0,
            ];
        }

        $response = $this->postJson('/api/exhibitions', [
            'templateId' => 'template01',
            'title' => 'Pameran Bersih Normal',
            'description' => 'Deskripsi pameran',
            'items' => $items,
        ]);

        $response->assertStatus(422)
            ->assertJson([
                'message' => 'panel melebihi jumlah maksimal',
            ]);
    }

    public function test_objek_melebihi_batas_menghasilkan_422(): void
    {
        // Template01 maksimal 9 objek
        $items = [];
        for ($i = 0; $i < 10; $i++) {
            $items[] = [
                'prefabId' => 'Chair Modern',
                'surface' => 'floor',
                'x' => 1.0,
                'y' => 0.0,
                'z' => 1.0,
            ];
        }

        $response = $this->postJson('/api/exhibitions', [
            'templateId' => 'template01',
            'title' => 'Pameran Dekorasi Banyak',
            'description' => 'Deskripsi pameran',
            'items' => $items,
        ]);

        $response->assertStatus(422)
            ->assertJson([
                'message' => 'objek melebihi jumlah maksimal',
            ]);
    }

    public function test_publikasi_sukses_menghasilkan_200_dan_mencatat_log_keamanan(): void
    {
        $items = [
            [
                'prefabId' => 'Artwork Panel',
                'surface' => 'wall',
                'x' => 0.0,
                'y' => 1.5,
                'z' => 2.0,
                'rotY' => 0.0,
                'contentUrl' => 'https://example.com/poster.jpg',
            ],
            [
                'prefabId' => 'Modern Lamp',
                'surface' => 'floor',
                'x' => 1.0,
                'y' => 0.0,
                'z' => 1.0,
            ],
        ];

        $response = $this->postJson('/api/exhibitions', [
            'templateId' => 'template01',
            'title' => 'Pameran Seni Modern',
            'description' => 'Eksibisi seni visual kontemporer',
            'banner' => '/storage/pameran/banner/test.jpg',
            'wallColor' => '#FFFFFF',
            'items' => $items,
        ]);

        $response->assertOk()
            ->assertJson([
                'message' => 'pameran berhasil dipublikasi',
            ]);

        // Pastikan tercatat di log keamanan
        $this->assertDatabaseHas('log_keamanan', [
            'event' => 'pameran_dipublikasi',
            'id_pengguna' => $this->user->id_pengguna,
            'status_kode' => 200,
        ]);
    }
}
