@extends('admin.layouts.app')

@section('title', 'Activity log')
@section('page_title', 'Activity log')

@section('content')
<div class="space-y-8">
    {{-- Search Bar --}}
    <form method="GET" action="{{ route('admin.logs') }}" class="w-full">
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

    {{-- Activity Log List Rows --}}
    <div class="divide-y divide-[#E5E7EB] dark:divide-[#27272A] border-t border-b border-[#E5E7EB] dark:border-[#27272A]">
        @forelse ($logs as $log)
            <div class="py-3 flex items-center justify-between gap-4 hover:bg-gray-50/50 dark:hover:bg-zinc-900/30 transition-colors px-2 rounded-xl">
                {{-- Action Description --}}
                <div class="flex-1 pr-4">
                    <p class="text-sm font-normal text-[#0F172A] dark:text-white leading-relaxed">
                        {{ $log->pesan ?? $log->deskripsi ?? 'Admin action performed on system' }}
                    </p>
                </div>

                {{-- Date --}}
                <div class="text-xs font-medium text-[#94A3B8] dark:text-[#71717A] min-w-[70px] text-right">
                    {{ $log->waktu ? $log->waktu->format('d/m/y') : ($log->created_at ? $log->created_at->format('d/m/y') : '29/09/26') }}
                </div>

                {{-- Detail Link (Purple) --}}
                <div class="min-w-[60px] text-right">
                    <button type="button"
                            onclick="openDetailModal({{ json_encode($log) }})"
                            class="text-sm font-medium text-[#BA18F5] hover:text-[#9333EA] transition-colors cursor-pointer">
                        Detail
                    </button>
                </div>
            </div>
        @empty
            <div class="py-16 text-center text-[#94A3B8] dark:text-[#71717A]">
                No activity logs recorded.
            </div>
        @endforelse
    </div>

    @if ($logs instanceof \Illuminate\Pagination\LengthAwarePaginator)
        {{ $logs->withQueryString()->links('admin.layouts.pagination') }}
    @endif
</div>

{{-- Detail Audit Trail Modal --}}
<div id="detailModal" class="fixed inset-0 z-50 hidden items-center justify-center p-4">
    <div class="fixed inset-0 bg-black/60 backdrop-blur-xs" onclick="closeDetailModal()"></div>
    <div class="relative w-full max-w-lg bg-white dark:bg-[#1E1E20] rounded-3xl p-6 sm:p-8 shadow-2xl border border-[#E5E7EB] dark:border-[#2D2D30] z-10">
        <div class="flex items-center justify-between pb-4 border-b border-[#E5E7EB] dark:border-[#2D2D30] mb-6">
            <h2 class="text-xl font-bold text-[#0F172A] dark:text-white">Activity Log Details</h2>
            <button type="button" onclick="closeDetailModal()" class="p-1.5 rounded-full hover:bg-gray-100 dark:hover:bg-zinc-800 text-gray-500 hover:text-black dark:text-zinc-400 dark:hover:text-white transition-colors cursor-pointer">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
            </button>
        </div>

        <div class="space-y-4">
            <div class="p-4 bg-purple-50 dark:bg-purple-950/20 rounded-2xl border border-purple-100 dark:border-purple-900/40">
                <p class="text-xs font-semibold uppercase text-[#BA18F5] mb-1">Action Description</p>
                <p id="modalDesc" class="text-sm font-semibold text-[#0F172A] dark:text-white"></p>
            </div>

            <div class="grid grid-cols-2 gap-4 text-sm">
                <div class="p-3 bg-gray-50 dark:bg-zinc-800/40 rounded-xl">
                    <span class="block text-xs text-[#94A3B8] dark:text-[#71717A] font-medium">Administrator</span>
                    <span id="modalAdmin" class="font-medium text-[#0F172A] dark:text-white">Admin 1</span>
                </div>

                <div class="p-3 bg-gray-50 dark:bg-zinc-800/40 rounded-xl">
                    <span class="block text-xs text-[#94A3B8] dark:text-[#71717A] font-medium">Timestamp</span>
                    <span id="modalTime" class="font-medium text-[#0F172A] dark:text-white"></span>
                </div>

                <div class="p-3 bg-gray-50 dark:bg-zinc-800/40 rounded-xl">
                    <span class="block text-xs text-[#94A3B8] dark:text-[#71717A] font-medium">IP Address</span>
                    <span id="modalIp" class="font-mono text-xs text-[#0F172A] dark:text-white"></span>
                </div>

                <div class="p-3 bg-gray-50 dark:bg-zinc-800/40 rounded-xl">
                    <span class="block text-xs text-[#94A3B8] dark:text-[#71717A] font-medium">Entity Target</span>
                    <span id="modalEntity" class="font-medium text-[#0F172A] dark:text-white"></span>
                </div>
            </div>

            <div class="p-3 bg-gray-50 dark:bg-zinc-800/40 rounded-xl">
                <span class="block text-xs text-[#94A3B8] dark:text-[#71717A] font-medium">Client User Agent</span>
                <p id="modalAgent" class="font-mono text-xs text-gray-600 dark:text-zinc-400 break-all mt-1"></p>
            </div>

            <div class="flex justify-end pt-4 border-t border-[#E5E7EB] dark:border-[#2D2D30]">
                <button type="button" onclick="closeDetailModal()" class="h-10 px-6 rounded-full bg-[#BA18F5] text-white font-medium text-sm hover:opacity-90 transition-opacity cursor-pointer">
                    Close
                </button>
            </div>
        </div>
    </div>
</div>

@push('scripts')
<script>
    function openDetailModal(log) {
        const modal = document.getElementById('detailModal');
        document.getElementById('modalDesc').textContent = log.pesan || log.deskripsi || 'Administrative action';
        document.getElementById('modalAdmin').textContent = log.pengguna ? log.pengguna.nama : 'Admin 1';
        document.getElementById('modalTime').textContent = log.waktu ? new Date(log.waktu).toLocaleString() : (log.created_at ? new Date(log.created_at).toLocaleString() : '29/09/26');
        document.getElementById('modalIp').textContent = log.ip_address || '127.0.0.1';
        document.getElementById('modalEntity').textContent = (log.entitas || log.tipe_entitas || 'General') + (log.id_entitas ? ' #' + log.id_entitas : (log.aksi ? ' (' + log.aksi + ')' : ''));
        document.getElementById('modalAgent').textContent = log.user_agent || navigator.userAgent;
        modal.classList.remove('hidden');
        modal.classList.add('flex');
    }

    function closeDetailModal() {
        const modal = document.getElementById('detailModal');
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }
</script>
@endpush
@endsection
