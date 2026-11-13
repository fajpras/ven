<?php

namespace Tests\Feature;

use App\Exceptions\PenanganKesalahanApi;
use Illuminate\Support\Facades\Route;
use RuntimeException;
use Tests\TestCase;

class PenanganKesalahanApiTest extends TestCase
{
    public function test_endpoint_terproteksi_tanpa_token_mengembalikan_json_401(): void
    {
        $response = $this->get('/api/profil');

        $response->assertStatus(401)
            ->assertHeader('content-type', 'application/json')
            ->assertJson([
                'data' => null,
                'message' => PenanganKesalahanApi::PESAN_SESI_BERAKHIR,
            ]);
    }

    public function test_endpoint_api_yang_tidak_ada_mengembalikan_json_404(): void
    {
        $response = $this->get('/api/endpoint-yang-tidak-ada');

        $response->assertStatus(404)
            ->assertHeader('content-type', 'application/json')
            ->assertJson([
                'data' => null,
                'message' => 'Endpoint atau data yang diminta tidak ditemukan.',
            ]);
    }

    public function test_root_api_tetap_mengembalikan_json_404(): void
    {
        $response = $this->get('/api');

        $response->assertStatus(404)
            ->assertHeader('content-type', 'application/json')
            ->assertJson([
                'data' => null,
                'message' => 'Endpoint atau data yang diminta tidak ditemukan.',
            ]);
    }

    public function test_metode_http_yang_salah_mengembalikan_json_405(): void
    {
        $response = $this->post('/api/profil');

        $response->assertStatus(405)
            ->assertHeader('content-type', 'application/json')
            ->assertJson([
                'data' => null,
                'message' => 'Metode permintaan tidak didukung untuk endpoint ini.',
            ]);
    }

    public function test_kesalahan_validasi_mengembalikan_json_422(): void
    {
        $response = $this->postJson('/api/auth/masuk', []);

        $response->assertStatus(422)
            ->assertHeader('content-type', 'application/json')
            ->assertJsonStructure(['data' => ['identitas', 'kata_sandi'], 'errors' => ['identitas', 'kata_sandi'], 'message']);
    }

    public function test_pesan_galat_4xx_dari_aplikasi_tetap_ditampilkan(): void
    {
        Route::get('/api/_uji/terlarang', function (): never {
            abort(403, 'Anda bukan pemilik data ini.');
        });

        $response = $this->get('/api/_uji/terlarang');

        $response->assertStatus(403)
            ->assertHeader('content-type', 'application/json')
            ->assertJson([
                'data' => null,
                'message' => 'Anda bukan pemilik data ini.',
            ]);
    }

    public function test_api_yang_dibuka_dari_browser_dialihkan_ke_halaman_404(): void
    {
        $this->withHeaders([
            'Accept' => 'text/html,application/xhtml+xml',
            'User-Agent' => 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0 Safari/537.36',
            'Sec-Fetch-Mode' => 'navigate',
        ])->get('/api/profil')->assertRedirect('/404');

        $this->get('/404')
            ->assertStatus(404)
            ->assertSee('Halaman Tidak Ditemukan')
            ->assertSee('Value Exhibition Network');
    }

    public function test_api_dengan_accept_json_atau_wildcard_tetap_mendapat_json(): void
    {
        $this->get('/api/profil', ['Accept' => 'application/json'])
            ->assertStatus(401)
            ->assertJson(['data' => null, 'message' => PenanganKesalahanApi::PESAN_SESI_BERAKHIR]);

        $this->get('/api/profil', ['Accept' => '*/*'])
            ->assertStatus(401)
            ->assertJson(['data' => null, 'message' => PenanganKesalahanApi::PESAN_SESI_BERAKHIR]);
    }

    public function test_galat_server_tidak_membocorkan_detail_internal(): void
    {
        Route::get('/api/_uji/galat-server', function (): never {
            throw new RuntimeException('detail internal yang tidak boleh tampil');
        });

        $response = $this->get('/api/_uji/galat-server');

        $response->assertStatus(500)
            ->assertHeader('content-type', 'application/json')
            ->assertJson([
                'data' => null,
                'message' => 'Terjadi kesalahan pada server. Silakan coba lagi nanti.',
            ]);

        $this->assertStringNotContainsString(
            'detail internal',
            (string) $response->getContent(),
        );
    }
}
