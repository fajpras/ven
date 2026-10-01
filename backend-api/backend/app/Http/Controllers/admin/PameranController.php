<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Pameran;
use Illuminate\Http\Request;

class PameranController extends Controller
{
    public function index(Request $request)
    {
        $pameran = Pameran::with(['pengguna', 'model3d'])
            ->withTrashed()
            ->when($request->string('judul'), fn ($q, $v) => $q->where('judul', 'like', "%{$v}%"))
            ->when($request->boolean('trashed'), fn ($q) => $q->onlyTrashed())
            ->latest('id_pameran')
            ->paginate(24);

        return $this->ok($pameran);
    }

    public function show(Request $request, string $id)
    {
        $pameran = Pameran::withTrashed()->findOrFail($id);

        return $this->ok($pameran->load(['pengguna', 'model3d', 'ruangan', 'karya']));
    }

    public function destroy(Request $request, Pameran $pameran)
    {
        $pameran->delete();

        return $this->ok(null, 'Pameran dimoderasi (dihapus).');
    }
}
