<?php

namespace App\Exceptions;

use Illuminate\Auth\AuthenticationException;
use Illuminate\Http\Exceptions\HttpResponseException;
use Illuminate\Http\Exceptions\PostTooLargeException;
use Illuminate\Http\Exceptions\ThrottleRequestsException;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Routing\Exceptions\InvalidSignatureException;
use Illuminate\Session\TokenMismatchException;
use Illuminate\Validation\ValidationException;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpKernel\Exception\HttpExceptionInterface;
use Symfony\Component\HttpKernel\Exception\MethodNotAllowedHttpException;
use Symfony\Component\HttpKernel\Exception\NotFoundHttpException;
use Throwable;

/**
 * Menyeragamkan seluruh respons galat untuk endpoint /api/* menjadi JSON.
 *
 * Tujuannya agar aplikasi klien (Android/Unity/Postman) tidak pernah menerima
 * halaman HTML, redirect ke halaman login web, atau stack trace ketika sebuah
 * endpoint tidak dapat diakses. Semua galat dikembalikan dengan bentuk yang
 * sama seperti respons sukses, yaitu { data, message }.
 */
final class PenanganKesalahanApi
{
    public const PESAN_SESI_BERAKHIR = 'Sesi berakhir atau token tidak valid. Silakan masuk kembali.';

    /** Pesan bawaan framework yang tidak perlu ditampilkan ke pengguna. */
    private const PESAN_BAWAAN = [
        'This action is unauthorized.',
        'Unauthenticated.',
        'Malformed URL.',
        'The page has expired due to inactivity.',
    ];

    /**
     * Ubah exception menjadi respons JSON untuk request /api/*.
     *
     * Mengembalikan null bila request bukan dari API, sehingga penanganan
     * bawaan Laravel (halaman web) tetap dipakai.
     */
    public static function render(Throwable $e, Request $request): ?JsonResponse
    {
        if (! $request->is('api', 'api/*')) {
            return null;
        }

        return match (true) {
            // Biarkan Laravel mengembalikan respons yang sudah dibentuk sendiri.
            $e instanceof HttpResponseException => null,
            $e instanceof AuthenticationException => self::buat(401, self::PESAN_SESI_BERAKHIR),
            $e instanceof ValidationException => self::buat(
                status: 422,
                pesan: $e->getMessage(),
                data: $e->errors(),
                // Dipertahankan agar klien lama yang membaca "errors" tetap bekerja.
                galat: $e->errors(),
            ),
            $e instanceof InvalidSignatureException => self::buat(403, 'Tautan tidak valid atau sudah kedaluwarsa.'),
            $e instanceof PostTooLargeException => self::buat(413, 'Data yang dikirim melebihi batas ukuran server.'),
            $e instanceof ThrottleRequestsException => self::buat(
                429,
                'Terlalu banyak permintaan. Silakan coba lagi dalam beberapa saat.',
                null,
                $e->getHeaders(),
            ),
            $e instanceof NotFoundHttpException => self::buat(404, 'Endpoint atau data yang diminta tidak ditemukan.'),
            $e instanceof MethodNotAllowedHttpException => self::buat(
                405,
                'Metode permintaan tidak didukung untuk endpoint ini.',
                null,
                $e->getHeaders(),
            ),
            $e instanceof TokenMismatchException => self::buat(419, 'Sesi formulir kedaluwarsa. Silakan muat ulang aplikasi.'),
            $e instanceof HttpExceptionInterface => self::buatHttp($e),
            // Jangan pernah bocorkan detail internal (query SQL, path, dsb.) ke klien.
            default => self::buat(500, 'Terjadi kesalahan pada server. Silakan coba lagi nanti.'),
        };
    }

    /**
     * Bentuk respons galat yang konsisten dengan envelope API VEN.
     */
    public static function buat(
        int $status,
        string $pesan,
        mixed $data = null,
        array $headers = [],
        array $galat = [],
    ): JsonResponse {
        $isi = ['data' => $data, 'message' => $pesan];

        if ($galat !== []) {
            $isi['errors'] = $galat;
        }

        return response()->json(
            $isi,
            $status,
            $headers,
            JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES,
        );
    }

    private static function buatHttp(HttpExceptionInterface $e): JsonResponse
    {
        $status = $e->getStatusCode();
        $pesan = trim($e->getMessage());

        // Pesan galat 4xx berasal dari kode aplikasi (mis. abort(403, '...')),
        // sehingga aman ditampilkan. Galat 5xx selalu memakai pesan umum agar
        // detail internal tidak bocor.
        if ($status >= 500 || $pesan === '' || in_array($pesan, self::PESAN_BAWAAN, true)) {
            $pesan = self::pesanStatus($status);
        }

        return self::buat($status, $pesan, null, $e->getHeaders());
    }

    private static function pesanStatus(int $status): string
    {
        return match ($status) {
            400 => 'Permintaan tidak dapat diproses.',
            401 => self::PESAN_SESI_BERAKHIR,
            403 => 'Anda tidak memiliki hak akses ke endpoint ini.',
            404 => 'Endpoint atau data yang diminta tidak ditemukan.',
            405 => 'Metode permintaan tidak didukung untuk endpoint ini.',
            408 => 'Permintaan melebihi batas waktu. Silakan coba lagi.',
            413 => 'Data yang dikirim melebihi batas ukuran server.',
            415 => 'Format data yang dikirim tidak didukung.',
            419 => 'Sesi formulir kedaluwarsa. Silakan muat ulang aplikasi.',
            422 => 'Data yang dikirim tidak valid.',
            429 => 'Terlalu banyak permintaan. Silakan coba lagi dalam beberapa saat.',
            502, 503, 504 => 'Layanan sedang tidak tersedia. Silakan coba beberapa saat lagi.',
            default => $status >= 500
                ? 'Terjadi kesalahan pada server. Silakan coba lagi nanti.'
                : (Response::$statusTexts[$status] ?? 'Permintaan tidak dapat diproses.'),
        };
    }
}
