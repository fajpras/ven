@extends('admin.layouts.app')

@section('title', 'Account')
@section('page_title', 'Account')

@section('content')
<div class="space-y-8">
    {{-- Search Bar (Note: Filter dropdown "Active" is REMOVED per directive) --}}
    <form method="GET" action="{{ route('admin.accounts') }}" class="w-full">
        <div class="relative flex items-center max-w-4xl">
            <svg class="w-5 h-5 absolute left-5 text-[#94A3B8] dark:text-[#71717A] pointer-events-none" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
            </svg>
            <input type="text"
                   name="cari"
                   value="{{ request('cari') }}"
                   placeholder="Search"
                   class="w-full h-12 pl-13 pr-6 rounded-full bg-[#EBEBEB] dark:bg-[#27272A] text-[#0F172A] dark:text-white placeholder-[#94A3B8] dark:placeholder-[#71717A] text-base font-normal outline-none focus:ring-2 focus:ring-[#BA18F5] transition-all">
        </div>
    </form>

    {{-- Account List --}}
    <div class="divide-y divide-[#E5E7EB] dark:divide-[#27272A] border-t border-b border-[#E5E7EB] dark:border-[#27272A]">
        @forelse ($pengguna as $user)
            <div class="py-3.5 flex flex-col md:flex-row items-start md:items-center justify-between gap-4 hover:bg-gray-50/50 dark:hover:bg-zinc-900/30 transition-colors px-2 rounded-xl">
                {{-- Avatar & Username --}}
                <div class="flex items-center gap-4 min-w-[260px]">
                    <img src="{{ $user->foto_profil ? asset('storage/' . $user->foto_profil) : 'https://ui-avatars.com/api/?name=' . urlencode($user->nama) . '&background=BA18F5&color=fff' }}"
                         alt="{{ $user->nama }}"
                         class="w-11 h-11 rounded-full object-cover border border-gray-100 dark:border-zinc-800 shadow-xs flex-shrink-0">
                    <div>
                        <span class="block text-xs text-[#94A3B8] dark:text-[#71717A] font-medium leading-none mb-1">username</span>
                        <span class="text-sm font-semibold text-[#0F172A] dark:text-white">{{ $user->nama_panggilan }}</span>
                    </div>
                </div>

                {{-- Name --}}
                <div class="min-w-[180px]">
                    <span class="block text-xs text-[#94A3B8] dark:text-[#71717A] font-medium leading-none mb-1">name</span>
                    <span class="text-sm font-medium text-[#0F172A] dark:text-white">{{ $user->nama }}</span>
                </div>

                {{-- Post --}}
                <div class="min-w-[50px]">
                    <span class="block text-xs text-[#94A3B8] dark:text-[#71717A] font-medium leading-none mb-1">post</span>
                    <span class="text-sm font-medium text-[#0F172A] dark:text-white">{{ $user->karya_count ?? $user->karya()->count() }}</span>
                </div>

                {{-- Exhibition --}}
                <div class="min-w-[60px]">
                    <span class="block text-xs text-[#94A3B8] dark:text-[#71717A] font-medium leading-none mb-1">exhibition</span>
                    <span class="text-sm font-medium text-[#0F172A] dark:text-white">{{ $user->pameran_count ?? $user->pameran()->count() }}</span>
                </div>

                {{-- Join Date --}}
                <div class="min-w-[80px]">
                    <span class="block text-xs text-[#94A3B8] dark:text-[#71717A] font-medium leading-none mb-1">join date</span>
                    <span class="text-sm font-medium text-[#0F172A] dark:text-white">{{ $user->created_at ? $user->created_at->format('d/m/y') : '29/09/26' }}</span>
                </div>

                {{-- Action Button: Suspend (Red Pill) --}}
                <div class="flex items-center">
                    <button type="button"
                            onclick="openSuspendModal('{{ $user->id_pengguna }}', '{{ addslashes($user->nama) }}', '{{ addslashes($user->nama_panggilan) }}')"
                            class="h-9 px-6 rounded-full bg-[#FF0000] text-white font-semibold text-xs tracking-wide shadow-sm hover:bg-red-600 active:scale-95 transition-all cursor-pointer">
                        Suspend
                    </button>
                </div>
            </div>
        @empty
            <div class="py-16 text-center text-[#94A3B8] dark:text-[#71717A]">
                No user accounts found.
            </div>
        @endforelse
    </div>

    @if ($pengguna instanceof \Illuminate\Pagination\LengthAwarePaginator)
        {{ $pengguna->withQueryString()->links('admin.layouts.pagination') }}
    @endif
