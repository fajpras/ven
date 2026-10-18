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

        // Optional: Assign Minat Kategori ke Pengguna (jika data Kategori sudah di-seed)
        $kategoriDigit = Kategori::firstWhere('kategori', 'Seni Digital 3D');
        $kategoriDesain = Kategori::firstWhere('kategori', 'Desain Grafis & Poster');

        if ($kategoriDigit && $kategoriDesain) {
            $fajri->kategori()->syncWithoutDetaching([
                $kategoriDigit->id_kategori => ['bobot' => 0.850],
                $kategoriDesain->id_kategori => ['bobot' => 0.600],
            ]);

            $creator->kategori()->syncWithoutDetaching([
                $kategoriDigit->id_kategori => ['bobot' => 0.950],
            ]);
        }
    }
}
