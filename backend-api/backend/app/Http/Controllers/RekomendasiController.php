<?php

namespace App\Http\Controllers;

use App\Models\Karya;
use App\Models\Pengguna;
use Illuminate\Http\Request;

class RekomendasiController extends Controller
{
    public function index(Request $request)
    {
        /** @var Pengguna $pengguna */
        $pengguna = $request->user();

        $minat = $pengguna->kategori()->pluck('id_kategori');

        $karya = Karya::query()->select('karya.*')->with(['kategori', 'pengguna']);

        if ($minat->isNotEmpty()) {
            $karya->join('pengguna_kategori', function ($join) use ($pengguna) {
                $join->on('karya.id_kategori', '=', 'pengguna_kategori.id_kategori')
                    ->where('pengguna_kategori.id_pengguna', '=', $pengguna->id_pengguna);
            })
                ->orderByDesc('pengguna_kategori.bobot')
                ->latest('created_at');
        } else {
            $karya->inRandomOrder();
        }

        return $this->ok($karya->paginate(24));
    }
}
