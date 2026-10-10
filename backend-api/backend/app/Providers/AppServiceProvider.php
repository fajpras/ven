<?php

namespace App\Providers;

use App\Contracts\PembuatKodeOtp;
use App\Listeners\CatatEventAuth;
use App\Models\Karya;
use App\Models\Objek;
use App\Models\Pameran;
use App\Models\Pengguna;
use App\Models\Ruangan;
use App\Models\SanksiAkun;
use App\Models\Undangan;
use App\Observers\AktivitasObserver;
use App\Services\Otp\LayananOtp;
use App\Services\Otp\PembuatKodeAcak;
use Illuminate\Contracts\Foundation\Application;
use Illuminate\Support\Facades\Event;
use Illuminate\Support\ServiceProvider;

class AppServiceProvider extends ServiceProvider
{
    public function register(): void
    {
        $this->app->bind(PembuatKodeOtp::class, PembuatKodeAcak::class);

        $this->app->singleton(LayananOtp::class, fn (Application $app): LayananOtp => new LayananOtp(
            $app->make(PembuatKodeOtp::class),
            (string) $app['config']->get('app.key'),
        ));
    }

    public function boot(): void
    {
        foreach ([Pengguna::class, Pameran::class, Karya::class, Objek::class, Ruangan::class, Undangan::class, SanksiAkun::class] as $model) {
            $model::observe(AktivitasObserver::class);
        }

        Event::subscribe(CatatEventAuth::class);
    }
}
