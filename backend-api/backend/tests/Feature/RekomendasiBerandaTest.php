<?php

namespace Tests\Feature;

use App\Models\Karya;
use App\Models\Kategori;
use App\Models\Model3D;
use App\Models\Pameran;
use App\Models\Pengguna;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

class RekomendasiBerandaTest extends TestCase
{
    private Pengguna $pengguna;
    private Kategori $kategoriTinggi;
    private Kategori $kategoriRendah;
    private Kategori $kategoriNol;

    private Karya $karyaTinggi;
    private Karya $karyaRendah;
    private Karya $karyaNol;

    private Pameran $pameranFavorit;
    private Pameran $pameranBiasa;

    protected function setUp(): void
    {
        parent::setUp();

        $this->assertSame(
            'ven_testing',
            DB::connection('mysql')->getDatabaseName(),
            'Test harus menggunakan database ven_testing.'
        );

        $this->pengguna = Pengguna::factory()->create([
            'email' => 'rekomendasi-'.uniqid().'@ven.test',
            'nama_panggilan' => 'user_rek_'.uniqid(),
        ]);

        $this->kategoriTinggi = Kategori::firstOrCreate(['kategori' => 'Kategori Tinggi '.uniqid()]);
        $this->kategoriRendah = Kategori::firstOrCreate(['kategori' => 'Kategori Rendah '.uniqid()]);
        $this->kategoriNol = Kategori::firstOrCreate(['kategori' => 'Kategori Nol '.uniqid()]);

        // Hubungkan minat pengguna: Tinggi (bobot 1.0), Rendah (bobot 0.4)
        $this->pengguna->kategori()->attach([
            $this->kategoriTinggi->id_kategori => ['bobot' => 1.0],
            $this->kategoriRendah->id_kategori => ['bobot' => 0.4],
        ]);

        // Siapkan model 3D
        $model = Model3D::firstOrCreate(
            ['jenis' => 'hall', 'nama' => 'Template Test'],
            ['file' => 'test.glb', 'gambar' => 'test.png', 'kapasitas' => 10]
        );

        // Siapkan Pameran
        $this->pameranFavorit = Pameran::create([
            'id_pengguna' => $this->pengguna->id_pengguna,
            'id_model' => $model->id_model,
            'judul' => 'Pameran Favorit '.uniqid(),
            'tipe' => 'kecil',
            'warna' => '#1F1F1F',
            'banner' => 'banner_favorit.jpg',
        ]);

        $this->pameranBiasa = Pameran::create([
            'id_pengguna' => $this->pengguna->id_pengguna,
            'id_model' => $model->id_model,
            'judul' => 'Pameran Biasa '.uniqid(),
            'tipe' => 'kecil',
            'warna' => '#1F1F1F',
            'banner' => 'banner_biasa.jpg',
        ]);

        // Siapkan Karya
        $this->karyaTinggi = Karya::create([
            'id_pengguna' => $this->pengguna->id_pengguna,
            'id_pameran' => $this->pameranFavorit->id_pameran,
            'id_kategori' => $this->kategoriTinggi->id_kategori,
            'judul' => 'Karya Bobot Tinggi '.uniqid(),
            'deskripsi' => 'Deskripsi karya tinggi',
            'file' => 'karya_tinggi.jpg',
            'jenis' => 'poster',
        ]);

        $this->karyaRendah = Karya::create([
            'id_pengguna' => $this->pengguna->id_pengguna,
            'id_pameran' => $this->pameranFavorit->id_pameran,
            'id_kategori' => $this->kategoriRendah->id_kategori,
            'judul' => 'Karya Bobot Rendah '.uniqid(),
            'deskripsi' => 'Deskripsi karya rendah',
            'file' => 'karya_rendah.jpg',
            'jenis' => 'poster',
        ]);

        $this->karyaNol = Karya::create([
            'id_pengguna' => $this->pengguna->id_pengguna,
            'id_pameran' => $this->pameranBiasa->id_pameran,
            'id_kategori' => $this->kategoriNol->id_kategori,
            'judul' => 'Karya Kategori Tidak Dipilih '.uniqid(),
            'deskripsi' => 'Deskripsi karya tanpa bobot',
            'file' => 'karya_nol.jpg',
            'jenis' => 'poster',
        ]);
    }

