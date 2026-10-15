@extends('admin.layouts.app')

@section('title', 'Exhibition')
@section('page_title', 'Exhibition')

@section('content')
<div class="space-y-8">
    {{-- Search Bar --}}
    <form method="GET" action="{{ route('admin.exhibitions') }}" class="w-full">
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

    {{-- Exhibition List Rows --}}
    <div class="divide-y divide-[#E5E7EB] dark:divide-[#27272A] border-t border-b border-[#E5E7EB] dark:border-[#27272A]">
        @forelse ($pameran as $exhibit)
            <div class="py-3.5 flex flex-col md:flex-row items-start md:items-center justify-between gap-4 hover:bg-gray-50/50 dark:hover:bg-zinc-900/30 transition-colors px-2 rounded-xl">
                {{-- Landscape Banner Thumbnail --}}
                <div class="w-full md:w-[150px] h-[75px] flex-shrink-0 rounded-xl overflow-hidden shadow-xs bg-gray-100 dark:bg-zinc-800">
                    <img src="{{ $exhibit->banner ? asset('storage/' . $exhibit->banner) : ($exhibit->banner_url ?? 'https://images.unsplash.com/photo-1550745165-9bc0b252726f?w=600&auto=format&fit=crop&q=80') }}"
                         alt="{{ $exhibit->judul }}"
                         class="w-full h-full object-cover hover:scale-105 transition-transform duration-300">
                </div>

                {{-- Title --}}
                <div class="min-w-[160px]">
                    <span class="block text-xs text-[#94A3B8] dark:text-[#71717A] font-medium leading-none mb-1">title</span>
                    <span class="text-base font-bold text-[#0F172A] dark:text-white">{{ $exhibit->judul }}</span>
                </div>

                {{-- Owner --}}
                <div class="min-w-[130px]">
                    <span class="block text-xs text-[#94A3B8] dark:text-[#71717A] font-medium leading-none mb-1">owner</span>
                    <span class="text-sm font-medium text-[#0F172A] dark:text-white">{{ $exhibit->pengguna->nama_panggilan ?? 'user' }}</span>
                </div>

                {{-- Post --}}
                <div class="min-w-[50px]">
                    <span class="block text-xs text-[#94A3B8] dark:text-[#71717A] font-medium leading-none mb-1">post</span>
                    <span class="text-sm font-medium text-[#0F172A] dark:text-white">{{ $exhibit->karya_count ?? $exhibit->ruangan()->count() }}</span>
                </div>

                {{-- Created At --}}
                <div class="min-w-[80px]">
                    <span class="block text-xs text-[#94A3B8] dark:text-[#71717A] font-medium leading-none mb-1">created at</span>
                    <span class="text-sm font-medium text-[#0F172A] dark:text-white">{{ $exhibit->created_at ? $exhibit->created_at->format('d/m/y') : '29/09/26' }}</span>
                </div>

                {{-- Action: Delete Button --}}
                <div class="flex items-center">
                    <button type="button"
                            onclick="openDeleteExhibitionModal('{{ $exhibit->id_pameran }}', '{{ addslashes($exhibit->judul) }}')"
                            class="h-9 px-6 rounded-full bg-[#FF0000] text-white font-semibold text-xs tracking-wide shadow-sm hover:bg-red-600 active:scale-95 transition-all cursor-pointer">
                        Delete
                    </button>
                </div>
            </div>
        @empty
            <div class="py-16 text-center text-[#94A3B8] dark:text-[#71717A]">
                No virtual exhibitions found.
            </div>
        @endforelse
    </div>

    @if ($pameran instanceof \Illuminate\Pagination\LengthAwarePaginator)
        {{ $pameran->withQueryString()->links('admin.layouts.pagination') }}
    @endif
</div>

{{-- Delete Confirmation Modal --}}
<div id="deleteExhibitionModal" class="fixed inset-0 z-50 hidden items-center justify-center p-4">
    <div class="fixed inset-0 bg-black/60 backdrop-blur-xs" onclick="closeDeleteExhibitionModal()"></div>
    <div class="relative w-full max-w-md bg-white dark:bg-[#1E1E20] rounded-3xl p-6 sm:p-8 shadow-2xl border border-[#E5E7EB] dark:border-[#2D2D30] z-10">
        <div class="flex items-center justify-between pb-4 border-b border-[#E5E7EB] dark:border-[#2D2D30] mb-6">
            <h2 class="text-xl font-bold text-[#0F172A] dark:text-white">Delete Exhibition</h2>
            <button type="button" onclick="closeDeleteExhibitionModal()" class="p-1.5 rounded-full hover:bg-gray-100 dark:hover:bg-zinc-800 text-gray-500 hover:text-black dark:text-zinc-400 dark:hover:text-white transition-colors cursor-pointer">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
            </button>
        </div>

        <form id="deleteExhibitionForm" method="POST" action="" class="space-y-5">
            @csrf
            @method('DELETE')
            <p class="text-sm text-[#0F172A] dark:text-[#D4D4D8]">
                Are you sure you want to delete exhibition <strong id="deleteExhibitTitle" class="text-black dark:text-white"></strong>? All public visits to this virtual room will be deactivated.
            </p>

            <div class="flex items-center justify-end gap-3 pt-4 border-t border-[#E5E7EB] dark:border-[#2D2D30]">
                <button type="button" onclick="closeDeleteExhibitionModal()" class="h-10 px-5 rounded-full text-gray-600 dark:text-zinc-400 hover:bg-gray-100 dark:hover:bg-zinc-800 font-medium text-sm transition-colors cursor-pointer">
                    Cancel
                </button>
                <button type="submit" class="h-10 px-6 rounded-full bg-[#FF0000] text-white font-semibold text-sm shadow-md hover:bg-red-600 transition-colors cursor-pointer">
                    Delete Exhibition
                </button>
            </div>
        </form>
    </div>
</div>

@push('scripts')
<script>
    function openDeleteExhibitionModal(id, title) {
        const modal = document.getElementById('deleteExhibitionModal');
        const form = document.getElementById('deleteExhibitionForm');
        document.getElementById('deleteExhibitTitle').textContent = `"${title}"`;
        form.action = `/admin/exhibitions/${id}`;
        modal.classList.remove('hidden');
        modal.classList.add('flex');
    }

    function closeDeleteExhibitionModal() {
        const modal = document.getElementById('deleteExhibitionModal');
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }
</script>
@endpush
@endsection
