<?php

namespace Database\Seeders;

use App\Models\Kategori;
use App\Models\Pengguna;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Str;

class PenggunaSeeder extends Seeder
{
    public function run(): void
    {
        // 1. Akun Admin Utama
        $admin = Pengguna::create([
            'id_pengguna'    => (string) Str::uuid(),
            'nama'           => 'Administrator VEN',
            'nama_panggilan' => 'admin_ven',
            'email'          => 'admin@ven.test',
            'kata_sandi'     => Hash::make('password123'),
            'deskripsi'      => 'Akun pengelola utama platform Virtual Exhibition (VEN).',
            'role'           => 'admin',
            'status'         => 'aktif',
        ]);

        // 2. Akun Pengguna / Creator 1
        $fajri = Pengguna::create([
            'id_pengguna'    => (string) Str::uuid(),
            'nama'           => 'Fajri Nur Prasetyo',
            'nama_panggilan' => 'fajrinur',
            'email'          => 'fajri@ven.test',
            'kata_sandi'     => Hash::make('password123'),
            'deskripsi'      => 'Pengembang fullstack dan kreator pameran seni 3D.',
            'role'           => 'pengguna',
            'status'         => 'aktif',
        ]);

        // 3. Akun Pengguna / Creator 2
        $creator = Pengguna::create([
            'id_pengguna'    => (string) Str::uuid(),
            'nama'           => 'Kreator Digital',
            'nama_panggilan' => 'kreator_digital',
            'email'          => 'kreator@ven.test',
            'kata_sandi'     => Hash::make('password123'),
            'deskripsi'      => 'Seniman 3D modelling dan perancang ruangan virtual.',
            'role'           => 'pengguna',
            'status'         => 'aktif',
        ]);

        // 4. Akun Pengguna Terblokir / Sanksi (Untuk Pengujian)
        $userSanksi = Pengguna::create([
            'id_pengguna'    => (string) Str::uuid(),
            'nama'           => 'Akun Teranggung',
            'nama_panggilan' => 'user_suspended',
            'email'          => 'suspended@ven.test',
            'kata_sandi'     => Hash::make('password123'),
            'deskripsi'      => 'Akun contoh untuk pengujian fitur sanksi dan tata tertib.',
            'role'           => 'pengguna',
            'status'         => 'ditanggungkan',
        ]);

        // Optional: Assign Minat Kategori ke Pengguna (jika data Kategori sudah di-seed)
        $kategoriDigit = Kategori::firstWhere('kategori', 'Seni Digital 3D');
        $kategoriDesain = Kategori::firstWhere('kategori', 'Desain Grafis & Poster');

        if ($kategoriDigit && $kategoriDesain) {
            $fajri->kategori()->attach([
                $kategoriDigit->id_kategori  => ['bobot' => 0.850],
                $kategoriDesain->id_kategori => ['bobot' => 0.600],
            ]);

            $creator->kategori()->attach([
                $kategoriDigit->id_kategori => ['bobot' => 0.950],
            ]);
        }
    }
}