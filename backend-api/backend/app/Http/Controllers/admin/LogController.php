<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\LogAktifitas;
use App\Models\LogKeamanan;
use App\Models\Pengguna;
use Illuminate\Http\Request;
use Spatie\QueryBuilder\AllowedFilter;
use Spatie\QueryBuilder\QueryBuilder;

/**
 * Gabungan LogAktifitas + LogKeamanan (read-only).
 * Data log diisi otomatis lewat middleware/event listener, bukan lewat endpoint publik.
 */
class LogController extends Controller
{
    public function aktifitasIndex(Request $request)
    {
        $log = QueryBuilder::for(LogAktifitas::class)
            ->allowedFilters([
                'entitas',
                'status',
                AllowedFilter::callback('id_pengguna', fn ($q, $v) => $q->where('id_pengguna', Pengguna::uuidToBytes($v))),
                AllowedFilter::callback('from', fn ($q, $v) => $q->where('waktu', '>=', $v)),
                AllowedFilter::callback('to', fn ($q, $v) => $q->where('waktu', '<=', $v)),
            ])
            ->allowedSorts(['waktu', 'id_log'])
            ->defaultSort('-id_log')
            ->paginate(50);

        return $this->ok($log);
    }

    public function aktifitasShow(Request $request, string $id)
    {
        return $this->ok(LogAktifitas::findOrFail($id)->load('pengguna'));
    }

    public function keamananIndex(Request $request)
    {
        $log = QueryBuilder::for(LogKeamanan::class)
            ->allowedFilters([
                'event',
                'endpoint',
                'method',
                'ip_address',
                AllowedFilter::exact('status_kode'),
                AllowedFilter::callback('id_pengguna', fn ($q, $v) => $q->where('id_pengguna', Pengguna::uuidToBytes($v))),
                AllowedFilter::callback('from', fn ($q, $v) => $q->where('waktu', '>=', $v)),
                AllowedFilter::callback('to', fn ($q, $v) => $q->where('waktu', '<=', $v)),
            ])
            ->allowedSorts(['waktu', 'id_log', 'status_kode'])
            ->defaultSort('-id_log')
            ->paginate(50);

        return $this->ok($log);
    }

    public function keamananShow(Request $request, string $id)
    {
        return $this->ok(LogKeamanan::findOrFail($id)->load('pengguna'));
    }
}
