<aside class="w-20 min-h-screen bg-white dark:bg-[#121212] border-r border-[#E5E7EB] dark:border-[#27272A] flex flex-col items-center py-6 fixed left-0 top-0 z-30 transition-colors">
    {{-- Brand Logo --}}
    <div class="mb-10 flex items-center justify-center">
        <a href="{{ route('admin.accounts') }}">
            <img src="{{ asset('assets/admin/logo-ven.svg') }}" alt="VEN Logo" class="w-10 h-10 object-contain hover:scale-105 transition-transform">
        </a>
    </div>

    {{-- Navigation items --}}
    <nav class="flex flex-col gap-6 w-full items-center">
        {{-- 1. Account --}}
        @php $isAccount = request()->routeIs('admin.accounts*'); @endphp
        <a href="{{ route('admin.accounts') }}"
           title="Account"
           class="w-12 h-12 rounded-2xl flex items-center justify-center transition-all duration-200 {{ $isAccount ? 'bg-[#BA18F5] text-white shadow-lg shadow-purple-500/25' : 'text-[#64748B] dark:text-[#A1A1AA] hover:bg-gray-100 dark:hover:bg-zinc-800 hover:text-black dark:hover:text-white' }}">
            <img src="{{ asset('assets/admin/icon-account.svg') }}"
                 alt="Account"
                 class="w-6 h-6 {{ $isAccount ? 'filter brightness-0 invert' : 'dark:filter dark:brightness-0 dark:invert opacity-70 hover:opacity-100' }}">
        </a>

        {{-- 2. Post --}}
        @php $isPost = request()->routeIs('admin.posts*'); @endphp
        <a href="{{ route('admin.posts') }}"
           title="Post"
           class="w-12 h-12 rounded-2xl flex items-center justify-center transition-all duration-200 {{ $isPost ? 'bg-[#BA18F5] text-white shadow-lg shadow-purple-500/25' : 'text-[#64748B] dark:text-[#A1A1AA] hover:bg-gray-100 dark:hover:bg-zinc-800 hover:text-black dark:hover:text-white' }}">
            <img src="{{ asset('assets/admin/icon-post.svg') }}"
                 alt="Post"
                 class="w-6 h-6 {{ $isPost ? 'filter brightness-0 invert' : 'dark:filter dark:brightness-0 dark:invert opacity-70 hover:opacity-100' }}">
        </a>

        {{-- 3. Exhibition --}}
        @php $isExhibition = request()->routeIs('admin.exhibitions*'); @endphp
        <a href="{{ route('admin.exhibitions') }}"
           title="Exhibition"
           class="w-12 h-12 rounded-2xl flex items-center justify-center transition-all duration-200 {{ $isExhibition ? 'bg-[#BA18F5] text-white shadow-lg shadow-purple-500/25' : 'text-[#64748B] dark:text-[#A1A1AA] hover:bg-gray-100 dark:hover:bg-zinc-800 hover:text-black dark:hover:text-white' }}">
            <img src="{{ asset('assets/admin/icon-exhibition.svg') }}"
                 alt="Exhibition"
                 class="w-6 h-6 {{ $isExhibition ? 'filter brightness-0 invert' : 'dark:filter dark:brightness-0 dark:invert opacity-70 hover:opacity-100' }}">
        </a>

        {{-- 4. Activity log --}}
        @php $isLog = request()->routeIs('admin.logs*'); @endphp
        <a href="{{ route('admin.logs') }}"
           title="Activity log"
           class="w-12 h-12 rounded-2xl flex items-center justify-center transition-all duration-200 {{ $isLog ? 'bg-[#BA18F5] text-white shadow-lg shadow-purple-500/25' : 'text-[#64748B] dark:text-[#A1A1AA] hover:bg-gray-100 dark:hover:bg-zinc-800 hover:text-black dark:hover:text-white' }}">
            <img src="{{ asset('assets/admin/icon-history.svg') }}"
                 alt="Activity log"
                 class="w-6 h-6 {{ $isLog ? 'filter brightness-0 invert' : 'dark:filter dark:brightness-0 dark:invert opacity-70 hover:opacity-100' }}">
        </a>
    </nav>
</aside>
