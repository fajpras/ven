<?php

namespace App\Http\Controllers\Pengguna;

use App\Http\Controllers\Controller;
use App\Models\Model3D;
use App\Models\Pameran;
use Illuminate\Http\Request;
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
            ->when($request->boolean('milikku'), fn ($q) => $q->where('id_pengguna', $request->user()->id_pengguna))
            ->latest('id_pameran')
            ->paginate(24);

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