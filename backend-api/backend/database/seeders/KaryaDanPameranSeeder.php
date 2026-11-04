<?php

namespace Database\Seeders;

use App\Models\Kategori;
use App\Models\Karya;
use App\Models\Model3D;
use App\Models\Pameran;
use App\Models\Pengguna;
use App\Models\Ruangan;
use Carbon\Carbon;
use Illuminate\Database\Seeder;
use Illuminate\Support\Facades\File;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Str;

class KaryaDanPameranSeeder extends Seeder
{
    public function run(): void
    {
        $original = \Illuminate\Support\Facades\DB::getDefaultConnection();
        if (config()->has('database.connections.mysql_dba')) {
            \Illuminate\Support\Facades\DB::setDefaultConnection('mysql_dba');
        }

        try {
            $this->seedData();
        } finally {
            \Illuminate\Support\Facades\DB::setDefaultConnection($original);
        }
    }

    private function seedData(): void
    {
        // 1. Ambil daftar file banner/foto yang tersedia di storage
        $bannerDir = storage_path('app/public/pameran/banner');
        $karyaDir = storage_path('app/private/karya');

        $bannerFiles = [];
        if (File::isDirectory($bannerDir)) {
            $bannerFiles = collect(File::files($bannerDir))
                ->map(fn ($file) => $file->getFilename())
                ->values()
                ->all();
        }

        // Pastikan fallback nama file jika direktori belum terbaca
        if (empty($bannerFiles)) {
            $bannerFiles = [
                '1JpRYbw7lq6lMBW3lopkHgCGRLKhn78JDESxDY9z.jpg',
                '5x8MFeXQxsvSqGxMsIf8H12FfkGK6QxjNB4bg0RB.png',
                '6gDrwZZS73EX9R577cKrLOF0ZF8pXBnZLZ9K3MF8.png',
                '6OAdTnVVBIBsPpp18Y2MkDxxZgb6dz7qEW9Bdo71.png',
                '9YJu3HzvyCwYDcnETLOCCmS1dlUhX6gJSfmaA7TU.png',
                'aZzwmlb8uNp1x3y8ZFs2lYwLs2tUlrA6jNN2K4UN.png',
            ];
        }

        // Pastikan file karya juga ada di disk local (storage/app/private/karya)
        if (! File::isDirectory($karyaDir)) {
            File::makeDirectory($karyaDir, 0755, true);
        }
        foreach ($bannerFiles as $fName) {
            $src = $bannerDir . DIRECTORY_SEPARATOR . $fName;
            $dst = $karyaDir . DIRECTORY_SEPARATOR . $fName;
            if (File::exists($src) && ! File::exists($dst)) {
                File::copy($src, $dst);
            }
        }

        // 2. Ambil kategori mapping
        $kategoriMap = Kategori::all()->pluck('id_kategori', 'kategori');

        // 3. Pastikan model 3D hall tersedia
        $modelHall = Model3D::firstOrCreate(
            ['nama' => 'Grand Virtual Gallery Hall'],
            [
                'jenis' => 'hall',
                'file' => 'models/hall_gallery.glb',
                'kapasitas' => '20',
                'gambar' => 'models/hall_gallery_preview.png',
            ]
        );

        // 4. Siapkan / Ambil Akun-Akun Kreator Tematik
        $fajri = Pengguna::firstWhere('email', 'fajri@ven.test') ?? Pengguna::firstWhere('role', 'pengguna');

        $kreatorDigital = Pengguna::firstWhere('email', 'kreator@ven.test') ?? $fajri;

        $alexCyber = Pengguna::updateOrCreate(
            ['email' => 'alex.cyber@ven.test'],
            [
                'nama' => 'Alex Pratama',
                'nama_panggilan' => 'alex_cyber',
                'kata_sandi' => Hash::make('password123'),
                'deskripsi' => 'Security Researcher, Ethical Hacker, & Backend Architect.',
                'role' => 'pengguna',
                'status' => 'aktif',
            ]
        );
        if (isset($kategoriMap['Cybersecurity'])) {
            $alexCyber->kategori()->syncWithoutDetaching([
                $kategoriMap['Cybersecurity'] => ['bobot' => 0.950],
                $kategoriMap['Software'] ?? 2 => ['bobot' => 0.800],
            ]);
        }

        $sarahUiux = Pengguna::updateOrCreate(
            ['email' => 'sarah.uiux@ven.test'],
            [
                'nama' => 'Sarah Amanda',
                'nama_panggilan' => 'sarah_uiux',
                'kata_sandi' => Hash::make('password123'),
                'deskripsi' => 'Lead Product Designer & Design System Specialist.',
                'role' => 'pengguna',
                'status' => 'aktif',
            ]
        );
        if (isset($kategoriMap['UI/UX'])) {
            $sarahUiux->kategori()->syncWithoutDetaching([
                $kategoriMap['UI/UX'] => ['bobot' => 0.980],
                $kategoriMap['2D'] ?? 13 => ['bobot' => 0.700],
            ]);
        }

        $dindaVisual = Pengguna::updateOrCreate(
            ['email' => 'dinda.visual@ven.test'],
            [
                'nama' => 'Dinda Kirana',
                'nama_panggilan' => 'dinda_visual',
                'kata_sandi' => Hash::make('password123'),
                'deskripsi' => 'Visual Storyteller, Urban Photographer & Cinematographer.',
                'role' => 'pengguna',
                'status' => 'aktif',
            ]
        );
        if (isset($kategoriMap['Photography'])) {
            $dindaVisual->kategori()->syncWithoutDetaching([
                $kategoriMap['Photography'] => ['bobot' => 0.920],
                $kategoriMap['Videography'] ?? 4 => ['bobot' => 0.850],
            ]);
        }

        $rioIot = Pengguna::updateOrCreate(
            ['email' => 'rio.iot@ven.test'],
            [
                'nama' => 'Rio Wicaksono',
                'nama_panggilan' => 'rio_iot',
                'kata_sandi' => Hash::make('password123'),
                'deskripsi' => 'Embedded Systems, IoT & Industrial Automation Engineer.',
                'role' => 'pengguna',
                'status' => 'aktif',
            ]
        );
        if (isset($kategoriMap['Internet Of Things'])) {
            $rioIot->kategori()->syncWithoutDetaching([
                $kategoriMap['Internet Of Things'] => ['bobot' => 0.950],
                $kategoriMap['Automation System'] ?? 9 => ['bobot' => 0.900],
                $kategoriMap['Manufacturing'] ?? 12 => ['bobot' => 0.750],
            ]);
        }

        // 5. Definisi Pameran Tematik (Carousel / Exhibitions)
        $pameranList = [
            [
                'judul' => 'Cyber Security Expo 2026: Zero Trust Frontier',
                'creator' => $alexCyber,
                'warna' => '#0D47A1',
                'tipe' => 'besar',
                'banner_idx' => 0,
            ],
            [
                'judul' => 'Virtual UI/UX Design Summit & Showcase',
                'creator' => $sarahUiux,
                'warna' => '#7B1FA2',
                'tipe' => 'sedang',
                'banner_idx' => 1,
            ],
            [
                'judul' => 'Next-Gen 3D World & Game Arena',
                'creator' => $fajri,
                'warna' => '#C2185B',
                'tipe' => 'acara',
                'banner_idx' => 2,
            ],
            [
                'judul' => 'Indie Digital Photography & Visual Arts',
                'creator' => $dindaVisual,
                'warna' => '#E65100',
                'tipe' => 'sedang',
                'banner_idx' => 3,
            ],
            [
                'judul' => 'Smart IoT & Industrial Automation Expo',
                'creator' => $rioIot,
                'warna' => '#2E7D32',
                'tipe' => 'besar',
                'banner_idx' => 4,
            ],
            [
                'judul' => 'Creative 2D Universe & Concept Art Gallery',
                'creator' => $kreatorDigital,
                'warna' => '#00838F',
                'tipe' => 'kecil',
                'banner_idx' => 5,
            ],
        ];

        $pameranModels = [];
        foreach ($pameranList as $pData) {
            $bannerFile = $bannerFiles[$pData['banner_idx'] % count($bannerFiles)];
            $pameran = Pameran::updateOrCreate(
                [
                    'judul' => $pData['judul'],
                ],
                [
                    'id_pengguna' => $pData['creator']->id_pengguna,
                    'id_model' => $modelHall->id_model,
                    'warna' => $pData['warna'],
                    'banner' => 'pameran/banner/' . $bannerFile,
                    'tipe' => $pData['tipe'],
                ]
            );

            Ruangan::firstOrCreate([
                'id_pameran' => $pameran->id_pameran,
                'jenis' => 'umum',
            ]);

            $pameranModels[$pData['judul']] = $pameran;
        }

        // 6. Definisi Karya Seni Lengkap berdasarkan Kategori & Pameran
        // Gunakan variasi timestamp created_at untuk menguji algoritma recency & kebaruan
        $karyaList = [
            // --- CYBERSECURITY & SOFTWARE (Pameran: Cyber Security Expo) ---
            [
                'judul' => 'Zero-Day Threat Defense Visualizer',
                'kategori' => 'Cybersecurity',
                'pameran' => 'Cyber Security Expo 2026: Zero Trust Frontier',
                'creator' => $alexCyber,
                'jenis' => 'poster',
                'deskripsi' => 'Visualisasi grafis arsitektur deteksi anomali zero-day malware berbasis model machine learning terdistribusi dengan sandbox virtual.',
                'file_idx' => 6,
                'days_ago' => 1,
            ],
            [
                'judul' => 'Cloud SOC Incident Management Platform',
                'kategori' => 'Cybersecurity',
                'pameran' => 'Cyber Security Expo 2026: Zero Trust Frontier',
                'creator' => $alexCyber,
                'jenis' => 'poster',
                'deskripsi' => 'Dashboard komprehensif Security Operations Center (SOC) untuk mitigasi serangan DDoS dan pemantauan lalu lintas enkripsi TLS 1.3.',
                'file_idx' => 7,
                'days_ago' => 2,
            ],
            [
                'judul' => 'End-to-End Encrypted Tunnel Network',
                'kategori' => 'Software',
                'pameran' => 'Cyber Security Expo 2026: Zero Trust Frontier',
                'creator' => $alexCyber,
                'jenis' => 'poster',
                'deskripsi' => 'Diagram alur protokol komunikasi peer-to-peer terenkripsi AES-256-GCM dengan handshake berbasis elliptic curve cryptography.',
                'file_idx' => 8,
                'days_ago' => 3,
            ],

            // --- UI/UX & 2D (Pameran: Virtual UI/UX Design Summit) ---
            [
                'judul' => 'Fintech Neo-Glass Mobile Banking Design System',
                'kategori' => 'UI/UX',
                'pameran' => 'Virtual UI/UX Design Summit & Showcase',
                'creator' => $sarahUiux,
                'jenis' => 'poster',
                'deskripsi' => 'Komponen antarmuka modern dengan gaya glassmorphism untuk aplikasi keuangan generasi baru, dilengkapi sistem aksesibilitas WCAG AAA.',
                'file_idx' => 9,
                'days_ago' => 1,
            ],
            [
                'judul' => 'SaaS Data Analytics & Pipeline Dashboard',
                'kategori' => 'UI/UX',
                'pameran' => 'Virtual UI/UX Design Summit & Showcase',
                'creator' => $sarahUiux,
                'jenis' => 'poster',
                'deskripsi' => 'Rancangan interaksi visual untuk platform pengolahan big data dengan filter hierarkis dan visualisasi grafik performa real-time.',
                'file_idx' => 10,
                'days_ago' => 2,
            ],
            [
                'judul' => 'Cyberpunk Vector Iconography & Glyph Set',
                'kategori' => '2D',
                'pameran' => 'Virtual UI/UX Design Summit & Showcase',
                'creator' => $sarahUiux,
                'jenis' => 'poster',
                'deskripsi' => 'Koleksi lebih dari 200 ikon vektor bergaya futuristik retro-wave yang didesain secara presisi untuk antarmuka game dan aplikasi.',
                'file_idx' => 11,
                'days_ago' => 4,
            ],

            // --- 3D, GAME DEV, & ANIMATION (Pameran: Next-Gen 3D World) ---
            [
                'judul' => 'Cyberpunk Low Poly Mech Robot 3D',
                'kategori' => '3D',
                'pameran' => 'Next-Gen 3D World & Game Arena',
                'creator' => $fajri,
                'jenis' => 'poster',
                'deskripsi' => 'Model robot tempur taktis 3D bergaya low poly dengan tekstur PBR lengkap, dioptimalkan untuk performa rendering engine mobile.',
                'file_idx' => 12,
                'days_ago' => 0.5,
            ],
            [
                'judul' => 'Procedural Sci-Fi Space Cruiser Modular 3D',
                'kategori' => '3D',
                'pameran' => 'Next-Gen 3D World & Game Arena',
                'creator' => $fajri,
                'jenis' => 'poster',
                'deskripsi' => 'Kapal induk penjelajah luar angkasa dengan detail interior modular dan sistem pencahayaan emissive dinamis.',
                'file_idx' => 13,
                'days_ago' => 1.5,
            ],
            [
                'judul' => 'Medieval Fortress Unreal Engine Environment',
                'kategori' => 'Game Dev',
                'pameran' => 'Next-Gen 3D World & Game Arena',
                'creator' => $fajri,
                'jenis' => 'poster',
                'deskripsi' => 'Level design lingkungan benteng kuno abad pertengahan dengan pencahayaan Lumen dan geometri Nanite pada Unreal Engine 5.',
                'file_idx' => 14,
                'days_ago' => 2.5,
            ],
            [
                'judul' => 'Stylized Character Rig & Action Combat Loop',
                'kategori' => 'Animation',
                'pameran' => 'Next-Gen 3D World & Game Arena',
                'creator' => $kreatorDigital,
                'jenis' => 'poster',
                'deskripsi' => 'Siklus animasi serangan kombo pertarungan karakter heroik dengan bobot gerakan realistis dan kurva timing dinamis.',
                'file_idx' => 15,
                'days_ago' => 3.5,
            ],
            [
                'judul' => 'Retro Roguelike Pixel Dungeon Prototype',
                'kategori' => 'Game Dev',
                'pameran' => 'Next-Gen 3D World & Game Arena',
                'creator' => $fajri,
                'jenis' => 'poster',
                'deskripsi' => 'Prototipe mekanisme gameplay procedural generation dungeon dengan sistem pertarungan turn-based klasik berbasis Unity engine.',
                'file_idx' => 16,
                'days_ago' => 4.5,
            ],

            // --- PHOTOGRAPHY & VIDEOGRAPHY (Pameran: Indie Digital Photography) ---
            [
                'judul' => 'Neon Rainy Night: Tokyo Cityscape',
                'kategori' => 'Photography',
                'pameran' => 'Indie Digital Photography & Visual Arts',
                'creator' => $dindaVisual,
                'jenis' => 'poster',
                'deskripsi' => 'Foto jalanan malam hari di Shibuya dengan pantulan cahaya neon pada aspal basah, menghasilkan kontras warna oranye dan teal yang dramatis.',
                'file_idx' => 17,
                'days_ago' => 1,
            ],
            [
                'judul' => 'Golden Hour Architectural Reflections',
                'kategori' => 'Photography',
                'pameran' => 'Indie Digital Photography & Visual Arts',
                'creator' => $dindaVisual,
                'jenis' => 'poster',
                'deskripsi' => 'Fotografi simetri gedung bertingkat kaca modern yang memantulkan siluet matahari terbenam dengan warna keemasan hangat.',
                'file_idx' => 18,
                'days_ago' => 2,
            ],
            [
                'judul' => 'Visual Motion Symphony: Fluid Particle Video',
                'kategori' => 'Videography',
                'pameran' => 'Indie Digital Photography & Visual Arts',
                'creator' => $dindaVisual,
                'jenis' => 'video',
                'deskripsi' => 'Eksperimen sinematik slow-motion 120fps menyorot pergerakan partikel zat cair warna-warni berpadu dengan ritme musik ambient.',
                'file_idx' => 19,
                'days_ago' => 3,
            ],
            [
                'judul' => 'Cinematic Drone Journey Across Java Highlands',
                'kategori' => 'Videography',
                'pameran' => 'Indie Digital Photography & Visual Arts',
                'creator' => $dindaVisual,
                'jenis' => 'video',
                'deskripsi' => 'Kompilasi rekaman udara lanskap pegunungan berkabut dan perkebunan teh di pagi hari dengan color grading sinematik 4K.',
                'file_idx' => 20,
                'days_ago' => 5,
            ],

            // --- IOT, AUTOMATION, FABRICATION, & MANUFACTURING (Pameran: Smart IoT) ---
            [
                'judul' => 'Smart Agriculture LoRaWAN Sensor Node',
                'kategori' => 'Internet Of Things',
                'pameran' => 'Smart IoT & Industrial Automation Expo',
                'creator' => $rioIot,
                'jenis' => 'poster',
                'deskripsi' => 'Perangkat pemantau kelembaban tanah, suhu lingkungan, dan intensitas cahaya matahari berdaya rendah dengan transmisi jarak jauh LoRaWAN.',
                'file_idx' => 21,
                'days_ago' => 1,
            ],
            [
                'judul' => 'Industrial SCADA Robotic Arm Telemetry',
                'kategori' => 'Automation System',
                'pameran' => 'Smart IoT & Industrial Automation Expo',
                'creator' => $rioIot,
                'jenis' => 'poster',
                'deskripsi' => 'Sistem pemantauan posisi motor servo dan beban torsi lengan robot manufaktur 6-axis secara real-time via MQTT gateway.',
                'file_idx' => 22,
                'days_ago' => 2,
            ],
            [
                'judul' => 'Precision CNC Milling Toolpath Simulator',
                'kategori' => 'Manufacturing',
                'pameran' => 'Smart IoT & Industrial Automation Expo',
                'creator' => $rioIot,
                'jenis' => 'poster',
                'deskripsi' => 'Simulasi alur potong mata pahat mesin CNC 5-axis untuk fabrikasi komponen aluminium pesawat terbang berakurasi mikron.',
                'file_idx' => 23,
                'days_ago' => 3,
            ],
            [
                'judul' => 'Carbon Fiber Aerodynamic Drone Airframe',
                'kategori' => 'Fabrication',
                'pameran' => 'Smart IoT & Industrial Automation Expo',
                'creator' => $rioIot,
                'jenis' => 'poster',
                'deskripsi' => 'Rangka drone surveillance tahan benturan berbasis serat karbon komposit dengan cetakan vacuum infusion berbobot ultra-ringan.',
                'file_idx' => 24,
                'days_ago' => 4,
            ],

            // --- 2D & OTHERS (Pameran: Creative 2D Universe) ---
            [
                'judul' => 'Floating Archipelago: Fantasy Concept Art',
                'kategori' => '2D',
                'pameran' => 'Creative 2D Universe & Concept Art Gallery',
                'creator' => $kreatorDigital,
                'jenis' => 'poster',
                'deskripsi' => 'Lukisan digital konsep pulau melayang di angkasa dengan air terjun abadi dan peradaban kuno yang tenang.',
                'file_idx' => 25,
                'days_ago' => 1,
            ],
            [
                'judul' => 'Lofi Chill Hop Animated Loop Scene',
                'kategori' => 'Animation',
                'pameran' => 'Creative 2D Universe & Concept Art Gallery',
                'creator' => $kreatorDigital,
                'jenis' => 'video',
                'deskripsi' => 'Animasi siklus pendek pemandangan kamar saat hujan di sore hari dengan suasana hangat dan karakter sedang membaca buku.',
                'file_idx' => 26,
                'days_ago' => 2,
            ],
            [
                'judul' => 'Generative Audio-Visual Reactive Canvas',
                'kategori' => 'Others',
                'pameran' => 'Creative 2D Universe & Concept Art Gallery',
                'creator' => $kreatorDigital,
                'jenis' => 'poster',
                'deskripsi' => 'Seni generatif berbasis algoritma rekursif matematika Fourier transform yang merespons input frekuensi gelombang suara.',
                'file_idx' => 27,
                'days_ago' => 3,
            ],
        ];

        // 7. Simpan Karya ke Database
        foreach ($karyaList as $item) {
            $catId = $kategoriMap[$item['kategori']] ?? 1;
            $pameranId = $pameranModels[$item['pameran']]->id_pameran ?? null;
            $fileImage = $bannerFiles[$item['file_idx'] % count($bannerFiles)];
            $createdAt = Carbon::now()->subDays($item['days_ago'] ?? 0);

            Karya::updateOrCreate(
                [
                    'judul' => $item['judul'],
                ],
                [
                    'id_kategori' => $catId,
                    'id_pengguna' => $item['creator']->id_pengguna,
                    'id_pameran' => $pameranId,
                    'jenis' => $item['jenis'],
                    'deskripsi' => $item['deskripsi'],
                    'file' => 'karya/' . $fileImage,
                    'created_at' => $createdAt,
                    'updated_at' => $createdAt,
                ]
            );
        }

        $this->command?->info('Berhasil menginisialisasi ' . count($pameranList) . ' pameran tematik dan ' . count($karyaList) . ' karya seni!');
    }
}
