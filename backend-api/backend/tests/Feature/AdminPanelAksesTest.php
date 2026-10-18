<?php

namespace Tests\Feature;

use App\Models\Pengguna;
use Illuminate\Support\Facades\Hash;
use Tests\TestCase;

class AdminPanelAksesTest extends TestCase
{
    public function test_halaman_utama_mengarahkan_ke_login_admin(): void
    {
        $this->get('/')->assertRedirect(route('admin.login'));
    }

    public function test_halaman_admin_tidak_dapat_diakses_tanpa_login(): void
    {
        $this->get(route('admin.accounts'))->assertRedirect(route('admin.login'));
    }

    public function test_pengguna_biasa_tidak_dapat_membuka_halaman_admin(): void
    {
        $pengguna = Pengguna::factory()->create([
            'kata_sandi' => Hash::make('password123'),
            'role' => 'pengguna',
        ]);

        $this->actingAs($pengguna)
            ->get(route('admin.accounts'))
            ->assertRedirect(route('admin.login'));
    }
}
