<?php

namespace App\Http\Controllers\Pengguna;

use App\Http\Controllers\Controller;
use App\Models\Pengguna;
use App\Models\Ruangan;
use App\Models\Undangan;
use Illuminate\Http\Request;
use Illuminate\Support\Str;
use Illuminate\Validation\Rule;

class DiskusiController extends Controller
{
    public function index(Request $request)
    {
        $request->validate([
            'status' => ['nullable', Rule::in(['diproses', 'disetujui', 'ditolak'])],
            'sebagai' => ['nullable', Rule::in(['pengundang', 'penyelenggara'])],
        ]);

        $id = $request->user()->id_pengguna;

        $undangan = Undangan::with(['ruangan.pameran:id_pameran,judul,id_pengguna'])
            ->when(
                $request->query('sebagai') === 'penyelenggara',
                fn ($q) => $q->whereHas('ruangan.pameran', fn ($p) => $p->where('id_pengguna', $id)),
                fn ($q) => $q->where('id_pengguna', $id),
            )
            ->when($request->filled('status'), fn ($q) => $q->where('status', $request->query('status')))
            ->latest('id_undangan')
            ->paginate(24);

        return $this->ok($undangan);
    }

    public function store(Request $request)
    {
        $validated = $request->validate([
            'id_ruangan' => ['required', 'integer', Rule::exists('ruangan', 'id_ruangan')->where('jenis', 'privat')],
            'waktu' => ['required', 'date', 'after:now'],
            'catatan' => ['nullable', 'string', 'max:255'],
        ]);

        $ruangan = Ruangan::with('pameran')->findOrFail($validated['id_ruangan']);
        $user = $request->user();

        if (! $ruangan->pameran) {
            return $this->notFound('Pameran tidak tersedia.');
        }

        if (hash_equals($ruangan->pameran->id_pengguna, $user->id_pengguna)) {
            return $this->error('Anda tidak perlu mengundang diri sendiri ke pameran Anda.', 422);
        }

        $sudahAda = Undangan::where('id_pengguna', $user->id_pengguna)
            ->where('id_ruangan', $ruangan->id_ruangan)
            ->whereIn('status', ['diproses', 'disetujui'])
            ->exists();

        if ($sudahAda) {
            return $this->error('Anda sudah memiliki undangan aktif untuk ruangan ini.', 409);
        }

        $undangan = Undangan::forceCreate([
            'id_pengguna' => $user->id_pengguna,
            'id_ruangan' => $ruangan->id_ruangan,
            'waktu' => $validated['waktu'],
            'catatan' => $validated['catatan'] ?? null,
            'status' => 'diproses',
        ]);

        return $this->created($undangan, 'Undangan diskusi diajukan.');
    }

    public function show(Request $request, Undangan $undangan)
    {
        $this->pastikanPeserta($request, $undangan);

        return $this->ok($undangan->load(['ruangan.pameran:id_pameran,judul,id_pengguna', 'pengguna:id_pengguna,nama,nama_panggilan,foto_profil']));
    }

    public function status(Request $request, Undangan $undangan)
    {
        $this->pastikanPeserta($request, $undangan);

        return $this->ok([
            'id_undangan' => $undangan->id_undangan,
            'status' => $undangan->status,
            'waktu' => $undangan->waktu,
            'tautan' => $undangan->status === 'disetujui' ? $undangan->tautan : null,
        ]);
    }

    public function tanggapi(Request $request, Undangan $undangan)
    {
        $this->pastikanPenyelenggara($request, $undangan);

        $validated = $request->validate([
            'status' => ['required', Rule::in(['disetujui', 'ditolak'])],
            'catatan' => ['nullable', 'string', 'max:255'],
        ]);

        if ($undangan->status !== 'diproses') {
            return $this->error('Undangan ini sudah ditanggapi.', 409);
        }

        if ($validated['status'] === 'disetujui') {
            $undangan->forceFill(['status' => 'disetujui', 'tautan' => Str::random(32)])->save();

            return $this->ok($undangan->fresh(), 'Undangan disetujui.');
        }

        $undangan->forceFill([
            'status' => 'ditolak',
            'catatan' => $validated['catatan'] ?? $undangan->catatan,
        ])->save();

        return $this->ok($undangan->fresh(), 'Undangan ditolak.');
    }

    public function voiceToken(Request $request, Ruangan $ruangan)
    {
        $user = $request->user();
        $ruangan->loadMissing('pameran');

        abort_unless($ruangan->pameran, 404);

        $namaRoom = 'ruangan-'.$ruangan->id_ruangan;

        if ($ruangan->jenis === 'privat') {
            $penyelenggara = hash_equals($ruangan->pameran->id_pengguna, $user->id_pengguna);

            if ($penyelenggara) {
                $undangan = Undangan::where('id_ruangan', $ruangan->id_ruangan)->where('status', 'disetujui')
                    ->whereBetween('waktu', [now()->subHours(3), now()->addMinutes(15)])->latest('waktu')->first();
            } else {
                $undangan = Undangan::where('id_ruangan', $ruangan->id_ruangan)
                    ->where('id_pengguna', $user->id_pengguna)->where('status', 'disetujui')
                    ->whereBetween('waktu', [now()->subHours(3), now()->addMinutes(15)])->latest('waktu')->first();
            }

            abort_unless($undangan, 403, 'Anda tidak memiliki sesi diskusi aktif di ruangan ini.');

            $namaRoom = 'diskusi-'.$ruangan->id_ruangan.'-'.$undangan->id_undangan;
        }

        $kunci = config('services.livekit.key');
        $rahasia = config('services.livekit.secret');

        if (! $kunci || ! $rahasia) {
            return $this->error('Layanan voice chat belum dikonfigurasi.', 503);
        }

        $now = time();
        $token = $this->jwt([
            'iss' => $kunci,
            'sub' => $user->id_pengguna,
            'name' => $user->nama_panggilan ?: $user->nama,
            'nbf' => $now,
            'exp' => $now + 3600,
            'video' => [
                'room' => $namaRoom,
                'roomJoin' => true,
                'canPublish' => true,
                'canSubscribe' => true,
                'canPublishData' => false,
            ],
        ], $rahasia);

        return $this->ok(['url' => config('services.livekit.url'), 'room' => $namaRoom, 'token' => $token]);
    }

    private function pemilikPameran(Undangan $undangan): ?string
    {
        return $undangan->ruangan?->pameran?->id_pengguna;
    }

    private function pastikanPeserta(Request $request, Undangan $undangan): void
    {
        $user = $request->user();
        $pemilik = $this->pemilikPameran($undangan);

        $boleh = hash_equals($undangan->id_pengguna, $user->id_pengguna)
            || ($pemilik && hash_equals($pemilik, $user->id_pengguna))
            || $user->isAdmin();

        abort_unless($boleh, 403, 'Anda tidak berhak mengakses undangan ini.');
    }

    private function pastikanPenyelenggara(Request $request, Undangan $undangan): void
    {
        $this->pastikanPemilik($request, $this->pemilikPameran($undangan), true, 'Anda tidak berhak menanggapi undangan ini.');
    }

    private function jwt(array $claims, string $secret): string
    {
        $b64 = fn (string $s) => rtrim(strtr(base64_encode($s), '+/', '-_'), '=');
        $head = $b64(json_encode(['alg' => 'HS256', 'typ' => 'JWT']));
        $body = $b64(json_encode($claims));

        return "$head.$body.".$b64(hash_hmac('sha256', "$head.$body", $secret, true));
    }
}
