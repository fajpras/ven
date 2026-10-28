<?php

namespace App\Observers;

use App\Models\Pengguna;
use App\Services\LogSistem;
use Illuminate\Contracts\Events\ShouldHandleEventsAfterCommit;
use Illuminate\Database\Eloquent\Model;

class AktivitasObserver implements ShouldHandleEventsAfterCommit
{
    public function created(Model $m): void
    {
        $this->catat($m, 'tambah');
    }

    public function updated(Model $m): void
    {
        $kolom = array_values(array_diff(array_keys($m->getChanges()), ['updated_at', 'created_at']));

        if ($kolom) {
            $this->catat($m, 'ubah', implode(', ', $kolom));
        }
    }

    public function deleted(Model $m): void
    {
        $this->catat($m, 'hapus');
    }

    public function restored(Model $m): void
    {
        $this->catat($m, 'pulih');
    }

    private function catat(Model $m, string $aksi, ?string $detail = null): void
    {
        $key = $m->getKey();
        $label = (string) $key;
        $kolomProfil = ['nama', 'nama_panggilan', 'deskripsi', 'foto_profil', 'email', 'nomor_hp'];

        $jenis = match ($m->getTable()) {
            'pameran'     => 'pameran',
            'karya'       => 'karya',
            'model'       => 'model',
            'ruangan'     => 'ruangan',
            'undangan'    => 'undangan',
            'kategori'    => 'kategori',
            'objek'       => 'objek',
            'sanksi_akun' => 'sanksi',
            'pengguna'    => ($aksi === 'ubah') && array_intersect($kolomProfil, array_keys($m->getChanges()))
                ? 'profil'
                : 'pengguna',
            default       => $m->getTable(),
        };

        $namaObjek = $m->judul ?? $m->nama ?? null;
        $labelObjek = "#{$label}".($namaObjek ? " \"{$namaObjek}\"" : '');

        // Tentukan ID Pengguna yang berelasi dengan aktivitas (tidak boleh NULL jika entitasnya Pengguna)
        $pelakuId = ($m instanceof Pengguna)
            ? $key
            : (request()?->user('sanctum')?->id_pengguna ?? ($m->id_pengguna ?? null));

        $isRegistrasi = ($m instanceof Pengguna && $aksi === 'tambah' && (request()?->is('*register*') || request()?->is('*auth/google*')));
        $isSeeder = ($m instanceof Pengguna && $aksi === 'tambah' && app()->runningInConsole() && ! app()->runningUnitTests());

        if ($isSeeder) {
            $namaPelaku = 'Sistem (Seeder)';
            $pesan = "Inisialisasi akun pengguna {$labelObjek} via Seeder";
        } elseif ($isRegistrasi) {
            $aksi = 'registrasi';
            $namaPelaku = (string) $m->nama;
            $pesan = "Registrasi akun pengguna {$labelObjek}";
        } else {
            $namaPelaku = ($m instanceof Pengguna && $aksi === 'tambah')
                ? (string) $m->nama
                : (request()?->user('sanctum')?->nama ?? 'Sistem');

            $pesan = match ($aksi) {
                'tambah' => "Menambah {$jenis} {$labelObjek}",
                'ubah'   => "Mengubah {$jenis} {$labelObjek}".($detail ? " ({$detail})" : ''),
                'hapus'  => "Menghapus {$jenis} {$labelObjek}",
                'pulih'  => "Memulihkan {$jenis} {$labelObjek} (Restore)",
                default  => "{$aksi} {$jenis} {$labelObjek}",
            };
        }

        LogSistem::aktivitas(
            entitas: $namaPelaku,
            jenis: $jenis,
            pesan: $pesan,
            status: 'berhasil',
            idPengguna: $pelakuId,
            aksi: $aksi,
        );
    }
}
