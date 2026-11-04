<?php

namespace App\Http\Controllers\Pengguna;

use App\Http\Controllers\Controller;
use App\Mail\AkunDinonaktifkanMail;
use App\Models\Karya;
use App\Models\Kategori;
use App\Models\Pameran;
use App\Models\Pengguna;
use App\Models\PenggunaPublik;
use App\Enums\TujuanOtp;
use App\Http\Requests\Profil\GantiEmailRequest;
use App\Http\Requests\Profil\KirimOtpGantiEmailRequest;
use App\Services\LogSistem;
use App\Services\Otp\LayananOtp;
use Illuminate\Database\UniqueConstraintViolationException;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Mail;
use Illuminate\Support\Facades\Storage;
use Illuminate\Validation\Rule;
use Illuminate\Validation\Rules\Password;

class ProfilController extends Controller
{
    public function __construct(private readonly LayananOtp $layananOtp) {}

    public function show(Request $request)
    {
        $publik = PenggunaPublik::find($request->user()->id_pengguna);

        return $this->ok($publik ?? $request->user());
    }

    public function update(Request $request)
    {
        $pengguna = $request->user();

        $validated = $request->validate([
            'nama' => ['sometimes', 'required', 'string', 'max:30'],
            'nama_panggilan' => [
                'sometimes',
                'required',
                'string',
                'min:3',
                'max:30',
                'regex:/^[A-Za-z0-9._]+$/',
                Rule::unique('pengguna', 'nama_panggilan')->ignore($pengguna->id_pengguna, 'id_pengguna'),
            ],
            'nomor_hp' => [
                'nullable',
                'string',
                'regex:/^\+?[0-9]{8,15}$/',
                Rule::unique('pengguna', 'nomor_hp')->ignore($pengguna->id_pengguna, 'id_pengguna'),
            ],
            'deskripsi' => ['nullable', 'string', 'max:150'],
            'foto_profil' => ['nullable', 'url:https', 'max:255'],
        ], [
            'nama.required' => 'Nama wajib diisi.',
            'nama.max' => 'Nama maksimal 30 karakter.',
            'nama_panggilan.required' => 'Username wajib diisi.',
            'nama_panggilan.min' => 'Username minimal 3 karakter.',
            'nama_panggilan.max' => 'Username maksimal 30 karakter.',
            'nama_panggilan.regex' => 'Username hanya boleh berisi huruf, angka, titik, dan garis bawah.',
            'nama_panggilan.unique' => 'Username sudah digunakan.',
            'nomor_hp.regex' => 'Nomor HP harus 8-15 digit dan boleh diawali tanda +.',
            'nomor_hp.unique' => 'Nomor HP sudah digunakan akun lain.',
            'deskripsi.max' => 'Deskripsi maksimal 150 karakter.',
        ]);

        $pengguna->forceFill($validated)->save();

        return $this->ok($pengguna, 'Profil diperbarui.');
    }

    public function updateFoto(Request $request): JsonResponse
    {
        $pengguna = $request->user();

        $request->validate([
            'foto' => ['required', 'file', 'max:5120', 'mimes:jpg,jpeg,png,webp'],
        ]);

        $urlLama = $pengguna->getRawOriginal('foto_profil');
        $pathBaru = $request->file('foto')->store('foto-profil', 'public');

        $pengguna->forceFill([
            'foto_profil' => $request->getSchemeAndHttpHost() . '/storage/' . $pathBaru,
        ])->save();

        $pathLama = $this->pathDariUrl($urlLama);
        if ($pathLama && Storage::disk('public')->exists($pathLama)) {
            Storage::disk('public')->delete($pathLama);
        }

        return $this->ok($pengguna->fresh(), 'Foto profil diperbarui.');
    }

    private function pathDariUrl(?string $url): ?string
    {
        if (! $url) {
            return null;
        }

        $penanda = '/storage/';
        $posisi = strpos($url, $penanda);

        return $posisi === false ? null : substr($url, $posisi + strlen($penanda));
    }

