<?php

use App\Http\Controllers\Admin\KaryaController as AdminKaryaController;
use App\Http\Controllers\Admin\KategoriController as AdminKategoriController;
use App\Http\Controllers\Admin\LogController;
use App\Http\Controllers\Admin\PameranController as AdminPameranController;
use App\Http\Controllers\Admin\SanksiAkunController;
use App\Http\Controllers\AIChatController;
use App\Http\Controllers\AuthController;
use App\Http\Controllers\Model3DController;
use App\Http\Controllers\ObjekController;
use App\Http\Controllers\PasswordResetController;
use App\Http\Controllers\Pengguna\KaryaController as PenggunaKaryaController;
use App\Http\Controllers\Pengguna\PameranController as PenggunaPameranController;
use App\Http\Controllers\PenggunaKategoriController;
use App\Http\Controllers\PosisiObjekController;
use App\Http\Controllers\ProfileController;
use App\Http\Controllers\RekomendasiController;
use App\Http\Controllers\UndanganController;
use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| Publik (tanpa auth)
|--------------------------------------------------------------------------
*/
Route::post('/auth/register', [AuthController::class, 'register']);
Route::post('/auth/register/google', [AuthController::class, 'registerGoogle']);
Route::post('/auth/login', [AuthController::class, 'login']);
Route::post('/auth/login/google', [AuthController::class, 'loginGoogle']);

Route::post('/password/forgot', [PasswordResetController::class, 'forgot']);
Route::post('/password/reset', [PasswordResetController::class, 'reset']);

/*
|--------------------------------------------------------------------------
| Auth (auth:sanctum)
|--------------------------------------------------------------------------
*/
Route::middleware('auth:sanctum')->group(function () {
    Route::post('/auth/logout', [AuthController::class, 'logout']);

    // Profil
    Route::get('/profile', [ProfileController::class, 'show']);
    Route::put('/profile', [ProfileController::class, 'update']);
    Route::put('/profile/email', [ProfileController::class, 'updateEmail']);
    Route::put('/profile/password', [ProfileController::class, 'updatePassword']);
    Route::patch('/profile/status', [ProfileController::class, 'updateStatus']);

    // Karya & Pameran (pengguna)
    Route::apiResource('karya', PenggunaKaryaController::class);
    Route::apiResource('pameran', PenggunaPameranController::class);
    Route::post('/pameran/{pameran}/ruangan', [PenggunaPameranController::class, 'storeRuangan']);

    // Objek
    Route::apiResource('objek', ObjekController::class);
    Route::patch('/objek/{objek}/posisi', [PosisiObjekController::class, 'update']);

    // Model 3D
    Route::get('/model-3d', [Model3DController::class, 'index']);
    Route::get('/model-3d/{model3d}', [Model3DController::class, 'show']);

    // AI Chat
    Route::post('/ai-chat', [AIChatController::class, 'ask']);

    // Minat kategori pengguna
    Route::get('/pengguna/kategori-minat', [PenggunaKategoriController::class, 'show']);
    Route::put('/pengguna/kategori-minat', [PenggunaKategoriController::class, 'update']);

    // Rekomendasi
    Route::get('/rekomendasi', [RekomendasiController::class, 'index']);

    // Undangan
    Route::get('/undangan', [UndanganController::class, 'index']);
    Route::post('/undangan', [UndanganController::class, 'store']);
    Route::get('/undangan/{undangan}', [UndanganController::class, 'show']);
    Route::patch('/undangan/{undangan}', [UndanganController::class, 'update']);

    /*
    |----------------------------------------------------------------------
    | Admin (auth:sanctum + admin)
    |----------------------------------------------------------------------
    */
    Route::middleware('admin')->prefix('admin')->group(function () {
        // Karya
        Route::get('/karya', [AdminKaryaController::class, 'index']);
        Route::get('/karya/{id}', [AdminKaryaController::class, 'show']);
        Route::delete('/karya/{karya}', [AdminKaryaController::class, 'destroy']);

        // Kategori
        Route::apiResource('kategori', AdminKategoriController::class)->except(['show']);
        Route::get('/kategori/{kategori}', [AdminKategoriController::class, 'show']);

        // Log (read-only)
        Route::get('/log/aktifitas', [LogController::class, 'aktifitasIndex']);
        Route::get('/log/aktifitas/{id}', [LogController::class, 'aktifitasShow']);
        Route::get('/log/keamanan', [LogController::class, 'keamananIndex']);
        Route::get('/log/keamanan/{id}', [LogController::class, 'keamananShow']);

        // Pameran
        Route::get('/pameran', [AdminPameranController::class, 'index']);
        Route::get('/pameran/{id}', [AdminPameranController::class, 'show']);
        Route::delete('/pameran/{pameran}', [AdminPameranController::class, 'destroy']);

        // Sanksi akun (flat resource)
        Route::get('/sanksi-akun', [SanksiAkunController::class, 'index']);
        Route::post('/sanksi-akun', [SanksiAkunController::class, 'store']);
        Route::get('/sanksi-akun/{id}', [SanksiAkunController::class, 'show']);
        Route::patch('/sanksi-akun/{id}', [SanksiAkunController::class, 'update']);
    });
});
