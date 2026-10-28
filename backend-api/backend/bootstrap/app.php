<?php

use App\Console\Commands\BackupDatabase;
use App\Console\Commands\EnkripsiGoogleId;
use App\Console\Commands\InitializeDatabase;
use App\Console\Commands\RestoreDatabase;
use App\Console\Commands\SetupDatabase;
use App\Console\Commands\UjiTransaksiPameran;
use App\Http\Middleware\EnsureUserIsAdmin;
use Illuminate\Foundation\Application;
use Illuminate\Foundation\Configuration\Exceptions;
use Illuminate\Foundation\Configuration\Middleware;
use Illuminate\Http\Request;

return Application::configure(basePath: dirname(__DIR__))
    ->withRouting(
        web: __DIR__.'/../routes/web.php',
        api: __DIR__.'/../routes/api.php',
        commands: __DIR__.'/../routes/console.php',
        health: '/up',
    )
    ->withCommands([
        BackupDatabase::class,
        RestoreDatabase::class,
        SetupDatabase::class,
        InitializeDatabase::class,
        EnkripsiGoogleId::class,
        UjiTransaksiPameran::class,
    ])
    ->withMiddleware(function (Middleware $middleware): void {
        $middleware->alias([
            'admin' => EnsureUserIsAdmin::class,
        ]);
    })
    ->withExceptions(function (Exceptions $exceptions): void {
        $exceptions->shouldRenderJsonWhen(
            fn (Request $request) => $request->is('api/*') || $request->expectsJson(),
        );
    })
    ->create();
