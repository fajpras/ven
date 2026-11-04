<?php

namespace App\Http\Middleware;

use Closure;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Symfony\Component\HttpFoundation\Response;

/**
 * Mengarahkan query panel admin web ke akun database dengan hak akses admin
 * (SELECT log, INSERT sanksi_akun, UPDATE status/deleted_at, dll).
 */
class GunakanKoneksiAdmin
{
    public function handle(Request $request, Closure $next): Response
    {
        DB::setDefaultConnection('mysql_admin');

        return $next($request);
    }
}
