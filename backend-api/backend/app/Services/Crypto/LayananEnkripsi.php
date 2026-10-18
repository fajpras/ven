<?php

namespace App\Services\Crypto;

use Illuminate\Support\Facades\Log;
use RuntimeException;

/**
 * Enkripsi simetris AES-256-GCM untuk kolom sensitif (mis. google_id)
 * beserta blind index HMAC-SHA256 agar nilai terenkripsi tetap dapat dicari.
 *
 * Format payload: "v{versi}:{base64(IV || tag || ciphertext)}"
 * - IV  : 12 byte acak per operasi (Nonce GCM standar)
 * - tag : 16 byte Authentication Tag GCM
 */
final class LayananEnkripsi
{
    private const VERSI = 'v1';

    private const CIPHER = 'aes-256-gcm';

    private const PANJANG_IV = 12;

    private const PANJANG_TAG = 16;

    private readonly string $kunciCipher;

    private readonly string $kunciBlindIndex;

    public function __construct(string $kunciMaster)
    {
        if ($kunciMaster === '') {
            throw new RuntimeException('VEN_CUSTOM_KEY belum diisi. Enkripsi kolom sensitif tidak dapat dijalankan.');
        }

        $this->kunciCipher = hash('sha256', 'ven:aes-256-gcm:'.$kunciMaster, true);
        $this->kunciBlindIndex = hash_hmac('sha256', 'ven:blind-index', $kunciMaster, true);
    }

    public function enkripsi(?string $teks): ?string
    {
        if ($teks === null || $teks === '') {
            return null;
        }

        $iv = random_bytes(self::PANJANG_IV);
        $tag = '';

        $ciphertext = openssl_encrypt(
            $teks,
            self::CIPHER,
            $this->kunciCipher,
            OPENSSL_RAW_DATA,
            $iv,
            $tag,
            '',
            self::PANJANG_TAG,
        );

        if ($ciphertext === false) {
            throw new RuntimeException('Gagal mengenkripsi data.');
        }

        return self::VERSI.':'.base64_encode($iv.$tag.$ciphertext);
    }

    public function dekripsi(?string $payload): ?string
    {
        if ($payload === null || $payload === '') {
            return null;
        }

        if (! str_starts_with($payload, self::VERSI.':')) {
            return null;
        }

        $mentah = base64_decode(substr($payload, strlen(self::VERSI) + 1), true);

        if ($mentah === false || strlen($mentah) < self::PANJANG_IV + self::PANJANG_TAG) {
            $this->catatGagalDekripsi();

            return null;
        }

        $iv = substr($mentah, 0, self::PANJANG_IV);
        $tag = substr($mentah, self::PANJANG_IV, self::PANJANG_TAG);
        $ciphertext = substr($mentah, self::PANJANG_IV + self::PANJANG_TAG);

        $teks = openssl_decrypt(
            $ciphertext,
            self::CIPHER,
            $this->kunciCipher,
            OPENSSL_RAW_DATA,
            $iv,
            $tag,
        );

        if ($teks === false) {
            $this->catatGagalDekripsi();

            return null;
        }

        return $teks;
    }

    public function blindIndex(?string $teks): ?string
    {
        if ($teks === null || $teks === '') {
            return null;
        }

        return hash_hmac('sha256', $teks, $this->kunciBlindIndex);
    }

    public function sudahTerenkripsi(?string $nilai): bool
    {
        return is_string($nilai) && str_starts_with($nilai, self::VERSI.':');
    }

    private function catatGagalDekripsi(): void
    {
        Log::warning('Dekripsi kolom sensitif gagal (IV/tag rusak atau kunci tidak cocok).');
    }
}
