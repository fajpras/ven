<?php

namespace Tests\Unit;

use App\Models\Pengguna;
use App\Services\Crypto\LayananEnkripsi;
use Tests\TestCase;

/**
 * Uji dekripsi google_id tanpa menyentuh basis data sama sekali.
 *
 * Tidak ada tabel, kolom, atau migrasi baru yang dibutuhkan. Cukup jalankan:
 *   php vendor/bin/phpunit --testdox tests/Unit/DekripsiGoogleIdTest.php
 *
 * Kunci yang dipakai sama dengan VEN_CUSTOM_KEY pada phpunit.xml.
 */
class DekripsiGoogleIdTest extends TestCase
{
    private const KUNCI_UJI = 'testing-ven-custom-key-0123456789abcdef';

    private const SUB_UJI = '100000000000000000777';

    /**
     * Ciphertext tetap (known-answer) yang dibuat dengan KUNCI_UJI.
     * Dipakai untuk memastikan dekripsi menghasilkan nilai yang sama
     * setiap kali test dijalankan.
     */
    private const CIPHERTEXT_UJI = 'v1:NpHo4k45ZUbQIx8bZL649hdq1EDAsJKEh952XZMtsTk9NxrMPk/MTsZutVHkySGJyA==';

    private function layanan(string $kunci = self::KUNCI_UJI): LayananEnkripsi
    {
        return new LayananEnkripsi($kunci);
    }

    public function test_dekripsi_membuka_ciphertext_yang_diketahui(): void
    {
        $this->assertSame(
            self::SUB_UJI,
            $this->layanan()->dekripsi(self::CIPHERTEXT_UJI),
        );
    }

    public function test_hasil_dekripsi_konsisten_saat_diulang(): void
    {
        $layanan = $this->layanan();

        $this->assertSame(self::SUB_UJI, $layanan->dekripsi(self::CIPHERTEXT_UJI));
        $this->assertSame(self::SUB_UJI, $layanan->dekripsi(self::CIPHERTEXT_UJI));
    }

    public function test_dekripsi_gagal_bila_kunci_berbeda(): void
    {
        $payload = $this->layanan('kunci-asing-0123456789abcdef')->enkripsi(self::SUB_UJI);

        $this->assertNotNull($payload);
        $this->assertNull($this->layanan()->dekripsi($payload));
    }

    public function test_dekripsi_menolak_isi_data_yang_diubah(): void
    {
        $mentah = base64_decode(substr(self::CIPHERTEXT_UJI, 3), true);
        $this->assertIsString($mentah);

        // Ubah satu byte pada bagian ciphertext (setelah 12 byte IV + 16 byte tag).
        $posisi = 12 + 16;
        $rusak = $mentah;
        $rusak[$posisi] = chr(ord($rusak[$posisi]) ^ 0xFF);

        $this->assertNull($this->layanan()->dekripsi('v1:'.base64_encode($rusak)));
    }

    public function test_dekripsi_menolak_kode_verifikasi_yang_diubah(): void
    {
        $mentah = base64_decode(substr(self::CIPHERTEXT_UJI, 3), true);
        $this->assertIsString($mentah);

        // Ubah satu byte pada kode verifikasi keaslian (byte ke-12 s.d. ke-27).
        $posisi = 12;
        $rusak = $mentah;
        $rusak[$posisi] = chr(ord($rusak[$posisi]) ^ 0xFF);

        $this->assertNull($this->layanan()->dekripsi('v1:'.base64_encode($rusak)));
    }

    public function test_dekripsi_nilai_tidak_valid_mengembalikan_null(): void
    {
        $layanan = $this->layanan();

        $this->assertNull($layanan->dekripsi(null));
        $this->assertNull($layanan->dekripsi(''));
        $this->assertNull($layanan->dekripsi(self::SUB_UJI));          // masih plaintext
        $this->assertNull($layanan->dekripsi('bukan-ciphertext'));      // tanpa prefix
        $this->assertNull($layanan->dekripsi('v2:'.$this->ciphertextBaru())); // versi lain
        $this->assertNull($layanan->dekripsi('v1:'));                   // kosong
        $this->assertNull($layanan->dekripsi('v1:not-base64!!!'));      // base64 rusak
        $this->assertNull($layanan->dekripsi('v1:'.base64_encode('terlalu-pendek')));
    }

    public function test_enkripsi_lalu_dekripsi_untuk_berbagai_nilai(): void
    {
        $layanan = $this->layanan();

        foreach ([
            '100000000000000000777',
            '106123456789012345678',
            'sub-dengan-tanda-baca',
            'nilai dengan spasi',
        ] as $asli) {
            $this->assertSame($asli, $layanan->dekripsi($layanan->enkripsi($asli)));
        }
    }

    public function test_model_mendekripsi_tanpa_basis_data(): void
    {
        $pengguna = new Pengguna();
        $pengguna->google_id = self::SUB_UJI;

        $atribut = $pengguna->getAttributes();

        $this->assertStringStartsWith('v1:', (string) $atribut['google_id']);
        $this->assertNotSame(self::SUB_UJI, $atribut['google_id']);
        $this->assertSame(self::SUB_UJI, $pengguna->google_id);
        $this->assertSame(64, strlen((string) $atribut['google_id_bidx']));
    }

    private function ciphertextBaru(): string
    {
        return (string) $this->layanan()->enkripsi(self::SUB_UJI);
    }
}
