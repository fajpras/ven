<?php

namespace App\Http\Controllers;

use App\Models\Ruangan;
use App\Models\Undangan;
use Illuminate\Http\Request;
use Illuminate\Support\Str;

class UndanganController extends Controller
{
    public function index(Request $request)
    {
        $undangan = Undangan::with(['ruangan.pameran'])
            ->where('id_pengguna', $request->user()->id_pengguna)
            ->when($request->enum('status', ['diproses', 'disetujui', 'ditolak']), fn ($q, $v) => $q->where('status', $v))
            ->latest('id_undangan')
            ->paginate(24);

        return $this->ok($undangan);
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'id_ruangan' => ['required', 'integer', 'exists:ruangan,id_ruangan'],
            'waktu' => ['required', 'date', 'after:now'],
            'catatan' => ['nullable', 'string', 'max:255'],
        ]);

        Ruangan::where('id_ruangan', $validated['id_ruangan'])
            ->where('jenis', 'privat')
            ->firstOrFail();

        $undangan = Undangan::create([
            'id_pengguna' => $request->user()->id_pengguna,
            'status' => 'diproses',
            ...$validated,
        ]);

        return $this->created($undangan, 'Undangan diskusi diajukan.');
    }

    public function show(Request $request, Undangan $undangan)
    {
        return $this->ok($undangan->load(['ruangan.pameran', 'pengguna']));
    }

    public function status(Request $request, Undangan $undangan)
    {
        return $this->ok([
            'id_undangan' => $undangan->id_undangan,
            'status' => $undangan->status,
            'waktu' => $undangan->waktu,
            'tautan' => $undangan->tautan,
        ]);
    }

    public function update(Request $request, Undangan $undangan)
    {
        $this->requirePenyelenggara($request, $undangan);

        $validated = $request->validate([
            'status' => ['required', 'string', 'in:disetujui,ditolak'],
            'catatan' => ['nullable', 'string', 'max:255'],
        ]);

        if ($validated['status'] === 'disetujui') {
            $undangan->forceFill([
                'status' => 'disetujui',
                'tautan' => Str::random(32),
            ])->save();

            return $this->ok($undangan->fresh(), 'Undangan disetujui.');
        }

        $undangan->forceFill([
            'status' => 'ditolak',
            'catatan' => $validated['catatan'] ?? $undangan->catatan,
        ])->save();

        return $this->ok($undangan->fresh(), 'Undangan ditolak.');
    }

    private function requirePenyelenggara(Request $request, Undangan $undangan): void
    {
        $user = $request->user();
        $pemilik = $undangan->ruangan->pameran->id_pengguna ?? null;

        if (! $user->isAdmin() && $pemilik !== $user->id_pengguna) {
            abort(403, 'Anda tidak berhak menanggapi undangan ini.');
        }
    }
}
