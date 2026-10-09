<?php

namespace App\Http\Controllers\Pengguna;

use App\Http\Controllers\Controller;
use App\Models\Model3D;
use App\Models\Pameran;
use Illuminate\Http\Request;
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

        $pameran = $request->user()->pameran()->create($validated);

        return $this->created($pameran->load('model3d'), 'Pameran dibuat.');
    }

    public function update(Request $request, Pameran $pameran)
    {
        $this->pastikanPemilik($request, $pameran->id_pengguna);

        $pameran->fill($request->validate($this->rules(false)))->save();

        return $this->ok($pameran->load('model3d'), 'Pameran diperbarui.');
    }

    public function destroy(Request $request, Pameran $pameran)
    {
        $this->pastikanPemilik($request, $pameran->id_pengguna);

        $pameran->delete();

        return $this->ok(null, 'Pameran dihapus.');
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
            'banner' => [$r, 'url:https', 'max:255'],
            'tipe' => [$r, Rule::in(['kecil', 'sedang', 'besar', 'acara'])],
            'id_model' => [$r, 'integer', Rule::exists('model', 'id_model')->where('jenis', 'hall')],
        ];
    }
}
