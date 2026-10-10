<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Karya;
use App\Models\Pameran;
use Illuminate\Http\Request;

class ModerasiController extends Controller
{
    public function karyaIndex(Request $request)
    {
        $request->validate([
            'id_kategori' => ['nullable', 'integer'],
            'judul' => ['nullable', 'string', 'max:100'],
        ]);

        $karya = Karya::with(['kategori', 'pengguna', 'pameran'])
            ->withTrashed()
            ->when($request->filled('id_kategori'), fn ($q) => $q->where('id_kategori', $request->integer('id_kategori')))
            ->when($request->filled('judul'), fn ($q) => $q->where('judul', 'like', '%'.$this->escapeLike($request->query('judul')).'%'))
            ->when($request->boolean('trashed'), fn ($q) => $q->onlyTrashed())
            ->latest('created_at')
            ->paginate(24);

        return $this->ok($karya);
    }

    public function karyaShow(Karya $karya)
    {
        return $this->ok($karya->load(['kategori', 'pengguna', 'pameran', 'objek']));
    }

    public function karyaDestroy(Karya $karya)
    {
        $karya->delete();

        return $this->ok(null, 'Karya dimoderasi (dihapus).');
    }

    public function karyaRestore(Karya $karya)
    {
        $karya->restore();

        return $this->ok($karya, 'Karya dipulihkan.');
    }

    public function pameranIndex(Request $request)
    {
        $request->validate(['judul' => ['nullable', 'string', 'max:100']]);

        $pameran = Pameran::with(['pengguna', 'model3d'])
            ->withTrashed()
            ->when($request->filled('judul'), fn ($q) => $q->where('judul', 'like', '%'.$this->escapeLike($request->query('judul')).'%'))
            ->when($request->boolean('trashed'), fn ($q) => $q->onlyTrashed())
            ->latest('id_pameran')
            ->paginate(24);

        return $this->ok($pameran);
    }

    public function pameranShow(Pameran $pameran)
    {
        return $this->ok($pameran->load(['pengguna', 'model3d', 'ruangan', 'karya']));
    }

    public function pameranDestroy(Pameran $pameran)
    {
        $pameran->delete();

        return $this->ok(null, 'Pameran dimoderasi (dihapus).');
    }

    public function pameranRestore(Pameran $pameran)
    {
        $pameran->restore();

        return $this->ok($pameran, 'Pameran dipulihkan.');
    }
}
