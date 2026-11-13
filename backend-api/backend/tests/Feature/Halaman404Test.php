<?php

namespace Tests\Feature;

use Tests\TestCase;

class Halaman404Test extends TestCase
{
    public function test_halaman_web_tidak_ditemukan_menampilkan_blade_404(): void
    {
        $response = $this->get('/halaman-yang-tidak-ada');

        $response->assertStatus(404)
            ->assertSee('Halaman Tidak Ditemukan')
            ->assertSee('Kembali ke Beranda')
            ->assertSee('Muat Ulang')
            ->assertDontSee('RouteNotFoundException');

        $this->assertStringStartsWith('text/html', (string) $response->headers->get('content-type'));
    }

    public function test_halaman_404_mandiri_tanpa_ketergantungan_aset_vite(): void
    {
        $response = $this->get('/halaman-yang-tidak-ada');

        // Halaman galat harus tetap tampil walau aset Vite belum dibangun.
        $response->assertSee('<style>', false)
            ->assertSee('assets/admin/logo-ven.svg', false)
            ->assertDontSee('/build/assets', false);
    }
}
