<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Symfony\Component\HttpFoundation\Response;

class PilihKoneksi
{
    public function handle(Request $request, Closure $next): Response
    {
        $user = $request->user();

        if ($user) {
            $koneksi = match (true) {
                $user->isAdmin() && $request->segment(2) === 'admin' => 'mysql_admin',
                ! $request->isMethodSafe() => 'mysql_user',
                default => null,
            };

            if ($koneksi) {
                DB::setDefaultConnection($koneksi);
            }
        }

        return $next($request);
    }

    public function terminate(Request $request, Response $response): void
    {
        DB::setDefaultConnection(config('database.default'));
    }
}
