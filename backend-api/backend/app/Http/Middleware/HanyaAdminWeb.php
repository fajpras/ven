<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Symfony\Component\HttpFoundation\Response;

class HanyaAdminWeb
{
    /**
     * Batasi halaman panel admin hanya untuk pengguna dengan role admin.
     */
    public function handle(Request $request, Closure $next): Response
    {
        $pengguna = Auth::guard('web')->user();

        if (! $pengguna?->isAdmin()) {
            Auth::guard('web')->logout();

            return redirect()
                ->route('admin.login')
                ->withErrors(['email' => 'Silakan masuk sebagai administrator.']);
        }

        return $next($request);
    }
}
