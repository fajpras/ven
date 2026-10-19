<?php

namespace Database\Seeders;

use App\Models\Kategori;
use Illuminate\Database\Seeder;

class KategoriSeeder extends Seeder
{
    /**
     * 14 kategori minat sesuai DESIGN.md §8.1.
     *
     * @var list<string>
     */
    public const DAFTAR = [
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
        foreach (self::DAFTAR as $nama) {
            Kategori::firstOrCreate(['kategori' => $nama]);
        }
    }
}