</div>

{{-- Suspend Modal Dialog --}}
<div id="suspendModal" class="fixed inset-0 z-50 hidden items-center justify-center p-4">
    <div class="fixed inset-0 bg-black/60 backdrop-blur-xs" onclick="closeSuspendModal()"></div>
    <div class="relative w-full max-w-lg bg-white dark:bg-[#1E1E20] rounded-3xl p-6 sm:p-8 shadow-2xl border border-[#E5E7EB] dark:border-[#2D2D30] z-10">
        <div class="flex items-center justify-between pb-4 border-b border-[#E5E7EB] dark:border-[#2D2D30] mb-6">
            <h2 class="text-xl font-bold text-[#0F172A] dark:text-white">Suspend User Account</h2>
            <button type="button" onclick="closeSuspendModal()" class="p-1.5 rounded-full hover:bg-gray-100 dark:hover:bg-zinc-800 text-gray-500 hover:text-black dark:text-zinc-400 dark:hover:text-white transition-colors cursor-pointer">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
            </button>
        </div>

        <form id="suspendForm" method="POST" action="" class="space-y-5">
            @csrf
            <div class="p-4 bg-gray-50 dark:bg-zinc-800/50 rounded-2xl">
                <p id="modalUserName" class="font-semibold text-base text-[#0F172A] dark:text-white"></p>
                <p id="modalUserHandle" class="text-xs text-[#94A3B8] dark:text-[#71717A]"></p>
            </div>

            <div>
                <label class="block text-xs font-semibold uppercase text-gray-500 dark:text-zinc-400 mb-2">Sanction Type</label>
                <select name="jenis_sanksi" class="w-full h-11 px-4 rounded-xl bg-gray-100 dark:bg-zinc-800 text-[#0F172A] dark:text-white text-sm outline-none border border-transparent focus:border-[#BA18F5]">
                    <option value="ditangguhkan">Suspension (Temporary)</option>
                    <option value="diblokir">Permanent Ban (Blocked)</option>
                </select>
            </div>

            <div>
                <label class="block text-xs font-semibold uppercase text-gray-500 dark:text-zinc-400 mb-2">Duration</label>
                <select name="duration" class="w-full h-11 px-4 rounded-xl bg-gray-100 dark:bg-zinc-800 text-[#0F172A] dark:text-white text-sm outline-none border border-transparent focus:border-[#BA18F5]">
                    <option value="24h">24 Hours</option>
                    <option value="7d">7 Days</option>
                    <option value="30d">30 Days</option>
                    <option value="permanent">Permanent</option>
                </select>
            </div>

            <div>
                <label class="block text-xs font-semibold uppercase text-gray-500 dark:text-zinc-400 mb-2">Reason for Suspension *</label>
                <textarea name="alasan" rows="3" required placeholder="Specify policy violation reason..." class="w-full p-4 rounded-xl bg-gray-100 dark:bg-zinc-800 text-[#0F172A] dark:text-white placeholder-gray-400 text-sm outline-none border border-transparent focus:border-[#BA18F5]"></textarea>
            </div>

            <div class="flex items-center justify-end gap-3 pt-4 border-t border-[#E5E7EB] dark:border-[#2D2D30]">
                <button type="button" onclick="closeSuspendModal()" class="h-10 px-5 rounded-full text-gray-600 dark:text-zinc-400 hover:bg-gray-100 dark:hover:bg-zinc-800 font-medium text-sm transition-colors cursor-pointer">
                    Cancel
                </button>
                <button type="submit" class="h-10 px-6 rounded-full bg-[#FF0000] text-white font-semibold text-sm shadow-md hover:bg-red-600 transition-colors cursor-pointer">
                    Confirm Suspend
                </button>
            </div>
        </form>
    </div>
</div>

@push('scripts')
<script>
    function openSuspendModal(userId, name, handle) {
        const modal = document.getElementById('suspendModal');
        const form = document.getElementById('suspendForm');
        document.getElementById('modalUserName').textContent = name;
        document.getElementById('modalUserHandle').textContent = '@' + handle;
        form.action = `/admin/accounts/${userId}/suspend`;
        modal.classList.remove('hidden');
        modal.classList.add('flex');
    }

    function closeSuspendModal() {
        const modal = document.getElementById('suspendModal');
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }
</script>
@endpush
@endsection
