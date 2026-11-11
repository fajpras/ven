<?php

namespace App\Http\Controllers\Pengguna;

use App\Http\Controllers\Controller;
use App\Models\Model3D;
use App\Models\Pameran;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Cache;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Storage;
use Illuminate\Validation\Rule;

class PameranController extends Controller
{
    private const PENGGUNA_PUBLIK = 'pengguna:id_pengguna,nama,nama_panggilan,foto_profil';

    public function index(Request $request)
    {
        $request->validate([
            'judul' => ['nullable', 'string', 'max:100'],
            'tipe' => ['nullable', Rule::in(['kecil', 'sedang', 'besar', 'acara'])],
        ]);

        $pameran = Pameran::with(['model3d', self::PENGGUNA_PUBLIK])
            ->when($request->filled('judul'), fn ($q) => $q->where('judul', 'like', '%'.$this->escapeLike($request->query('judul')).'%'))
            ->when($request->filled('tipe'), fn ($q) => $q->where('tipe', $request->query('tipe')))
            ->when($request->filled('pengguna'), fn ($q) => $q->where('id_pengguna', $request->query('pengguna')))
            ->when($request->boolean('milikku'), fn ($q) => $q->where('id_pengguna', $request->user()->id_pengguna))
            ->latest('id_pameran')
            ->paginate(24);

        return $this->ok($pameran);
    }

    public function rekomendasi(Request $request)
    {
        $user = $request->user();
        $page = $request->integer('page', 1);
        $perPage = min($request->integer('per_page', 8), 16);

        $userCategories = $user ? $user->kategori()->get() : collect();
        $hasOthers = $userCategories->contains(fn ($k) => strcasecmp($k->kategori, 'others') === 0);

        $query = Pameran::query()
            ->with(['model3d', self::PENGGUNA_PUBLIK, 'karya.kategori']);

        if ($user && $user->kategori()->exists() && ! $hasOthers) {
            $subqueryBobot = DB::table('karya')
                ->join('pengguna_kategori', function ($join) use ($user) {
                    $join->on('karya.id_kategori', '=', 'pengguna_kategori.id_kategori')
                        ->where('pengguna_kategori.id_pengguna', '=', $user->id_pengguna);
                })
                ->whereColumn('karya.id_pameran', 'pameran.id_pameran')
                ->selectRaw('COALESCE(SUM(pengguna_kategori.bobot), 0)');

            $query->orderByDesc($subqueryBobot);
        }

        $pameran = $query->latest('pameran.id_pameran')->paginate($perPage);

        return $this->ok($pameran);
    }

    public function show(Pameran $pameran)
    {
        return $this->ok($pameran->load([
            'model3d', self::PENGGUNA_PUBLIK, 'ruangan',
            'karya.kategori', 'karya.objek.posisi', 'karya.objek.model3d',
        ]));
    }

    public function store(Request $request)
    {
        $validated = $request->validate($this->rules(true));

        return DB::transaction(function () use ($request, $validated) {
            $path = null;

            if ($request->hasFile('banner')) {
                $path = $request->file('banner')->store('pameran/banner', 'public');
                $validated['banner'] = Storage::url($path);
            }

            try {
                $pameran = $request->user()->pameran()->create($validated);
                return $this->created($pameran->load('model3d'), 'Pameran dibuat.');
            } catch (\Throwable $e) {
                if ($path) {
                    Storage::disk('public')->delete($path);
                }
                throw $e;
            }
        });
    }

    public function update(Request $request, Pameran $pameran)
    {
        $this->pastikanPemilik($request, $pameran->id_pengguna);

        $validated = $request->validate($this->rules(false));

        return DB::transaction(function () use ($request, $pameran, $validated) {
            $newPath = null;

            if ($request->hasFile('banner')) {
                $newPath = $request->file('banner')->store('pameran/banner', 'public');
                $validated['banner'] = Storage::url($newPath);
            }

            try {
                $oldBanner = $pameran->banner;
                $pameran->fill($validated)->save();

                if ($newPath && $oldBanner) {
                    $oldPath = str_replace('/storage/', '', parse_url($oldBanner, PHP_URL_PATH));
                    Storage::disk('public')->delete($oldPath);
                }

                return $this->ok($pameran->load('model3d'), 'Pameran diperbarui.');
            } catch (\Throwable $e) {
                if ($newPath) {
                    Storage::disk('public')->delete($newPath);
                }
                throw $e;
            }
        });
    }

    public function destroy(Request $request, Pameran $pameran)
    {
        $this->pastikanPemilik($request, $pameran->id_pengguna);

        return DB::transaction(function () use ($pameran) {
            $bannerUrl = $pameran->banner;
            $pameran->delete();

            if ($bannerUrl) {
                $oldPath = str_replace('/storage/', '', parse_url($bannerUrl, PHP_URL_PATH));
                Storage::disk('public')->delete($oldPath);
            }

            return $this->ok(null, 'Pameran dihapus.');
        });
    }

