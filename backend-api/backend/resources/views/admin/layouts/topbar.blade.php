<header class="h-20 w-full flex items-center justify-between px-10 bg-white/70 dark:bg-[#121212]/70 backdrop-blur-md sticky top-0 z-20 transition-colors">
    <h1 class="text-2xl font-bold text-[#0F172A] dark:text-white tracking-tight">
        @yield('page_title', 'Dashboard')
    </h1>

    <div class="flex items-center gap-4">
        {{-- Theme switcher --}}
        <button type="button"
                onclick="toggleTheme()"
                aria-label="Toggle theme"
                class="p-2.5 rounded-full hover:bg-gray-100 dark:hover:bg-zinc-800 text-[#0F172A] dark:text-white transition-colors cursor-pointer"
                title="Toggle Light / Dark mode">
            {{-- Sun Icon (shown in dark mode) --}}
            <svg class="w-6 h-6 hidden dark:block text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"></path>
            </svg>
            {{-- Moon Icon (shown in light mode) --}}
            <svg class="w-6 h-6 block dark:hidden text-black fill-black" viewBox="0 0 24 24">
                <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
            </svg>
        </button>

        {{-- Logout button --}}
        <form method="POST" action="{{ route('admin.logout') }}" class="inline">
            @csrf
            <button type="submit"
                    aria-label="Log Out"
                    class="p-2.5 rounded-full hover:bg-red-50 dark:hover:bg-red-950/30 text-gray-500 hover:text-red-600 dark:text-zinc-400 dark:hover:text-red-400 transition-colors cursor-pointer"
                    title="Sign Out">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"></path>
                </svg>
            </button>
        </form>
    </div>
</header>
