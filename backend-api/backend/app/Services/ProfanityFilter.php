<?php

namespace App\Services;

class ProfanityFilter
{
    /**
     * Memeriksa apakah teks mengandung kata kasar dari konfigurasi.
     *
     * @param string|null ...$texts Teks yang ingin diperiksa (judul, deskripsi, dll)
     * @return bool True jika terdeteksi kata kasar, false jika bersih.
     */
    public static function containsProfanity(?string ...$texts): bool
    {
        $bannedWords = config('exhibition_templates.banned_words', []);
        if (empty($bannedWords)) {
            return false;
        }

        foreach ($texts as $text) {
            if (empty($text)) {
                continue;
            }

            $normalized = strtolower($text);

            foreach ($bannedWords as $word) {
                $word = trim(strtolower($word));
                if ($word === '') {
                    continue;
                }

                // Gunakan regex boundary atau substring check untuk mendeteksi kata terlarang
                $pattern = '/\b' . preg_quote($word, '/') . '\b/i';
                if (preg_match($pattern, $normalized)) {
                    return true;
                }
            }
        }

        return false;
    }
}
