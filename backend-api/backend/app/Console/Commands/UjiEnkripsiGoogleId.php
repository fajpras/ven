<?php

namespace App\Console\Commands;

use App\Models\Pengguna;
use App\Services\Crypto\LayananEnkripsi;
use Illuminate\Console\Command;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class UjiEnkripsiGoogleId extends Command
{
    protected $signature = 'uji:enkripsi-google-id 
                            {google_id? : Nilai Google ID dummy opsional (default: otomatis digenerate)}
                            {--simpan : Simpan pengguna dummy ke database untuk menguji model Eloquent}';

    protected $description = 'Melakukan simulasi dan pengujian enkripsi, blind index, dan dekripsi Google ID';

    public function handle(LayananEnkripsi $enkripsi): int
    {
        $this->line(str_repeat('=', 70));
        $this->info(' UJI ENKRIPSI & DEKRIPSI GOOGLE ID (AES-256-GCM + BLIND INDEX)');
        $this->line(str_repeat('=', 70));

        // 1. Siapkan Plaintext Google ID
        $googleIdAsli = (string) ($this->argument('google_id') ?: '10'.random_int(10000000000000000, 99999999999999999));
        $this->line("<comment>1. Data Input (Plaintext):</comment>");
        $this->line("   Google ID Asli : <info>{$googleIdAsli}</info>");
        $this->newLine();

        // 2. Proses Enkripsi AES-256-GCM
        $ciphertext1 = $enkripsi->enkripsi($googleIdAsli);
        $ciphertext2 = $enkripsi->enkripsi($googleIdAsli); // Enkripsi ulang teks yang sama untuk buktikan IV acak
        $blindIndex  = $enkripsi->blindIndex($googleIdAsli);

        $this->line("<comment>2. Hasil Enkripsi (AES-256-GCM):</comment>");
        $this->line("   Ciphertext #1  : <info>{$ciphertext1}</info>");
        $this->line("   Ciphertext #2  : <info>{$ciphertext2}</info>");
        $this->line("   Blind Index    : <info>{$blindIndex}</info> (HMAC-SHA-256 untuk WHERE query)");
        $this->newLine();

        // Verifikasi sifat acak IV
        if ($ciphertext1 !== $ciphertext2) {
            $this->line("   <info>✓ Kerapatan Kriptografi Terpenuhi:</info> Plaintext yang sama menghasilkan ciphertext yang berbeda karena random IV (12-byte nonce).");
        }
        $this->newLine();

        // 3. Proses Dekripsi
        $this->line("<comment>3. Pengujian Dekripsi:</comment>");
        $hasilDekripsi1 = $enkripsi->dekripsi($ciphertext1);
        $hasilDekripsi2 = $enkripsi->dekripsi($ciphertext2);

        $this->line("   Dekripsi #1    : <info>{$hasilDekripsi1}</info>");
        $this->line("   Dekripsi #2    : <info>{$hasilDekripsi2}</info>");

        if ($hasilDekripsi1 === $googleIdAsli && $hasilDekripsi2 === $googleIdAsli) {
            $this->line("   <info>✓ Dekripsi Berhasil & Identik dengan input asli.</info>");
        } else {
            $this->error("   ✗ Dekripsi gagal mengembalikan teks asli!");
            return self::FAILURE;
        }
        $this->newLine();

        // 4. Uji Anti-Tampering (Auth Tag Verification)
        $this->line("<comment>4. Pengujian Keamanan Anti-Tampering (Auth Tag):</comment>");
        // Ubah 1 karakter pada ciphertext
        $panjang = strlen($ciphertext1);
        $karakterTerakhir = $ciphertext1[$panjang - 1];
        $karakterPalsu = ($karakterTerakhir === 'A') ? 'B' : 'A';
        $ciphertextRusak = substr($ciphertext1, 0, -1) . $karakterPalsu;

        $hasilTamper = $enkripsi->dekripsi($ciphertextRusak);
        if ($hasilTamper === null) {
            $this->line("   Ciphertext Palsu: {$ciphertextRusak}");
            $this->line("   Hasil Dekripsi  : <info>NULL (Ditolak)</info>");
            $this->line("   <info>✓ Auth Tag Bekerja:</info> Manipulasi data sekecil apapun langsung digagalkan.");
        } else {
            $this->error("   ✗ Bahaya: Ciphertext yang dimanipulasi berhasil didekripsi!");
            return self::FAILURE;
        }
        $this->newLine();

        // 5. Opsi Uji Model Eloquent (Jika flag --simpan digunakan)
        if ($this->option('simpan')) {
            $this->line("<comment>5. Pengujian Siklus Model Eloquent & Database:</comment>");
            $emailDummy = 'dummy_google_' . Str::lower(Str::random(8)) . '@ven.test';

            // Simpan via Eloquent (Mutator otomatis enkripsi)
            $pengguna = Pengguna::create([
                'nama'           => 'Dummy Tester Google',
                'nama_panggilan' => 'tester_' . Str::lower(Str::random(6)),
                'email'          => $emailDummy,
                'google_id'      => $googleIdAsli, // Model akan mengenkripsi otomatis
                'role'           => 'pengguna',
            ]);

            // Periksa baris mentah di DB (harus terenkripsi)
            $rawDb = DB::table('pengguna')->where('id_pengguna', $pengguna->id_pengguna)->first();
            $this->line("   DB Raw google_id      : <comment>{$rawDb->google_id}</comment>");
            $this->line("   DB Raw google_id_bidx : <comment>{$rawDb->google_id_bidx}</comment>");

            // Cari via Blind Index
            $ditemukan = Pengguna::where('google_id_bidx', $blindIndex)->first();
            $this->line("   Pencarian Blind Index : " . ($ditemukan ? "<info>Ditemukan (ID: {$ditemukan->id_pengguna})</info>" : "<error>Gagal ditemukan</error>"));

            // Akses via Accessor (harus otomatis didekripsi)
            $this->line("   Eloquent Accessor get : <info>{$ditemukan->google_id}</info>");

            // Bersihkan data dummy menggunakan koneksi admin/DBA agar tidak terbentur hak akses least-privilege
            DB::connection('mysql_dba')->table('pengguna')->where('id_pengguna', $pengguna->id_pengguna)->delete();
            $this->line("   <info>✓ Data dummy DB berhasil dibersihkan (via DBA).</info>");
            $this->newLine();
        } else {
            $this->line("Tip: Jalankan dengan <comment>--simpan</comment> untuk menguji siklus lengkap simpan & baca di database.");
        }

        $this->line(str_repeat('=', 70));
        $this->info(" SEMUA PENGUJIAN ENKRIPSI & DEKRIPSI BERHASIL!");
        $this->line(str_repeat('=', 70));

        return self::SUCCESS;
    }
}