    protected function tearDown(): void
    {
        $this->pengguna->tokens()->delete();

        Karya::whereIn('id_karya', [
            $this->karyaTinggi->id_karya,
            $this->karyaRendah->id_karya,
            $this->karyaNol->id_karya,
        ])->forceDelete();

        Pameran::whereIn('id_pameran', [
            $this->pameranFavorit->id_pameran,
            $this->pameranBiasa->id_pameran,
        ])->forceDelete();

        DB::connection('mysql')->table('pengguna_kategori')
            ->where('id_pengguna', $this->pengguna->id_pengguna)
            ->delete();

        $this->pengguna->forceDelete();

        Kategori::whereIn('id_kategori', [
            $this->kategoriTinggi->id_kategori,
            $this->kategoriRendah->id_kategori,
            $this->kategoriNol->id_kategori,
        ])->delete();

        DB::setDefaultConnection('mysql');

        parent::tearDown();
    }

    public function test_rekomendasi_karya_memprioritaskan_kategori_berbobot_tinggi(): void
    {
        $response = $this->actingAs($this->pengguna)
            ->getJson('/api/karya/rekomendasi');

        $response->assertOk();

        $items = $response->json('data.data');
        $this->assertNotEmpty($items);

        $ids = array_column($items, 'id_karya');

        // Pastikan urutan: karya berbobot 1.0 muncul sebelum 0.4, dan karya 0.4 muncul sebelum karya tak berpilih
        $posisiTinggi = array_search($this->karyaTinggi->id_karya, $ids, true);
        $posisiRendah = array_search($this->karyaRendah->id_karya, $ids, true);
        $posisiNol = array_search($this->karyaNol->id_karya, $ids, true);

        $this->assertNotFalse($posisiTinggi, 'Karya tinggi harus ditemukan');
        $this->assertNotFalse($posisiRendah, 'Karya rendah harus ditemukan');
        $this->assertNotFalse($posisiNol, 'Karya tanpa bobot harus tetap muncul');

        $this->assertLessThan($posisiRendah, $posisiTinggi, 'Karya bobot 1.0 harus muncul sebelum bobot 0.4');
        $this->assertLessThan($posisiNol, $posisiRendah, 'Karya bobot 0.4 harus muncul sebelum bobot 0');
    }

    public function test_rekomendasi_karya_pengguna_tanpa_minat_menampilkan_semua(): void
    {
        $userBaru = Pengguna::factory()->create([
            'email' => 'tanpa-minat-'.uniqid().'@ven.test',
        ]);

        $response = $this->actingAs($userBaru)
            ->getJson('/api/karya/rekomendasi');

        $response->assertOk();
        $items = $response->json('data.data');
        $ids = array_column($items, 'id_karya');

        // Pastikan seluruh karya tetap bisa tampil
        $this->assertContains($this->karyaTinggi->id_karya, $ids);
        $this->assertContains($this->karyaRendah->id_karya, $ids);
        $this->assertContains($this->karyaNol->id_karya, $ids);

        $userBaru->tokens()->delete();
        $userBaru->forceDelete();
    }

    public function test_carousel_pameran_memprioritaskan_pameran_yang_memuat_karya_minat_user(): void
    {
        $response = $this->actingAs($this->pengguna)
            ->getJson('/api/pameran/rekomendasi');

        $response->assertOk();

        $items = $response->json('data.data');
        $this->assertNotEmpty($items);

        $idList = array_column($items, 'id_pameran');

        $posisiFavorit = array_search($this->pameranFavorit->id_pameran, $idList, true);
        $posisiBiasa = array_search($this->pameranBiasa->id_pameran, $idList, true);

        $this->assertNotFalse($posisiFavorit);
        $this->assertNotFalse($posisiBiasa);

        // Pameran Favorit memuat karya dengan total bobot 1.4, Pameran Biasa hanya 0.
        $this->assertLessThan($posisiBiasa, $posisiFavorit, 'Pameran dengan akumulasi bobot karya lebih tinggi harus muncul di carousel terdepan');
    }
}
