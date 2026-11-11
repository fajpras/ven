<?php

namespace Database\Seeders;

use App\Models\Model3D;
use Illuminate\Database\Seeder;

class ModelSeeder extends Seeder
{
    /**
     * Seed tabel model dengan hall 3D berkapasitas kelipatan 6.
     * File GLB dapat ditempatkan pada proyek Unity di C:\laragon\www\VEN\VEN\Assets\3DModels\Halls\
     * dan di backend pada storage/app/public/models/halls/
     */
    public function run(): void
    {
        $halls = [
            [
                'nama' => 'Hall Gallery Minimalist 6',
                'kapasitas' => '6',
                'file' => 'models/halls/hall_6.glb',
                'gambar' => 'models/halls/preview_hall_6.png',
            ],
            [
                'nama' => 'Hall Studio Modern 12',
                'kapasitas' => '12',
                'file' => 'models/halls/hall_12.glb',
                'gambar' => 'models/halls/preview_hall_12.png',
            ],
            [
                'nama' => 'Hall Contemporary Gallery 18',
                'kapasitas' => '18',
                'file' => 'models/halls/hall_18.glb',
                'gambar' => 'models/halls/preview_hall_18.png',
            ],
            [
                'nama' => 'Hall Pavilion 24',
                'kapasitas' => '24',
                'file' => 'models/halls/hall_24.glb',
                'gambar' => 'models/halls/preview_hall_24.png',
            ],
            [
                'nama' => 'Hall Atrium 30',
                'kapasitas' => '30',
                'file' => 'models/halls/hall_30.glb',
                'gambar' => 'models/halls/preview_hall_30.png',
            ],
            [
                'nama' => 'Grand Exhibition Hall 36',
                'kapasitas' => '36',
                'file' => 'models/halls/hall_36.glb',
                'gambar' => 'models/halls/preview_hall_36.png',
            ],
            [
                'nama' => 'Hall Eksibisi Megah 42',
                'kapasitas' => '42',
                'file' => 'models/halls/hall_42.glb',
                'gambar' => 'models/halls/preview_hall_42.png',
            ],
            [
                'nama' => 'Hall Katedral Virtual 48',
                'kapasitas' => '48',
                'file' => 'models/halls/hall_48.glb',
                'gambar' => 'models/halls/preview_hall_48.png',
            ],
            [
                'nama' => 'Hall Konvensi Nasional 54',
                'kapasitas' => '54',
                'file' => 'models/halls/hall_54.glb',
                'gambar' => 'models/halls/preview_hall_54.png',
            ],
            [
                'nama' => 'Grand Virtual Expo Hall 60',
                'kapasitas' => '60',
                'file' => 'models/halls/hall_60.glb',
                'gambar' => 'models/halls/preview_hall_60.png',
            ],
        ];

        foreach ($halls as $hall) {
            Model3D::updateOrCreate(
                ['nama' => $hall['nama']],
                [
                    'jenis' => 'hall',
                    'file' => $hall['file'],
                    'kapasitas' => $hall['kapasitas'],
                    'gambar' => $hall['gambar'],
                ]
            );
        }
    }
}