    public function kirimOtpGantiEmail(KirimOtpGantiEmailRequest $request): JsonResponse
    {
        $pengguna = $request->user();

        if ($pengguna->kata_sandi && ! Hash::check((string) $request->validated('kata_sandi'), $pengguna->kata_sandi)) {
            return $this->error('Kata sandi salah.', 422);
        }

        $this->layananOtp->terbitkan(TujuanOtp::GantiEmail, $request->validated('email_baru'), $pengguna->id_pengguna);

        LogSistem::aktivitas(
            entitas: 'pengguna',
            jenis: 'profil',
            pesan: 'Kode verifikasi OTP untuk pengiriman email baru telah dikirim',
            status: 'berhasil',
            idPengguna: $pengguna->id_pengguna,
            aksi: 'ubah',
        );

        return $this->ok(null, 'Kode verifikasi telah dikirim ke email baru.');
    }

    public function gantiEmail(GantiEmailRequest $request): JsonResponse
    {
        $pengguna = $request->user();
        $data = $request->validated();

        $this->layananOtp->verifikasi(TujuanOtp::GantiEmail, $data['email_baru'], $data['otp'], $pengguna->id_pengguna);

        try {
            $pengguna->forceFill(['email' => $data['email_baru']])->save();
        } catch (UniqueConstraintViolationException) {
            return $this->error('Email sudah digunakan akun lain.', 409);
        }

        return $this->ok($pengguna->fresh(), 'Email diperbarui.');
    }

    public function updatePassword(Request $request)
    {
        $pengguna = $request->user();

        $validated = $request->validate([

            'kata_sandi_saat_ini' => [Rule::requiredIf((bool) $pengguna->kata_sandi), 'nullable', 'string'],
            'kata_sandi' => ['required', 'string', 'max:72', 'different:kata_sandi_saat_ini', Password::min(8)->letters()->numbers()],
        ]);

        if ($pengguna->kata_sandi && ! Hash::check($validated['kata_sandi_saat_ini'], $pengguna->kata_sandi)) {
            return $this->error('Kata sandi saat ini salah.', 422);
        }

        $pengguna->forceFill(['kata_sandi' => $validated['kata_sandi']])->save();
        $pengguna->tokens()->where('id', '!=', $pengguna->currentAccessToken()->id)->delete();

        return $this->ok(null, 'Kata sandi diperbarui.');
    }

    public function updateStatus(Request $request)
    {
        $pengguna = $request->user();

        $validated = $request->validate([
            'status' => ['required', 'in:aktif,nonaktifkan'],
            'kata_sandi' => [Rule::requiredIf((bool) $pengguna->kata_sandi), 'nullable', 'string', 'max:72'],
        ]);

        if ($pengguna->kata_sandi && ! Hash::check((string) ($validated['kata_sandi'] ?? ''), $pengguna->kata_sandi)) {
            LogSistem::aktivitas(
                entitas: $pengguna->nama,
                jenis: 'profil',
                pesan: 'Gagal menonaktifkan akun: kata sandi salah',
                status: 'gagal',
                idPengguna: $pengguna->id_pengguna,
                aksi: 'ubah',
            );

            return $this->error('Kata sandi yang Anda masukkan salah.', 422);
        }

        if ($validated['status'] === 'nonaktifkan') {
            $pengguna->forceFill(['status' => 'nonaktifkan'])->save();
            $pengguna->tokens()->delete();

            $this->kirimEmailNonaktif($pengguna);

            return $this->ok(null, 'Akun Anda telah dinonaktifkan. Masuk kembali kapan saja untuk mengaktifkan.');
        }

        return $this->ok($pengguna, 'Akun sudah aktif.');
    }

    private function kirimEmailNonaktif(Pengguna $pengguna): void
    {
        try {
            Mail::to($pengguna->email)->send(new AkunDinonaktifkanMail($pengguna->nama));
        } catch (\Throwable $kesalahan) {
            report($kesalahan);
        }
    }

