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

        if ($tipe === 'karya') {
            return $this->ok($this->queryKarya($keyword)->paginate(24));
        }

        if ($tipe === 'pameran') {
            return $this->ok($this->queryPameran($keyword)->paginate(24));
        }

        if ($tipe === 'pengguna') {
            return $this->ok($this->queryPengguna($keyword)->paginate(24));
        }

        // Tipe 'semua': ringkasan 8 item teratas per kategori
        return $this->ok([
            'karya' => $this->queryKarya($keyword)->take(8)->get(),
            'pameran' => $this->queryPameran($keyword)->take(8)->get(),
            'pengguna' => $this->queryPengguna($keyword)->take(8)->get(),
        ]);
    }

    private function queryKarya(string $keyword)
    {
        return Karya::with(['kategori', self::PENGGUNA_PUBLIK])
            ->when($keyword !== '', function ($q) use ($keyword) {
                $q->where(function ($sub) use ($keyword) {
                    $sub->where('judul', 'like', "%{$keyword}%")
                        ->orWhere('deskripsi', 'like', "%{$keyword}%")
                        ->orWhereHas('kategori', fn ($k) => $k->where('kategori', 'like', "%{$keyword}%"));
                });
            })
            ->latest('created_at');
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

