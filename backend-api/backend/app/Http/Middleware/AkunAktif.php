<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;

class AkunAktif
{
    public function handle(Request $request, Closure $next)
    {
        $user = $request->user();

        if ($user && $user->sanksiAktif()) {
            $user->currentAccessToken()?->delete();

            return response()->json(['data' => null, 'message' => 'Akun Anda sedang dikenai sanksi.'], 403);
        }

        return $next($request);
    }
}