    public function storeRuangan(Request $request, Pameran $pameran)
    {
        $this->pastikanPemilik($request, $pameran->id_pengguna);

        $validated = $request->validate(['jenis' => ['required', Rule::in(['privat', 'umum'])]]);

        return $this->created($pameran->ruangan()->create($validated), 'Ruangan dibuat.');
    }

    public function templates(Request $request)
    {
        $model = Model3D::query()
            ->when($request->enum('jenis', ['hall', 'objek']), fn ($q, $v) => $q->where('jenis', $v))
            ->orderBy('jenis')
            ->orderBy('id_model')
            ->paginate(24);

        return $this->ok($model);
    }

    public function templateShow(Model3D $model3d)
    {
        return $this->ok($model3d->load('objek'));
    }

    public function uploadBanner(Request $request)
    {
        $request->validate([
            'banner' => ['required', 'file', 'image', 'mimes:jpeg,jpg,png,webp', 'max:2048'],
        ]);

        $path = $request->file('banner')->store('pameran/banner', 'public');

        return $this->ok([
            'url' => Storage::url($path),
            'path' => $path,
        ], 'Banner berhasil diunggah.');
    }

    public function storeFromUnity(Request $request)
    {
        $validated = $request->validate([
            'templateId' => ['nullable', 'string'],
            'title' => ['required', 'string', 'max:100'],
            'description' => ['nullable', 'string'],
            'banner' => ['nullable', 'string'],
            'wallColor' => ['nullable', 'string', 'max:10'],
            'items' => ['nullable', 'array'],
        ]);

        // 1. Validasi kata kasar (profanity check)
        if (\App\Services\ProfanityFilter::containsProfanity($validated['title'], $validated['description'] ?? '')) {
            return response()->json([
                'message' => 'mengandung kata kasar',
            ], 422);
        }

        // 2. Normalisasi & validasi batas kapasitas template
        $rawTemplateId = strtolower(trim($validated['templateId'] ?? 'template01'));
        $templatesConfig = config('exhibition_templates.templates', []);

        $matchedConfig = null;
        $canonicalKey = 'template01';

        foreach ($templatesConfig as $key => $cfg) {
            $aliases = array_map('strtolower', $cfg['aliases'] ?? []);
            if ($rawTemplateId === strtolower($key) || in_array($rawTemplateId, $aliases, true)) {
                $matchedConfig = $cfg;
                $canonicalKey = $key;
                break;
            }
        }

        if (!$matchedConfig) {
            $matchedConfig = $templatesConfig['template01'] ?? [
                'name' => 'Small',
                'max_panels' => 9,
                'max_objects' => 9,
                'tipe' => 'kecil',
            ];
        }

        $tipe = $matchedConfig['tipe'] ?? 'kecil';
        $maxPanels = (int) ($matchedConfig['max_panels'] ?? 9);
        $maxObjects = (int) ($matchedConfig['max_objects'] ?? 9);

        $items = $validated['items'] ?? [];
        $panelCount = 0;
        $objectCount = 0;

        foreach ($items as $item) {
            $surface = strtolower($item['surface'] ?? '');
            $prefabId = strtolower($item['prefabId'] ?? '');
            $isPanel = ($surface === 'wall') || str_contains($prefabId, 'panel') || str_contains($prefabId, 'poster') || str_contains($prefabId, 'frame');

            if ($isPanel) {
                $panelCount++;
            } else {
                $objectCount++;
            }
        }

        if ($panelCount > $maxPanels) {
            return response()->json([
                'message' => 'panel melebihi jumlah maksimal',
            ], 422);
        }

        if ($objectCount > $maxObjects) {
            return response()->json([
                'message' => 'objek melebihi jumlah maksimal',
            ], 422);
        }

        $pameran = DB::transaction(function () use ($request, $validated, $tipe, $canonicalKey, $items) {
            $user = $request->user();

            $hallModel = Model3D::firstOrCreate(
                ['nama' => 'Hall Template ' . ucfirst($tipe)],
                [
                    'jenis' => 'hall',
                    'file' => 'models/' . $canonicalKey . '.glb',
                    'kapasitas' => (string) match ($tipe) {
                        'sedang' => '14',
                        'besar' => '21',
                        'acara' => '60',
                        default => '9',
                    },
                    'gambar' => 'models/preview.png',
                ]
            );

            $banner = !empty($validated['banner']) ? $validated['banner'] : 'pameran/banner/virtual_art_banner.jpg';

            $pameran = $user->pameran()->create([
                'id_model' => $hallModel->id_model,
                'judul' => $validated['title'],
                'banner' => $banner,
                'warna' => substr($validated['wallColor'] ?? '#1565C0', 0, 7),
                'tipe' => $tipe,
            ]);

            $ruangan = $pameran->ruangan()->create([
                'jenis' => 'umum',
            ]);

            $defaultCategory = \App\Models\Kategori::first();

            if (!empty($items)) {
                foreach ($items as $item) {
                    $posisi = \App\Models\PosisiObjek::create([
                        'posisi_x' => (float) ($item['x'] ?? 0.0),
                        'posisi_y' => (float) ($item['y'] ?? 0.0),
                        'posisi_z' => (float) ($item['z'] ?? 0.0),
                        'rotasi_z' => (float) ($item['rotY'] ?? 0.0),
                    ]);

                    $objekModel = Model3D::firstOrCreate(
                        ['nama' => $item['prefabId'] ?? 'Artwork Panel'],
                        [
                            'jenis' => 'objek',
                            'file' => 'models/' . ($item['prefabId'] ?? 'panel') . '.glb',
                            'kapasitas' => '1',
                            'gambar' => 'models/panel_preview.png',
                        ]
                    );

                    $itemSurface = strtolower($item['surface'] ?? '');
                    $itemPrefab = strtolower($item['prefabId'] ?? '');
                    $isItemPanel = ($itemSurface === 'wall') || str_contains($itemPrefab, 'panel') || str_contains($itemPrefab, 'poster') || str_contains($itemPrefab, 'frame');

                    $objek = \App\Models\Objek::create([
                        'id_posisi' => $posisi->id_posisi,
                        'id_model' => $objekModel->id_model,
                        'jenis' => $isItemPanel ? 'panel' : 'aset',
                    ]);

                    if (!empty($item['contentUrl'])) {
                        \App\Models\Karya::create([
                            'id_kategori' => $defaultCategory?->id_kategori ?? 1,
                            'id_pengguna' => $user->id_pengguna,
                            'id_pameran' => $pameran->id_pameran,
                            'id_objek' => $objek->id_objek,
                            'judul' => $validated['title'] . ' - Karya Panel',
                            'jenis' => !empty($item['isVideo']) ? 'video' : 'poster',
                            'deskripsi' => $validated['description'] ?? 'Karya dalam pameran virtual',
                            'file' => $item['contentUrl'],
                        ]);
                    }
                }
            }

            return $pameran;
        });

        // 3. Catat aktivitas ke LogKeamanan SETELAH commit selesai
        \App\Services\LogSistem::keamanan(
            event: 'pameran_dipublikasi',
            pesan: 'Pameran berhasil dipublikasi',
            statusKode: 200,
            idPengguna: $request->user()->id_pengguna
        );

        return $this->ok([
            'id_pameran' => $pameran->id_pameran,
            'pameran' => $pameran->load(['ruangan', 'model3d']),
        ], 'pameran berhasil dipublikasi');
    }

