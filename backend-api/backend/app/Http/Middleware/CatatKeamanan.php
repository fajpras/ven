<?php

namespace App\Http\Middleware;

use App\Services\LogSistem;
use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class CatatKeamanan
{
    public function handle(Request $request, Closure $next): Response
    {
        return $next($request);
    }

    public function terminate(Request $request, Response $response): void
    {
        $status = $response->getStatusCode();

        if ($request->segment(2) === 'auth' || ! in_array($status, [401, 403, 429], true)) {
            return;
        }

        LogSistem::keamanan(match ($status) {
            401 => 'tidak_terautentikasi',
            403 => 'akses_ditolak',
            default => 'terlalu_banyak_permintaan',
        }, null, $status);
    }
}
