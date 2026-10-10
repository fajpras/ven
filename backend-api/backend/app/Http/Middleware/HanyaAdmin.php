<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;

class HanyaAdmin
{
    public function handle(Request $request, Closure $next)
    {
        if (! $request->user()?->isAdmin()) {
            return response()->json(['data' => null, 'message' => 'Anda tidak memiliki hak akses.'], 403);
        }

        return $next($request);
    }
}
