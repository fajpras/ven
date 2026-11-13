<?php

namespace App\Http\Controllers\Pengguna;

use App\Http\Controllers\Controller;
use App\Models\Karya;
use App\Models\Pameran;
use App\Models\Pengguna;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\Rule;

class PencarianController extends Controller
{
    private const PENGGUNA_PUBLIK = 'pengguna:id_pengguna,nama,nama_panggilan,foto_profil';

    /**
     * Pencarian terpadu (Unified Search) untuk Karya, Pameran, dan Pengguna.
     */
    public function search(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'q' => ['nullable', 'string', 'max:100'],
            'tipe' => ['nullable', 'string', Rule::in(['semua', 'karya', 'pameran', 'pengguna'])],
        ]);

        $keyword = $this->escapeLike(trim($validated['q'] ?? ''));
        $tipe = $validated['tipe'] ?? 'semua';
        $excludeIds = array_filter(array_map('trim', explode(',', (string) $request->query('exclude', ''))));
        $refresh = $request->boolean('refresh', false);

        if ($tipe === 'karya') {
            $paginator = $this->queryKarya($request, $keyword, $excludeIds, $refresh)->paginate(24);
            if ($paginator->isEmpty() && !empty($excludeIds)) {
                $paginator = $this->queryKarya($request, $keyword, [], true)->paginate(24);
            }
            return $this->ok($paginator);
        }

        if ($tipe === 'pameran') {
            return $this->ok($this->queryPameran($keyword)->paginate(24));
        }

        if ($tipe === 'pengguna') {
            return $this->ok($this->queryPengguna($keyword)->paginate(24));
        }

        // Tipe 'semua': ringkasan 8 item teratas per kategori
        return $this->ok([
            'karya' => $this->queryKarya($request, $keyword, $excludeIds, $refresh)->take(8)->get(),
            'pameran' => $this->queryPameran($keyword)->take(8)->get(),
            'pengguna' => $this->queryPengguna($keyword)->take(8)->get(),
        ]);
    }

    private function queryKarya(Request $request, string $keyword, array $excludeIds = [], bool $refresh = false)
    {
        $user = $request->user();
        $userCategories = $user ? $user->kategori()->get() : collect();
        $hasOthers = $userCategories->contains(fn ($k) => strcasecmp($k->kategori, 'others') === 0);
        $minatIds = $userCategories->pluck('id_kategori')->toArray();

        // Jika ada keyword pencarian, utamakan pencarian berbasis teks
        if ($keyword !== '') {
            return Karya::with(['kategori', self::PENGGUNA_PUBLIK])
                ->where(function ($sub) use ($keyword) {
                    $sub->where('judul', 'like', "%{$keyword}%")
                        ->orWhere('deskripsi', 'like', "%{$keyword}%")
                        ->orWhereHas('kategori', fn ($k) => $k->where('kategori', 'like', "%{$keyword}%"));
                })
                ->when(!empty($excludeIds), fn ($q) => $q->whereNotIn('id_karya', $excludeIds))
                ->when($refresh, fn ($q) => $q->inRandomOrder(), fn ($q) => $q->latest('created_at'));
        }

        // Jika tidak mencari keyword (Discovery Feed):
        // KASUS 1: Pengguna memilih "Others" atau tanpa preferensi -> Tampilkan seluruh kategori
        if ($hasOthers || empty($minatIds)) {
            return Karya::with(['kategori', self::PENGGUNA_PUBLIK])
                ->when(!empty($excludeIds), fn ($q) => $q->whereNotIn('id_karya', $excludeIds))
                ->when($refresh, fn ($q) => $q->inRandomOrder(), fn ($q) => $q->latest('created_at'));
        }

        // KASUS 2: Pengguna memilih kategori minat spesifik (misal: 3D) TANPA "Others"
        $totalMinatTersedia = Karya::whereIn('id_kategori', $minatIds)
            ->when(!empty($excludeIds), fn ($q) => $q->whereNotIn('id_karya', $excludeIds))
            ->count();

        if ($totalMinatTersedia > 0) {
            // Tampilkan HANYA karya yang sesuai minat pengguna
            return Karya::with(['kategori', self::PENGGUNA_PUBLIK])
                ->whereIn('id_kategori', $minatIds)
                ->when(!empty($excludeIds), fn ($q) => $q->whereNotIn('id_karya', $excludeIds))
                ->when($refresh, fn ($q) => $q->inRandomOrder(), fn ($q) => $q->latest('created_at'));
        }

        // Jika karya minat sudah habis -> Baru tampilkan kategori lain
        return Karya::with(['kategori', self::PENGGUNA_PUBLIK])
            ->whereNotIn('id_kategori', $minatIds)
            ->when(!empty($excludeIds), fn ($q) => $q->whereNotIn('id_karya', $excludeIds))
            ->when($refresh, fn ($q) => $q->inRandomOrder(), fn ($q) => $q->latest('created_at'));
    }

    private function queryPameran(string $keyword)
    {
        return Pameran::with(['model3d', self::PENGGUNA_PUBLIK])
            ->when($keyword !== '', function ($q) use ($keyword) {
                $q->where(function ($sub) use ($keyword) {
                    $sub->where('judul', 'like', "%{$keyword}%")
                        ->orWhere('tipe', 'like', "%{$keyword}%");
                });
            })
            ->latest('id_pameran');
    }

    private function queryPengguna(string $keyword)
    {
        return Pengguna::query()
            ->where('status', 'aktif')
            ->when($keyword !== '', function ($q) use ($keyword) {
                $q->where(function ($sub) use ($keyword) {
                    $sub->where('nama', 'like', "%{$keyword}%")
                        ->orWhere('nama_panggilan', 'like', "%{$keyword}%");
                });
            })
            ->select(['id_pengguna', 'nama', 'nama_panggilan', 'foto_profil', 'deskripsi'])
            ->latest('created_at');
    }
}

