<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="csrf-token" content="{{ csrf_token() }}">
    <title>Sign In - VEN Admin Panel</title>
    <link rel="icon" type="image/svg+xml" href="{{ asset('assets/admin/logo-ven.svg') }}">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
    @vite(['resources/css/app.css', 'resources/js/app.js'])
    <style>
        body {
            font-family: 'Plus Jakarta Sans', system-ui, -apple-system, sans-serif;
        }
    </style>
</head>
<body class="relative min-h-screen w-full flex items-center justify-center overflow-hidden bg-white select-none">
    {{-- Concentric Purple Gradient Background Circles --}}
    <div class="absolute inset-0 flex items-center justify-center pointer-events-none">
        {{-- Outer Circle --}}
        <div class="absolute w-[1400px] h-[1400px] rounded-full bg-gradient-to-b from-[#BA18F5]/15 via-[#C084FC]/25 to-transparent blur-3xl transform translate-y-12"></div>
        {{-- Middle Circles --}}
        <div class="absolute w-[1100px] h-[1100px] rounded-full bg-gradient-to-b from-[#BA18F5]/35 via-[#A855F7]/40 to-transparent blur-2xl transform translate-y-8"></div>
        <div class="absolute w-[850px] h-[850px] rounded-full bg-gradient-to-b from-[#BA18F5]/60 via-[#9333EA]/70 to-[#7E22CE]/80 transform translate-y-4"></div>
        {{-- Inner Circle Arc --}}
        <div class="absolute w-[650px] h-[650px] rounded-full bg-gradient-to-b from-[#A855F7] via-[#BA18F5] to-[#7E22CE]"></div>
    </div>

    {{-- Floating 3D Cubes (balok-1 and balok-2) --}}
    {{-- 1. Large Isometric Block (Bottom-Left) --}}
    <div class="absolute -bottom-8 -left-12 w-[340px] sm:w-[460px] md:w-[540px] pointer-events-none z-10 animate-float-slow">
        <img src="{{ asset('assets/admin/balok-1.svg') }}" alt="Decorative Cube Large Left" class="w-full h-auto drop-shadow-2xl opacity-95">
    </div>

    {{-- 2. Large Isometric Block (Bottom-Right, Mirrored) --}}
    <div class="absolute -bottom-8 -right-12 w-[340px] sm:w-[460px] md:w-[540px] pointer-events-none z-10 scale-x-[-1] animate-float-slow">
        <img src="{{ asset('assets/admin/balok-1.svg') }}" alt="Decorative Cube Large Right" class="w-full h-auto drop-shadow-2xl opacity-95">
    </div>

    {{-- 3. Small Isometric Block (Mid-Left Floating) --}}
    <div class="absolute top-[42%] left-[8%] sm:left-[14%] w-[110px] sm:w-[150px] md:w-[170px] pointer-events-none z-10 -rotate-12 animate-float-fast">
        <img src="{{ asset('assets/admin/balok-2.svg') }}" alt="Decorative Cube Small Left" class="w-full h-auto drop-shadow-xl">
    </div>

    {{-- 4. Small Isometric Block (Mid-Right Floating) --}}
    <div class="absolute top-[45%] right-[8%] sm:right-[14%] w-[110px] sm:w-[150px] md:w-[170px] pointer-events-none z-10 rotate-12 animate-float-fast">
        <img src="{{ asset('assets/admin/balok-2.svg') }}" alt="Decorative Cube Small Right" class="w-full h-auto drop-shadow-xl">
    </div>

    {{-- Center Login Card --}}
    <div class="relative z-20 w-full max-w-[420px] mx-4 bg-white rounded-3xl p-8 sm:p-10 shadow-[0_20px_50px_rgba(0,0,0,0.15)] border border-gray-100">
        <form method="POST" action="{{ route('admin.login.submit') }}" class="space-y-5">
            @csrf

            @if (isset($errors) && $errors->any())
                <div class="p-3 text-sm text-red-600 bg-red-50 border border-red-200 rounded-xl">
                    {{ $errors->first() }}
                </div>
            @endif

            {{-- Email Input --}}
            <div>
                <input type="email"
                       name="email"
                       value="{{ old('email', 'admin@ven.test') }}"
                       placeholder="Email"
                       required
                       class="w-full h-12 px-4 rounded-lg bg-[#EAEAEA] text-[#0F172A] placeholder-[#8E8E93] text-sm font-medium border border-transparent focus:border-[#BA18F5] focus:bg-white outline-none transition-all">
            </div>

            {{-- Password Input with Visibility Toggle --}}
            <div class="relative">
                <input type="password"
                       id="passwordInput"
                       name="password"
                       placeholder="Password"
                       required
                       class="w-full h-12 pl-4 pr-12 rounded-lg bg-[#EAEAEA] text-[#0F172A] placeholder-[#8E8E93] text-sm font-medium border border-transparent focus:border-[#BA18F5] focus:bg-white outline-none transition-all">
                <button type="button"
                        onclick="togglePasswordVisibility()"
                        class="absolute right-4 top-1/2 -translate-y-1/2 text-gray-500 hover:text-black transition-colors cursor-pointer">
                    <svg id="eyeIcon" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
                    </svg>
                </button>
            </div>

            {{-- Forgot Password Link --}}
            <div class="text-right">
                <a href="#"
                   onclick="alert('Please contact the super administrator to reset your password.'); return false;"
                   class="text-xs text-[#0F172A] hover:text-[#BA18F5] font-medium transition-colors">
                    Forgot Password?
                </a>
            </div>

            {{-- Submit Button --}}
            <button type="submit"
                    class="w-full h-12 rounded-lg bg-gradient-to-r from-[#BA18F5] to-[#D257FF] text-white font-semibold text-base shadow-lg shadow-purple-500/30 hover:opacity-95 active:scale-[0.99] transition-all flex items-center justify-center cursor-pointer">
                Sign In
            </button>
        </form>
    </div>

    <script>
        function togglePasswordVisibility() {
            const input = document.getElementById('passwordInput');
            if (input.type === 'password') {
                input.type = 'text';
            } else {
                input.type = 'password';
            }
        }
    </script>
</body>
</html>
