<?php

namespace Database\Seeders;

use App\Models\Kategori;
use App\Models\Pengguna;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;

class PenggunaSeeder extends Seeder
{
    public function run(): void
    {
        // 1. Akun Admin Utama
        Pengguna::updateOrCreate(
            ['email' => 'admin@ven.test'],
            [
                'nama' => 'Administrator VEN',
                'nama_panggilan' => 'admin_ven',
                'kata_sandi' => Hash::make('password123'),
                'deskripsi' => 'Akun pengelola utama platform Virtual Exhibition (VEN).',
                'role' => 'admin',
                'status' => 'aktif',
            ],
        );

        // 2. Akun Pengguna / Creator 1
        $fajri = Pengguna::updateOrCreate(
            ['email' => 'fajri@ven.test'],
            [
                'nama' => 'Fajri Nur Prasetyo',
                'nama_panggilan' => 'fajrinur',
                'kata_sandi' => Hash::make('password123'),
                'deskripsi' => 'Pengembang fullstack dan kreator pameran seni 3D.',
                'role' => 'pengguna',
                'status' => 'aktif',
            ],
        );

        // 3. Akun Pengguna / Creator 2
        $creator = Pengguna::updateOrCreate(
            ['email' => 'kreator@ven.test'],
            [
                'nama' => 'Kreator Digital',
                'nama_panggilan' => 'kreator_digital',
                'kata_sandi' => Hash::make('password123'),
                'deskripsi' => 'Seniman 3D modelling dan perancang ruangan virtual.',
                'role' => 'pengguna',
                'status' => 'aktif',
            ],
        );

        // 4. Akun Pengguna Terblokir / Sanksi (Untuk Pengujian)
        Pengguna::updateOrCreate(
            ['email' => 'suspended@ven.test'],
            [
                'nama' => 'Akun Teranggung',
                'nama_panggilan' => 'user_suspended',
                'kata_sandi' => Hash::make('password123'),
                'deskripsi' => 'Akun contoh untuk pengujian fitur sanksi dan tata tertib.',
                'role' => 'pengguna',
                'status' => 'ditangguhkan',
            ],
        );

        // Assign Minat Kategori ke Pengguna sesuai kategori mobile
        $kategori3D = Kategori::firstWhere('kategori', '3D');
        $kategoriAnimation = Kategori::firstWhere('kategori', 'Animation');
        $kategoriSoftware = Kategori::firstWhere('kategori', 'Software');

        if ($fajri) {
            $minatFajri = [];
            if ($kategori3D) {
                $minatFajri[$kategori3D->id_kategori] = ['bobot' => 0.850];
            }
            if ($kategoriSoftware) {
                $minatFajri[$kategoriSoftware->id_kategori] = ['bobot' => 0.750];
            }
            if (! empty($minatFajri)) {
                $fajri->kategori()->syncWithoutDetaching($minatFajri);
            }
        }

        if ($creator) {
            $minatCreator = [];
            if ($kategori3D) {
                $minatCreator[$kategori3D->id_kategori] = ['bobot' => 0.950];
            }
            if ($kategoriAnimation) {
                $minatCreator[$kategoriAnimation->id_kategori] = ['bobot' => 0.800];
            }
            if (! empty($minatCreator)) {
                $creator->kategori()->syncWithoutDetaching($minatCreator);
            }
        }
    }
}
