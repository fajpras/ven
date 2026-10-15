<?php

use App\Http\Controllers\Admin\Web\AdminAccountController;
use App\Http\Controllers\Admin\Web\AdminAuthController;
use App\Http\Controllers\Admin\Web\AdminExhibitionController;
use App\Http\Controllers\Admin\Web\AdminLogController;
use App\Http\Controllers\Admin\Web\AdminPostController;
use Illuminate\Support\Facades\Route;

Route::get('/', function () {
    return redirect()->route('admin.login');
});

// Admin Authentication Routes
Route::prefix('admin')->group(function () {
    Route::get('/login', [AdminAuthController::class, 'showLoginForm'])->name('admin.login');
    Route::post('/login', [AdminAuthController::class, 'login'])->name('admin.login.submit');
    Route::post('/logout', [AdminAuthController::class, 'logout'])->name('admin.logout');

    Route::get('/', function () {
        return redirect()->route('admin.accounts');
    });

    // Admin Protected Views
    Route::get('/accounts', [AdminAccountController::class, 'index'])->name('admin.accounts');
    Route::post('/accounts/{id}/suspend', [AdminAccountController::class, 'suspend'])->name('admin.accounts.suspend');

    Route::get('/posts', [AdminPostController::class, 'index'])->name('admin.posts');
    Route::delete('/posts/{id}', [AdminPostController::class, 'destroy'])->name('admin.posts.destroy');

    Route::get('/exhibitions', [AdminExhibitionController::class, 'index'])->name('admin.exhibitions');
    Route::delete('/exhibitions/{id}', [AdminExhibitionController::class, 'destroy'])->name('admin.exhibitions.destroy');

    Route::get('/activity-log', [AdminLogController::class, 'index'])->name('admin.logs');
});
