<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Karya;
use Illuminate\Http\Request;

class KaryaController extends Controller
{
    public function index(Request $request)
    {
        $karya = Karya::with(['kategori', 'pengguna', 'pameran'])
            ->withTrashed()
            ->when($request->integer('id_kategori'), fn ($q, $v) => $q->where('id_kategori', $v))
            ->when($request->string('judul'), fn ($q, $v) => $q->where('judul', 'like', "%{$v}%"))
            ->when($request->boolean('trashed'), fn ($q) => $q->onlyTrashed())
            ->latest('created_at')
            ->paginate(24);

        return $this->ok($karya);
    }

    public function show(Request $request, string $id)
    {
        return $this->ok(Karya::withTrashed()->findOrFail($id)->load(['kategori', 'pengguna', 'pameran', 'objek']));
    }

    public function destroy(Request $request, Karya $karya)
    {
        $karya->delete();

        return $this->ok(null, 'Karya dimoderasi (dihapus).');
    }
}