    public function minat(Request $request)
    {
        return $this->ok($request->user()->kategori()->orderByPivot('bobot', 'desc')->get());
    }

    /**
     * Daftar seluruh kategori yang dapat dipilih pengguna.
     */
    public function kategoriTersedia(): JsonResponse
    {
        return $this->ok(
            Kategori::query()
                ->orderBy('id_kategori')
                ->get(['id_kategori', 'kategori'])
        );
    }

    /**
     * Ganti seluruh minat pengguna. Array kosong dipakai tombol "Maybe later"
     * dan tetap menandai onboarding selesai agar tidak ditampilkan berulang.
     */
    public function updateMinat(Request $request): JsonResponse
    {
        $validated = $request->validate([
            'kategori' => ['present', 'array', 'max:50'],
            'kategori.*.id_kategori' => ['required', 'integer', 'distinct', 'exists:kategori,id_kategori'],
            'kategori.*.bobot' => ['required', 'numeric', 'min:0', 'max:1'],
        ]);

        $pengguna = $request->user();

        $sync = collect($validated['kategori'])
            ->keyBy('id_kategori')
            ->map(fn (array $item): array => ['bobot' => $item['bobot']])
            ->all();

        DB::transaction(function () use ($pengguna, $sync): void {
            $pengguna->kategori()->sync($sync);
            $pengguna->forceFill(['onboarding_selesai' => true])->save();
        });

        $jumlah = count($validated['kategori']);
        $pesan = $jumlah > 0
            ? "Memperbarui minat kategori ({$jumlah} kategori dipilih)"
            : 'Melewati pemilihan minat kategori (onboarding selesai)';

        LogSistem::aktivitas(
            entitas: $pengguna->nama,
            jenis: 'kategori',
            pesan: $pesan,
            status: 'berhasil',
            idPengguna: $pengguna->id_pengguna,
            aksi: 'ubah',
        );

        return $this->ok(
            $pengguna->kategori()->orderByPivot('bobot', 'desc')->get(),
            'Minat kategori disimpan.'
        );
    }

    public function destroyMinat(Request $request, Kategori $kategori): JsonResponse
    {
        $pengguna = $request->user();
        $pengguna->kategori()->detach($kategori->id_kategori);

        LogSistem::aktivitas(
            entitas: $pengguna->nama,
            jenis: 'kategori',
            pesan: "Menghapus minat kategori #{$kategori->id_kategori} \"{$kategori->kategori}\"",
            status: 'berhasil',
            idPengguna: $pengguna->id_pengguna,
            aksi: 'hapus',
        );

        return $this->ok(null, 'Minat kategori dihapus.');
    }

    /**
     * Mengambil detail profil publik pengguna lain beserta karya dan pamerannya.
     */
    public function detailPengguna(Pengguna $pengguna): JsonResponse
    {
        $karya = Karya::where('id_pengguna', $pengguna->id_pengguna)
            ->with(['kategori'])
            ->latest('created_at')
            ->get();

        $pameran = Pameran::where('id_pengguna', $pengguna->id_pengguna)
            ->with(['model3d'])
            ->latest('id_pameran')
            ->get();

        return $this->ok([
            'pengguna' => [
                'id_pengguna' => $pengguna->id_pengguna,
                'nama' => $pengguna->nama,
                'nama_panggilan' => $pengguna->nama_panggilan,
                'foto_profil' => $pengguna->foto_profil,
                'deskripsi' => $pengguna->deskripsi,
                'email' => $pengguna->email,
                'post_count' => $karya->count(),
                'exhibit_count' => $pameran->count(),
                'kategori' => $pengguna->kategori()->get(['kategori.id_kategori', 'kategori.kategori']),
            ],
            'karya' => $karya,
            'pameran' => $pameran,
        ]);
    }
}