    public function roomDto(Pameran $pameran, Request $request)
    {
        $pameran->load([
            'model3d', 'ruangan',
            'karya.kategori', 'karya.objek.posisi', 'karya.objek.model3d',
        ]);

        $items = [];
        foreach ($pameran->karya as $karya) {
            $posisi = $karya->objek?->posisi;
            $items[] = [
                'prefabId' => $karya->objek?->model3d?->nama ?? 'panel',
                'position' => [
                    'x' => (float) ($posisi?->posisi_x ?? 0.0),
                    'y' => (float) ($posisi?->posisi_y ?? 0.0),
                    'z' => (float) ($posisi?->posisi_z ?? 0.0),
                ],
                'rotation' => [
                    'x' => 0.0,
                    'y' => (float) ($posisi?->rotasi_z ?? 0.0),
                    'z' => 0.0,
                ],
                'contentType' => $karya->jenis,
                'contentUrl' => $karya->file,
            ];
        }

        $dto = [
            'roomId' => (string) $pameran->id_pameran,
            'templateId' => $pameran->model3d?->file ? basename($pameran->model3d->file, '.glb') : 'tpl_small',
            'title' => $pameran->judul,
            'description' => 'Pameran Virtual 3D',
            'wallColor' => $pameran->warna ?? '#1565C0',
            'isPublic' => true,
            'ownerId' => (string) $pameran->id_pengguna,
            'viewerId' => (string) ($request->user()?->id_pengguna ?? ''),
            'items' => $items,
        ];

        return response()->json($dto);
    }

    private function rules(bool $wajib): array
    {
        $r = $wajib ? 'required' : 'sometimes';

        return [
            'judul' => [$r, 'string', 'max:255'],
            'banner' => [$r, 'file', 'image', 'mimes:jpeg,jpg,png,webp', 'max:2048'],
            'tipe' => [$r, Rule::in(['kecil', 'sedang', 'besar', 'acara'])],
            'id_model' => [$r, 'integer', Rule::exists('model', 'id_model')->where('jenis', 'hall')],
        ];
    }
}