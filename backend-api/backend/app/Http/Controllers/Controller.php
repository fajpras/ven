<?php

namespace App\Http\Controllers;

use App\Models\Pengguna;
use Illuminate\Support\Str;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Validation\ValidationException;

abstract class Controller
{
    protected function ok(mixed $data = null, string $message = 'OK', int $status = 200): JsonResponse
    {
        return response()->json(['data' => $data, 'message' => $message], $status);
    }

    protected function created(mixed $data = null, string $message = 'Created'): JsonResponse
    {
        return $this->ok($data, $message, 201);
    }

    protected function noContent(): JsonResponse
    {
        return response()->json(null, 204);
    }

    protected function error(string $message, int $status = 400, mixed $data = null): JsonResponse
    {
        return response()->json(['data' => $data, 'message' => $message], $status);
    }

    protected function unauthorized(string $message = 'Sesi berakhir, silakan login kembali.'): JsonResponse
    {
        return $this->error($message, 401);
    }

    protected function forbidden(string $message = 'Anda tidak memiliki hak akses.'): JsonResponse
    {
        return $this->error($message, 403);
    }

    protected function notFound(string $message = 'Data tidak ditemukan.'): JsonResponse
    {
        return $this->error($message, 404);
    }

    protected function unprocessable(mixed $errors, string $message = 'Validasi data gagal.'): JsonResponse
    {
        return $this->error($message, 422, $errors);
    }

    protected function escapeLike(string $value): string
    {
        return addcslashes($value, '%_\\');
    }

    protected function pastikanPemilik(Request $request, ?string $idPemilik, bool $izinkanAdmin = false, string $pesan = 'Anda bukan pemilik data ini.'): void
    {
        $user = $request->user();

        if ($izinkanAdmin && $user->isAdmin()) {
            return;
        }

        if ($idPemilik === null || ! hash_equals($idPemilik, $user->id_pengguna)) {
            abort(403, $pesan);
        }
    }

    protected function uuidQuery(Request $request, string $key): ?string
    {
        $value = $request->query($key);

        if (! is_string($value) || $value === '') {
            return null;
        }

        if (! Str::isUuid($value)) {
            throw ValidationException::withMessages([$key => "$key harus berupa UUID."]);
        }

        return Str::lower($value);
    }
}
