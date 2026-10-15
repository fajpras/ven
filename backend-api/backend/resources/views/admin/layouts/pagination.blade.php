@if ($paginator->total() > 0)
    <nav role="navigation" aria-label="Pagination Navigation" class="flex flex-col sm:flex-row items-center justify-between gap-4 pt-6 border-t border-[#E5E7EB] dark:border-[#27272A] mt-6">
        {{-- Results Counter Summary --}}
        <div class="text-sm text-[#64748B] dark:text-[#A1A1AA]">
            <span>Showing</span>
            <span class="font-semibold text-[#0F172A] dark:text-white">{{ $paginator->firstItem() ?? 1 }}</span>
            <span>to</span>
            <span class="font-semibold text-[#0F172A] dark:text-white">{{ $paginator->lastItem() ?? $paginator->total() }}</span>
            <span>of</span>
            <span class="font-semibold text-[#0F172A] dark:text-white">{{ $paginator->total() }}</span>
            <span>results</span>
        </div>

        {{-- Page Navigation Controls --}}
        <div class="flex items-center gap-1.5">
            {{-- Previous Page Button --}}
            @if ($paginator->onFirstPage())
                <span class="w-9 h-9 rounded-full flex items-center justify-center text-[#94A3B8] dark:text-[#52525B] bg-gray-100/60 dark:bg-zinc-800/40 cursor-not-allowed select-none" aria-disabled="true">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
                    </svg>
                </span>
            @else
                <a href="{{ $paginator->previousPageUrl() }}" rel="prev" class="w-9 h-9 rounded-full flex items-center justify-center text-[#0F172A] dark:text-white bg-gray-100 hover:bg-gray-200 dark:bg-zinc-800 dark:hover:bg-zinc-700 transition-colors shadow-xs" aria-label="Previous page">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
                    </svg>
                </a>
            @endif

            {{-- Numeric Page Buttons --}}
            @foreach ($elements as $element)
                {{-- Array of links --}}
                @if (is_array($element))
                    @foreach ($element as $page => $url)
                        @if ($page == $paginator->currentPage())
                            <span aria-current="page" class="w-9 h-9 rounded-full flex items-center justify-center text-sm font-semibold text-white bg-[#BA18F5] shadow-md shadow-purple-500/25 select-none">
                                {{ $page }}
                            </span>
                        @else
                            <a href="{{ $url }}" class="w-9 h-9 rounded-full flex items-center justify-center text-sm font-medium text-[#0F172A] dark:text-white hover:bg-gray-100 dark:hover:bg-zinc-800 transition-colors">
                                {{ $page }}
                            </a>
                        @endif
                    @endforeach
                @endif

                {{-- Ellipsis Separator --}}
                @if (is_string($element))
                    <span class="w-9 h-9 rounded-full flex items-center justify-center text-sm text-[#94A3B8] dark:text-[#71717A] select-none">
                        {{ $element }}
                    </span>
                @endif
            @endforeach

            {{-- Next Page Button --}}
            @if ($paginator->hasMorePages())
                <a href="{{ $paginator->nextPageUrl() }}" rel="next" class="w-9 h-9 rounded-full flex items-center justify-center text-[#0F172A] dark:text-white bg-gray-100 hover:bg-gray-200 dark:bg-zinc-800 dark:hover:bg-zinc-700 transition-colors shadow-xs" aria-label="Next page">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                </a>
            @else
                <span class="w-9 h-9 rounded-full flex items-center justify-center text-[#94A3B8] dark:text-[#52525B] bg-gray-100/60 dark:bg-zinc-800/40 cursor-not-allowed select-none" aria-disabled="true">
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
                    </svg>
                </span>
            @endif
        </div>
    </nav>
@endif
