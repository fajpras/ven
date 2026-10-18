<?php

namespace Tests\Unit;

use App\Services\Crypto\LayananEnkripsi;
use Tests\TestCase;

class LayananEnkripsiTest extends TestCase
{
    private function layanan(): LayananEnkripsi
    {
        return new LayananEnkripsi('kunci-uji-enkripsi-0123456789abcdef');
    }

    public function test_enkripsi_lalu_dekripsi_menghasilkan_nilai_sama(): void
    {
        $layanan = $this->layanan();
        $asli = '100000000000000000001';

        $ciphertext = $layanan->enkripsi($asli);

        $this->assertNotSame($asli, $ciphertext);
        $this->assertSame($asli, $layanan->dekripsi($ciphertext));
    }

    public function test_enkripsi_plaintext_sama_menghasilkan_ciphertext_berbeda(): void
    {
        $layanan = $this->layanan();

        $pertama = $layanan->enkripsi('google-sub-abc');
        $kedua = $layanan->enkripsi('google-sub-abc');

        $this->assertNotSame($pertama, $kedua);
        $this->assertSame('google-sub-abc', $layanan->dekripsi($pertama));
        $this->assertSame('google-sub-abc', $layanan->dekripsi($kedua));
    }

    public function test_dekripsi_null_dan_plaintext_tanpa_prefix_mengembalikan_null(): void
    {
        $layanan = $this->layanan();

        $this->assertNull($layanan->dekripsi(null));
        $this->assertNull($layanan->dekripsi(''));
        $this->assertNull($layanan->dekripsi('bukan-ciphertext'));
    }

    public function test_dekripsi_data_rusak_menghasilkan_null_tanpa_crash(): void
    {
        $layanan = $this->layanan();
        $ciphertext = $layanan->enkripsi('google-sub-abc');

        $mentah = base64_decode(substr($ciphertext, 3), true);
        $rusak = $mentah;
        $rusak[0] = chr(ord($mentah[0]) ^ 0xFF);

        $this->assertNull($layanan->dekripsi('v1:'.base64_encode($rusak)));
        $this->assertNull($layanan->dekripsi('v1:not-base64!!!'));
    }

    public function test_blind_index_deterministik_dan_berbeda_untuk_nilai_berbeda(): void
    {
        $layanan = $this->layanan();

        $this->assertSame(
            $layanan->blindIndex('google-sub-abc'),
            $layanan->blindIndex('google-sub-abc'),
        );
        $this->assertNotSame(
            $layanan->blindIndex('google-sub-abc'),
            $layanan->blindIndex('google-sub-xyz'),
        );
        $this->assertNull($layanan->blindIndex(null));
    }

    public function test_sudah_terenkripsi_mengenali_format(): void
    {
        $layanan = $this->layanan();

        $this->assertTrue($layanan->sudahTerenkripsi($layanan->enkripsi('abc')));
        $this->assertFalse($layanan->sudahTerenkripsi('plaintext'));
        $this->assertFalse($layanan->sudahTerenkripsi(null));
    }
}
