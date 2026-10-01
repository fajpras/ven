<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Symfony\Component\HttpFoundation\Response;

class EnsureUserIsAdmin
{
    public function handle(Request $request, Closure $next): Response
    {
        $pengguna = $request->user();

        if (! $pengguna || $pengguna->role !== 'admin') {
            return response()->json([
                'message' => 'Forbidden: akses khusus admin.',
            ], 403);
        }

        return $next($request);
    }
}
