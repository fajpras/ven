<?php

namespace Database\Seeders;

use App\Models\Kategori;
use Illuminate\Database\Seeder;

class KategoriSeeder extends Seeder
{
    /**
     * 14 Kategori resmi yang diselaraskan dengan aplikasi mobile VEN
     * (Sesuai dengan DefaultInterestCategories dan CategoryColorMap di ven-mobile)
     */
    public const DAFTAR_KATEGORI = [
        'Cybersecurity',
        'Software',
        'UI/UX',
        'Videography',
        'Photography',
        '3D',
        'Animation',
        'Internet Of Things',
        'Automation System',
        'Game Dev',
        'Fabrication',
        'Manufacturing',
        '2D',
        'Others',
    ];

    public function run(): void
    {
        foreach (self::DAFTAR_KATEGORI as $namaKategori) {
            Kategori::firstOrCreate([
                'kategori' => $namaKategori,
            ]);
        }
    }
}

