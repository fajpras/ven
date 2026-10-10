<?php

use App\Http\Controllers\Admin;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\Pengguna;
use App\Http\Middleware\AkunAktif;
use App\Http\Middleware\CatatKeamanan;
use App\Http\Middleware\HanyaAdmin;
use App\Http\Middleware\PilihKoneksi;
use Illuminate\Support\Facades\Route;

Route::middleware(CatatKeamanan::class)->group(function () {
    Route::prefix('auth')->controller(AuthController::class)->group(function () {
        Route::post('daftar/kirim-otp', 'kirimOtpRegistrasi')->middleware('throttle:5,1');
        Route::post('daftar', 'register')->middleware('throttle:5,1');
        Route::post('masuk', 'login')->middleware('throttle:5,1');
        Route::post('google/daftar', 'registerGoogle')->middleware('throttle:10,1');
        Route::post('google/masuk', 'loginGoogle')->middleware('throttle:10,1');
        Route::post('lupa-kata-sandi', 'lupaKataSandi')->middleware('throttle:3,1');
        Route::post('reset-kata-sandi', 'resetKataSandi')->middleware('throttle:5,1');
        Route::post('keluar', 'logout')->middleware('auth:sanctum');
    });

    Route::get('karya/{karya}/berkas', [Pengguna\KaryaController::class, 'berkas'])
        ->whereUuid('karya')->middleware(['signed', 'throttle:120,1'])->name('karya.berkas');

    Route::middleware(['auth:sanctum', AkunAktif::class, PilihKoneksi::class, 'throttle:120,1'])->group(function () {
        Route::controller(Pengguna\ProfilController::class)->prefix('profil')->group(function () {
            Route::get('/', 'show');
            Route::put('/', 'update');
            Route::post('email/kirim-otp', 'kirimOtpGantiEmail')->middleware('throttle:5,1');
            Route::put('email', 'gantiEmail')->middleware('throttle:5,1');
            Route::put('kata-sandi', 'updatePassword');
            Route::put('status', 'updateStatus');
            Route::get('minat', 'minat');
            Route::put('minat', 'updateMinat');
            Route::delete('minat/{kategori}', 'destroyMinat');
        });

        Route::controller(Pengguna\PameranController::class)->group(function () {
            Route::get('model-3d', 'templates');
            Route::get('model-3d/{model3d}', 'templateShow');
            Route::get('pameran', 'index');
            Route::post('pameran', 'store');
            Route::get('pameran/{pameran}', 'show')->whereNumber('pameran');
            Route::put('pameran/{pameran}', 'update')->whereNumber('pameran');
            Route::delete('pameran/{pameran}', 'destroy')->whereNumber('pameran');
            Route::post('pameran/{pameran}/ruangan', 'storeRuangan')->whereNumber('pameran');
        });

        Route::controller(Pengguna\KaryaController::class)->group(function () {
            Route::get('karya', 'index');
            Route::get('karya/rekomendasi', 'rekomendasi');
            Route::post('karya', 'store');
            Route::get('karya/{karya}', 'show')->whereUuid('karya');

            Route::match(['put', 'patch', 'post'], 'karya/{karya}', 'update')->whereUuid('karya');
            Route::delete('karya/{karya}', 'destroy')->whereUuid('karya');
            Route::put('karya/{karya}/posisi', 'updatePosisiObjek')->whereUuid('karya');
        });

        Route::controller(Pengguna\DiskusiController::class)->group(function () {
            Route::get('undangan', 'index');
            Route::post('undangan', 'store');
            Route::get('undangan/{undangan}', 'show');
            Route::get('undangan/{undangan}/status', 'status');
            Route::patch('undangan/{undangan}', 'tanggapi');
            Route::get('ruangan/{ruangan}/voice', 'voiceToken')->middleware('throttle:30,1');
        });

        Route::post('ai/tanya', [Pengguna\AIChatController::class, 'ask'])->middleware('throttle:10,1');

        Route::prefix('admin')->middleware(HanyaAdmin::class)->group(function () {
            Route::controller(Admin\PenggunaController::class)->group(function () {
                Route::get('pengguna', 'index');
                Route::get('pengguna/{pengguna}', 'show')->whereUuid('pengguna');
                Route::post('pengguna/{pengguna}/sanksi', 'sanksiStore')->whereUuid('pengguna');
                Route::get('sanksi', 'sanksiIndex');
                Route::get('sanksi/{sanksi}', 'sanksiShow');
                Route::patch('sanksi/{sanksi}', 'sanksiUpdate');
                Route::delete('sanksi/{sanksi}', 'sanksiCabut');
            });

            Route::controller(Admin\ModerasiController::class)->group(function () {
                Route::get('karya', 'karyaIndex');
                Route::get('karya/{karya}', 'karyaShow')->whereUuid('karya')->withTrashed();
                Route::delete('karya/{karya}', 'karyaDestroy')->whereUuid('karya');
                Route::post('karya/{karya}/pulihkan', 'karyaRestore')->whereUuid('karya')->withTrashed();
                Route::get('pameran', 'pameranIndex');
                Route::get('pameran/{pameran}', 'pameranShow')->whereNumber('pameran')->withTrashed();
                Route::delete('pameran/{pameran}', 'pameranDestroy')->whereNumber('pameran');
                Route::post('pameran/{pameran}/pulihkan', 'pameranRestore')->whereNumber('pameran')->withTrashed();
            });

            Route::controller(Admin\LogController::class)->prefix('log')->group(function () {
                Route::get('aktifitas', 'aktifitasIndex');
                Route::get('aktifitas/{log}', 'aktifitasShow');
                Route::get('keamanan', 'keamananIndex');
                Route::get('keamanan/{log}', 'keamananShow');
            });
        });
    });
});
