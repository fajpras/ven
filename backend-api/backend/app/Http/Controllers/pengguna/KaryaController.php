<?php

namespace App\Http\Controllers\Pengguna;

use App\Http\Controllers\Controller;
use App\Models\Karya;
use App\Models\Objek;
use App\Models\PosisiObjek;
use Illuminate\Http\Request;
use Illuminate\Support\Arr;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Storage;
use Illuminate\Validation\Rule;
use Throwable;

class KaryaController extends Controller
{
    private const PENGGUNA_PUBLIK = 'pengguna:id_pengguna,nama,nama_panggilan,foto_profil';

    public function index(Request $request)
    {
        $request->validate([
            'id_kategori' => ['nullable', 'integer'],
            'id_pameran' => ['nullable', 'integer'],
            'jenis' => ['nullable', Rule::in(['poster', 'video'])],
            'judul' => ['nullable', 'string', 'max:100'],
        ]);

        $karya = Karya::with(['kategori', self::PENGGUNA_PUBLIK])
            ->when($request->filled('id_kategori'), fn ($q) => $q->where('id_kategori', $request->integer('id_kategori')))
            ->when($request->filled('id_pameran'), fn ($q) => $q->where('id_pameran', $request->integer('id_pameran')))
            ->when($request->filled('jenis'), fn ($q) => $q->where('jenis', $request->query('jenis')))
            ->when($request->filled('judul'), fn ($q) => $q->where('judul', 'like', '%'.$this->escapeLike($request->query('judul')).'%'))
            ->when($request->boolean('milikku'), fn ($q) => $q->where('id_pengguna', $request->user()->id_pengguna))
            ->latest('created_at')
            ->paginate(24);

        return $this->ok($karya);
    }

    public function rekomendasi(Request $request)
    {
        $user = $request->user();

        $query = Karya::query()->select('karya.*')->with(['kategori', self::PENGGUNA_PUBLIK]);

        if ($user->kategori()->exists()) {
            $query->join('pengguna_kategori', fn ($join) => $join
                ->on('karya.id_kategori', '=', 'pengguna_kategori.id_kategori')
                ->where('pengguna_kategori.id_pengguna', '=', $user->id_pengguna))
                ->orderByDesc('pengguna_kategori.bobot');
        }

        return $this->ok($query->latest('karya.created_at')->paginate(24));
    }

    public function show(Karya $karya)
    {
        return $this->ok($karya->load(['kategori', self::PENGGUNA_PUBLIK, 'pameran', 'objek.posisi', 'objek.model3d']));
    }

    public function store(Request $request)
    {
        $user = $request->user();
        $validated = $request->validate($this->rules($request, true));

        $path = $request->file('file')->store('karya', 'local');

        try {
            $karya = DB::transaction(function () use ($validated, $path, $user) {
                $data = Arr::except($validated, ['file', 'objek']) + ['file' => $path];

                if (! empty($validated['objek'])) {
                    $data['id_objek'] = Objek::forceCreate($validated['objek'])->id_objek;
                }

                return $user->karya()->create($data);
            });
        } catch (Throwable $e) {
            Storage::disk('local')->delete($path);
            throw $e;
        }

        return $this->created($karya->load(['kategori', 'objek']), 'Karya dibuat.');
    }

    public function update(Request $request, Karya $karya)
    {
        $this->pastikanPemilik($request, $karya->id_pengguna);

        $validated = $request->validate($this->rules($request, false));
        $pathLama = null;
        $pathBaru = null;

        if ($request->hasFile('file')) {
            $pathLama = $karya->file;
            $pathBaru = $request->file('file')->store('karya', 'local');
        }

        try {
            DB::transaction(function () use ($validated, $karya, $pathBaru) {
                $data = Arr::except($validated, ['file', 'objek']);

                if ($pathBaru) {
                    $data['file'] = $pathBaru;
                }

                if (! empty($validated['objek'])) {
                    if ($karya->objek) {
                        $karya->objek->forceFill($validated['objek'])->save();
                    } else {
                        $data['id_objek'] = Objek::forceCreate($validated['objek'])->id_objek;
                    }
                }

                $karya->fill($data)->save();
            });
        } catch (Throwable $e) {
            if ($pathBaru) {
                Storage::disk('local')->delete($pathBaru);
            }
            throw $e;
        }

        if ($pathLama) {
            Storage::disk('local')->delete($pathLama);
        }

        return $this->ok($karya->fresh(['kategori', 'objek.posisi']), 'Karya diperbarui.');
    }

    public function destroy(Request $request, Karya $karya)
    {
        $this->pastikanPemilik($request, $karya->id_pengguna);

        $karya->delete();

        return $this->ok(null, 'Karya dihapus.');
    }

    public function updatePosisiObjek(Request $request, Karya $karya)
    {
        $pemilikPameran = $karya->pameran?->id_pengguna;
        $user = $request->user();

        if (! hash_equals((string) $karya->id_pengguna, $user->id_pengguna)
            && ! ($pemilikPameran && hash_equals($pemilikPameran, $user->id_pengguna))) {
            abort(403, 'Anda tidak berhak mengatur posisi objek ini.');
        }

        $objek = $karya->objek;

        if (! $objek) {
            return $this->error('Karya ini belum memiliki objek 3D.', 422);
        }

        $angka = ['required', 'numeric', 'between:-100000,100000'];
        $validated = $request->validate([
            'posisi_x' => $angka, 'posisi_y' => $angka, 'posisi_z' => $angka,
            'rotasi_x' => $angka, 'rotasi_y' => $angka, 'rotasi_z' => $angka,
        ]);

        DB::transaction(function () use ($objek, $validated) {
            $lama = $objek->posisi;
            $baru = PosisiObjek::forceCreate($validated);

            $objek->forceFill(['id_posisi' => $baru->id_posisi])->save();
            $lama?->delete();
        });

        return $this->ok($objek->fresh()->load('posisi'), 'Posisi objek diperbarui.');
    }

    public function berkas(Karya $karya)
    {
        abort_unless($karya->file && Storage::disk('local')->exists($karya->file), 404);

        return Storage::disk('local')->response($karya->file, null, [
            'X-Content-Type-Options' => 'nosniff',
            'Cache-Control' => 'private, max-age=1800',
        ]);
    }

    private function rules(Request $request, bool $wajib): array
    {
        $r = $wajib ? 'required' : 'sometimes';
        $jenis = $request->input('jenis', $request->route('karya')?->jenis);

        return [
            'id_kategori' => [$r, 'integer', 'exists:kategori,id_kategori'],

            'id_pameran' => ['nullable', 'integer', Rule::exists('pameran', 'id_pameran')
                ->where('id_pengguna', $request->user()->id_pengguna)
                ->whereNull('deleted_at')],
            'judul' => [$r, 'string', 'max:255'],
            'jenis' => [$r, Rule::in(['poster', 'video'])],
            'deskripsi' => [$r, 'string', 'max:5000'],
            'file' => [$r, 'file', 'max:51200', $jenis === 'video'
                ? 'mimetypes:video/mp4,video/webm,video/quicktime'
                : 'mimes:jpg,jpeg,png,webp'],
            'objek' => ['sometimes', 'array'],
            'objek.id_model' => ['required_with:objek', 'integer', Rule::exists('model', 'id_model')->where('jenis', 'objek')],
            'objek.jenis' => ['required_with:objek', Rule::in(['panel', 'aset'])],
        ];
    }
}
