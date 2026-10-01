<?php

namespace App\Http\Controllers;

use App\Models\Model3D;
use App\Models\Objek;
use Illuminate\Http\Request;

class ObjekController extends Controller
{
    public function index(Request $request)
    {
        $objek = Objek::with(['model3d', 'posisi'])
            ->when($request->integer('id_pameran'), fn ($q, $v) => $q->whereHas(
                'karya',
                fn ($sub) => $sub->where('id_pameran', $v),
            ))
            ->orderBy('id_objek')
            ->paginate(100);

        return $this->ok($objek);
    }

    public function show(Request $request, Objek $objek)
    {
        return $this->ok($objek->load(['model3d', 'posisi', 'karya']));
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'id_model' => ['required', 'integer', 'exists:model,id_model'],
            'jenis' => ['required', 'string', 'in:panel,aset'],
        ]);

        Model3D::where('id_model', $validated['id_model'])
            ->where('jenis', 'objek')
            ->firstOrFail();

        $objek = Objek::create($validated);

        return $this->created($objek, 'Objek dibuat.');
    }

    public function update(Request $request, Objek $objek)
    {
        $validated = $request->validate([
            'id_model' => ['sometimes', 'integer', 'exists:model,id_model'],
            'jenis' => ['sometimes', 'string', 'in:panel,aset'],
        ]);

        if (isset($validated['id_model'])) {
            Model3D::where('id_model', $validated['id_model'])
                ->where('jenis', 'objek')
                ->firstOrFail();
        }

        $objek->forceFill($validated)->save();

        return $this->ok($objek, 'Objek diperbarui.');
    }

    public function destroy(Request $request, Objek $objek)
    {
        $objek->delete();

        return $this->ok(null, 'Objek dihapus.');
    }
}
