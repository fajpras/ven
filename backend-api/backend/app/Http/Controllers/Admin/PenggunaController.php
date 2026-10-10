<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\Pengguna;
use App\Models\SanksiAkun;
use Illuminate\Http\Request;
use Illuminate\Support\Carbon;
use Illuminate\Support\Facades\DB;
use Illuminate\Validation\Rule;

class PenggunaController extends Controller
{
    public function index(Request $request)
    {
        $request->validate([
            'cari' => ['nullable', 'string', 'max:100'],
            'status' => ['nullable', Rule::in(['aktif', 'ditanggungkan', 'nonaktifkan'])],
        ]);

        $pengguna = Pengguna::query()
            ->when($request->filled('cari'), function ($q) use ($request) {
                $kata = '%'.$this->escapeLike($request->query('cari')).'%';
                $q->where(fn ($s) => $s->where('nama', 'like', $kata)->orWhere('email', 'like', $kata)->orWhere('nama_panggilan', 'like', $kata));
            })
            ->when($request->filled('status'), fn ($q) => $q->where('status', $request->query('status')))
            ->latest('created_at')
            ->paginate(24);

        return $this->ok($pengguna);
    }

    public function show(Pengguna $pengguna)
    {
        return $this->ok($pengguna->load('sanksi'));
    }

    public function sanksiIndex(Request $request)
    {
        $request->validate([
            'id_pengguna' => ['nullable', 'uuid'],
            'jenis_sanksi' => ['nullable', Rule::in(['ditanggungkan', 'diblokir'])],
        ]);

        $sanksi = SanksiAkun::with('pengguna:id_pengguna,nama,email')
            ->when($this->uuidQuery($request, 'id_pengguna'), fn ($q, $v) => $q->where('id_pengguna', $v))
            ->when($request->filled('jenis_sanksi'), fn ($q) => $q->where('jenis_sanksi', $request->query('jenis_sanksi')))
            ->when($request->boolean('aktif'), fn ($q) => $q->where('mulai', '<=', now())
                ->where(fn ($s) => $s->whereNull('berakhir')->orWhere('berakhir', '>', now())))
            ->latest('id_sanksi')
            ->paginate(24);

        return $this->ok($sanksi);
    }

    public function sanksiStore(Request $request, Pengguna $pengguna)
    {
        if ($pengguna->isAdmin() || $pengguna->is($request->user())) {
            return $this->forbidden('Akun admin tidak dapat dikenai sanksi.');
        }

        $request->merge(['mulai' => $request->input('mulai', now()->toDateTimeString())]);

        $validated = $request->validate([
            'jenis_sanksi' => ['required', Rule::in(['ditanggungkan', 'diblokir'])],
            'alasan' => ['required', 'string', 'max:255'],
            'mulai' => ['required', 'date'],
            'permanen' => ['required', 'boolean'],
            'berakhir' => [Rule::requiredIf(! $request->boolean('permanen')), 'nullable', 'date', 'after:mulai'],
        ]);

        $sanksi = DB::transaction(function () use ($pengguna, $validated, $request) {
            $sanksi = $pengguna->sanksi()->forceCreate([
                'id_pengguna' => $pengguna->id_pengguna,
                'jenis_sanksi' => $validated['jenis_sanksi'],
                'alasan' => $validated['alasan'],
                'mulai' => $validated['mulai'],
                'berakhir' => $request->boolean('permanen') ? null : $validated['berakhir'],
            ]);

            $pengguna->sinkronStatusSanksi();

            return $sanksi;
        });

        return $this->created($sanksi->load('pengguna:id_pengguna,nama,email'), 'Sanksi diterapkan.');
    }

    public function sanksiShow(SanksiAkun $sanksi)
    {
        return $this->ok($sanksi->load('pengguna'));
    }

    public function sanksiUpdate(Request $request, SanksiAkun $sanksi)
    {
        $validated = $request->validate([
            'jenis_sanksi' => ['sometimes', Rule::in(['ditanggungkan', 'diblokir'])],
            'alasan' => ['sometimes', 'string', 'max:255'],
            'mulai' => ['sometimes', 'date'],
            'berakhir' => ['sometimes', 'nullable', 'date'],
        ]);

        $mulai = Carbon::parse($validated['mulai'] ?? $sanksi->mulai);
        $berakhir = array_key_exists('berakhir', $validated)
            ? ($validated['berakhir'] ? Carbon::parse($validated['berakhir']) : null)
            : $sanksi->berakhir;

        if ($berakhir && $berakhir->lessThanOrEqualTo($mulai)) {
            return $this->unprocessable(['berakhir' => ['Tanggal berakhir harus setelah tanggal mulai.']]);
        }

        DB::transaction(function () use ($sanksi, $validated) {
            $sanksi->forceFill($validated)->save();
            $sanksi->pengguna->sinkronStatusSanksi();
        });

        return $this->ok($sanksi->fresh()->load('pengguna'), 'Sanksi diperbarui.');
    }

    public function sanksiCabut(SanksiAkun $sanksi)
    {
        DB::transaction(function () use ($sanksi) {
            $sekarang = now();

            $sanksi->forceFill([

                'mulai' => $sanksi->mulai->lessThan($sekarang) ? $sanksi->mulai : $sekarang->copy()->subSecond(),
                'berakhir' => $sekarang,
            ])->save();

            $sanksi->pengguna->sinkronStatusSanksi();
        });

        return $this->ok(null, 'Sanksi dicabut.');
    }
}
