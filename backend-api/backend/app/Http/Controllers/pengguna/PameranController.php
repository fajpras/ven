<?php

namespace App\Http\Controllers\Pengguna;

use App\Http\Controllers\Controller;
use App\Models\Model3D;
use App\Models\Pameran;
use Illuminate\Http\Request;

class PameranController extends Controller
{
    public function index(Request $request)
    {
        $pameran = Pameran::with(['model3d', 'pengguna'])
            ->when($request->string('judul'), fn ($q, $v) => $q->where('judul', 'like', "%{$v}%"))
            ->when($request->enum('tipe', ['kecil', 'sedang', 'besar', 'acara']), fn ($q, $v) => $q->where('tipe', $v))
            ->latest('id_pameran')
            ->paginate(24);

        return $this->ok($pameran);
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'judul' => ['required', 'string', 'max:255'],
            'banner' => ['required', 'string', 'max:255'],
            'tipe' => ['required', 'string', 'in:kecil,sedang,besar,acara'],
            'id_model' => ['required', 'integer', 'exists:model,id_model'],
        ]);

        Model3D::where('id_model', $validated['id_model'])
            ->where('jenis', 'hall')
            ->firstOrFail();

        $pameran = Pameran::create([
            'id_pengguna' => $request->user()->id_pengguna,
            ...$validated,
        ]);

        return $this->created($pameran, 'Pameran dibuat.');
    }

    public function show(Request $request, Pameran $pameran)
    {
        return $this->ok($pameran->load(['model3d', 'pengguna', 'ruangan', 'karya']));
    }

    public function update(Request $request, Pameran $pameran)
    {
        $this->requireOwner($request, $pameran);

        $validated = $request->validate([
            'judul' => ['sometimes', 'string', 'max:255'],
            'banner' => ['sometimes', 'string', 'max:255'],
            'tipe' => ['sometimes', 'string', 'in:kecil,sedang,besar,acara'],
            'id_model' => ['sometimes', 'integer', 'exists:model,id_model'],
        ]);

        if (isset($validated['id_model'])) {
            Model3D::where('id_model', $validated['id_model'])
                ->where('jenis', 'hall')
                ->firstOrFail();
        }

        $pameran->forceFill($validated)->save();

        return $this->ok($pameran, 'Pameran diperbarui.');
    }

    public function destroy(Request $request, Pameran $pameran)
    {
        $this->requireOwner($request, $pameran);

        $pameran->delete();

        return $this->ok(null, 'Pameran dihapus.');
    }

    public function storeRuangan(Request $request, Pameran $pameran)
    {
        $this->requireOwner($request, $pameran);

        $validated = $request->validate([
            'jenis' => ['required', 'string', 'in:privat,umum'],
        ]);

        $ruangan = $pameran->ruangan()->create($validated);

        return $this->created($ruangan, 'Ruangan dibuat.');
    }

    private function requireOwner(Request $request, Pameran $pameran): void
    {
        if ($pameran->id_pengguna !== $request->user()->id_pengguna) {
            abort(403, 'Anda bukan pemilik pameran ini.');
        }
    }
}
