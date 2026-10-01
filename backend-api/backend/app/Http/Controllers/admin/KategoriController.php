<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Kategori;
use Illuminate\Http\Request;

class KategoriController extends Controller
{
    public function index(Request $request)
    {
        $kategori = Kategori::withCount('karya')
            ->when($request->string('q'), fn ($q, $v) => $q->where('kategori', 'like', "%{$v}%"))
            ->orderBy('id_kategori')
            ->paginate(100);

        return $this->ok($kategori);
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'kategori' => ['required', 'string', 'max:255', 'unique:kategori,kategori'],
        ]);

        return $this->created(Kategori::create($validated), 'Kategori dibuat.');
    }

    public function show(Request $request, Kategori $kategori)
    {
        return $this->ok($kategori->loadCount('karya'));
    }

    public function update(Request $request, Kategori $kategori)
    {
        $validated = $request->validate([
            'kategori' => ['sometimes', 'string', 'max:255', 'unique:kategori,kategori,'.$kategori->id_kategori.',id_kategori'],
        ]);

        $kategori->forceFill($validated)->save();

        return $this->ok($kategori, 'Kategori diperbarui.');
    }

    public function destroy(Request $request, Kategori $kategori)
    {
        $kategori->delete();

        return $this->ok(null, 'Kategori dihapus.');
    }
}
