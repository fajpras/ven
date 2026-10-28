<?php

namespace Database\Seeders;

use App\Models\Model3D;
use App\Models\Pameran;
use App\Models\Pengguna;
use App\Models\Ruangan;
use Illuminate\Database\Seeder;

class PameranSeeder extends Seeder
{
    public function run(): void
    {
        // 1. Ambil akun kreator (kreator@ven.test atau pengguna biasa pertama)
        $creator = Pengguna::firstWhere('email', 'kreator@ven.test')
            ?? Pengguna::firstWhere('email', 'fajri@ven.test')
            ?? Pengguna::firstWhere('role', 'pengguna');

        if (! $creator) {
            $this->command?->warn('Tidak ada akun kreator ditemukan. Jalankan PenggunaSeeder terlebih dahulu.');
            return;
        }

        // 2. Pastikan model 3D hall pameran tersedia
        $modelHall = Model3D::firstOrCreate(
            ['nama' => 'Grand Virtual Gallery Hall'],
            [
                'jenis' => 'hall',
                'file' => 'models/hall_gallery.glb',
                'kapasitas' => '20',
                'gambar' => 'models/hall_gallery_preview.png',
            ]
        );

        // 3. Tambahkan pameran milik kreator
        $pameran = Pameran::updateOrCreate(
            [
                'id_pengguna' => $creator->id_pengguna,
                'judul' => 'Virtual Art & 3D Interactive Exhibition 2026',
            ],
            [
                'id_model' => $modelHall->id_model,
                'warna' => '#1565C0',
                'banner' => 'pameran/banner/virtual_art_banner.jpg',
                'tipe' => 'sedang',
            ]
        );

        // 4. Tambahkan ruangan default untuk pameran ini
        Ruangan::firstOrCreate([
            'id_pameran' => $pameran->id_pameran,
            'jenis' => 'umum',
        ]);
    }
}

