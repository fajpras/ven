<?php

namespace App\Http\Controllers;

use App\Models\Objek;
use App\Models\PosisiObjek;
use Illuminate\Http\Request;

class PosisiObjekController extends Controller
{
    public function update(Request $request, Objek $objek)
    {
        $validated = $request->validate([
            'posisi_x' => ['nullable', 'numeric'],
            'posisi_y' => ['nullable', 'numeric'],
            'posisi_z' => ['nullable', 'numeric'],
            'rotasi_x' => ['nullable', 'numeric'],
            'rotasi_y' => ['nullable', 'numeric'],
            'rotasi_z' => ['nullable', 'numeric'],
        ]);

        $posisi = PosisiObjek::create($validated);

        $lama = $objek->posisi;
        $objek->forceFill(['id_posisi' => $posisi->id_posisi])->save();

        if ($lama) {
            $lama->delete();
        }

        return $this->ok($objek->fresh()->load('posisi'), 'Posisi objek diperbarui.');
    }
}
