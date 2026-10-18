<?php

namespace App\Http\Controllers\Admin\Web;

use App\Http\Controllers\Controller;
use App\Models\Pengguna;
use Illuminate\Http\RedirectResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Auth;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Str;
use Illuminate\View\View;

class AdminAuthController extends Controller
{
    public function showLoginForm(): View|RedirectResponse
    {
        if (Auth::guard('web')->check() && Auth::guard('web')->user()?->isAdmin()) {
            return redirect()->route('admin.accounts');
        }

        return view('admin.auth.login');
    }

    public function login(Request $request): RedirectResponse
    {
        $request->validate([
            'email' => ['required', 'string', 'email'],
            'password' => ['required', 'string'],
        ]);

        $user = Pengguna::where('email', Str::lower($request->input('email')))->first();

        if (! $user || ! $user->kata_sandi || ! Hash::check($request->input('password'), $user->kata_sandi)) {
            return back()->withInput()->withErrors([
                'email' => 'Invalid email or password provided.',
            ]);
        }

        if (! $user->isAdmin()) {
            return back()->withInput()->withErrors([
                'email' => 'Access denied. Only administrators can access this portal.',
            ]);
        }

        Auth::guard('web')->login($user);
        $request->session()->regenerate();

        return redirect()->intended(route('admin.accounts'));
    }

    public function logout(Request $request): RedirectResponse
    {
        Auth::guard('web')->logout();
        $request->session()->invalidate();
        $request->session()->regenerateToken();

        return redirect()->route('admin.login');
    }
}
