<?php

namespace App\Http\Controllers;

use App\Models\Kategori;
use App\Models\Pengguna;
use Illuminate\Http\Request;

class PenggunaKategoriController extends Controller
{
    public function show(Request $request)
    {
        /** @var Pengguna $pengguna */
        $pengguna = $request->user();

        return $this->ok($pengguna->kategori()->orderByPivot('bobot', 'desc')->get());
    }

    public function update(Request $request)
    {
        $validated = $request->validate([
            'kategori' => ['required', 'array', 'min:1'],
            'kategori.*.id_kategori' => ['required', 'integer', 'exists:kategori,id_kategori'],
            'kategori.*.bobot' => ['required', 'numeric', 'min:0', 'max:1'],
        ]);

        /** @var Pengguna $pengguna */
        $pengguna = $request->user();

        $sync = collect($validated['kategori'])
            ->keyBy('id_kategori')
            ->map(fn ($item) => ['bobot' => $item['bobot']])
            ->all();

        $pengguna->kategori()->sync($sync);

        return $this->ok($pengguna->kategori()->orderByPivot('bobot', 'desc')->get(), 'Minat kategori disimpan.');
    }

    public function destroy(Request $request, Kategori $kategori)
    {
        /** @var Pengguna $pengguna */
        $pengguna = $request->user();

        $pengguna->kategori()->detach($kategori->id_kategori);

        return $this->ok(null, 'Minat kategori dihapus.');
    }
}
