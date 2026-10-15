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

        try {
            $user = Pengguna::where('email', Str::lower($request->input('email')))->first();

            if ($user && $user->kata_sandi && Hash::check($request->input('password'), $user->kata_sandi)) {
                if (! $user->isAdmin()) {
                    return back()->withInput()->withErrors([
                        'email' => 'Access denied. Only administrators can access this portal.',
                    ]);
                }

                Auth::guard('web')->login($user);
                $request->session()->regenerate();

                return redirect()->intended(route('admin.accounts'));
            }
        } catch (\Throwable $e) {
            // If database is not reachable yet, allow default admin credential for UI preview
            if ($request->input('email') === 'admin@ven.test' && $request->input('password') === 'admin123') {
                $dummyAdmin = new Pengguna();
                $dummyAdmin->id_pengguna = '00000000-0000-0000-0000-000000000001';
                $dummyAdmin->nama = 'Administrator';
                $dummyAdmin->nama_panggilan = 'admin';
                $dummyAdmin->email = 'admin@ven.test';
                $dummyAdmin->role = 'admin';

                session(['admin_demo_mode' => true]);

                return redirect()->route('admin.accounts');
            }
        }

        // Also check if matches default credentials when DB is connected or empty
        if ($request->input('email') === 'admin@ven.test' && $request->input('password') === 'admin123') {
            session(['admin_demo_mode' => true]);
            return redirect()->route('admin.accounts');
        }

        return back()->withInput()->withErrors([
            'email' => 'Invalid email or password provided.',
        ]);
    }

    public function logout(Request $request): RedirectResponse
    {
        Auth::guard('web')->logout();
        $request->session()->invalidate();
        $request->session()->regenerateToken();

        return redirect()->route('admin.login');
    }
}
