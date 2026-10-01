<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Pengguna;
use App\Models\SanksiAkun;
use Illuminate\Http\Request;

class SanksiAkunController extends Controller
{
    public function index(Request $request)
    {
        $sanksi = SanksiAkun::with('pengguna')
            ->when($request->filled('id_pengguna'), fn ($q) => $q->where(
                'id_pengguna',
                Pengguna::uuidToBytes($request->string('id_pengguna')),
            ))
            ->when($request->enum('jenis_sanksi', ['ditanggungkan', 'diblokir']), fn ($q, $v) => $q->where('jenis_sanksi', $v))
            ->when($request->boolean('aktif'), fn ($q) => $q->where(
                fn ($sub) => $sub->whereNull('berakhir')->orWhere('berakhir', '>', now()),
            ))
            ->latest('id_sanksi')
            ->paginate(24);

        return $this->ok($sanksi);
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'id_pengguna' => ['required', 'string', 'exists:pengguna,id_pengguna'],
            'jenis_sanksi' => ['required', 'string', 'in:ditanggungkan,diblokir'],
            'alasan' => ['required', 'string', 'max:255'],
            'mulai' => ['required', 'date'],
            'berakhir' => ['nullable', 'date', 'after:mulai'],
        ]);

        $pengguna = Pengguna::findOrFail($validated['id_pengguna']);

        $sanksi = SanksiAkun::create([
            'id_pengguna' => $pengguna->id_pengguna,
            'jenis_sanksi' => $validated['jenis_sanksi'],
            'alasan' => $validated['alasan'],
            'mulai' => $validated['mulai'],
            'berakhir' => $validated['berakhir'] ?? null,
        ]);

        $pengguna->forceFill([
            'status' => $validated['jenis_sanksi'] === 'diblokir' ? 'nonaktifkan' : 'ditanggungkan',
        ])->save();

        return $this->created($sanksi->load('pengguna'), 'Sanksi diterapkan.');
    }

    public function show(Request $request, string $id)
    {
        return $this->ok(SanksiAkun::findOrFail($id)->load('pengguna'));
    }

    public function update(Request $request, string $id)
    {
        $sanksi = SanksiAkun::findOrFail($id);
        $pengguna = $sanksi->pengguna;

        // Cabut sanksi lebih awal: hapus record dan pulihkan status akun bila tidak ada sanksi aktif lain.
        $validated = $request->validate([
            'cabut' => ['sometimes', 'boolean'],
            'jenis_sanksi' => ['sometimes', 'string', 'in:ditanggungkan,diblokir'],
            'alasan' => ['sometimes', 'string', 'max:255'],
            'mulai' => ['sometimes', 'date'],
            'berakhir' => ['sometimes', 'nullable', 'date', 'after:mulai'],
        ]);

        if ($request->boolean('cabut')) {
            $sanksi->delete();

            $masihAktif = $pengguna->sanksi()
                ->where('id_sanksi', '!=', $sanksi->id_sanksi)
                ->where(fn ($q) => $q->whereNull('berakhir')->orWhere('berakhir', '>', now()))
                ->exists();

            if (! $masihAktif) {
                $pengguna->forceFill(['status' => 'aktif'])->save();
            }

            return $this->ok(null, 'Sanksi dicabut.');
        }

        $sanksi->forceFill($validated)->save();

        return $this->ok($sanksi->fresh()->load('pengguna'), 'Sanksi diperbarui.');
    }
}
