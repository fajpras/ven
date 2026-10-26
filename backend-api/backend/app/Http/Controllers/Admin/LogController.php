<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use App\Models\LogAktivitas;
use App\Models\LogKeamanan;
use Illuminate\Http\Request;
use Spatie\QueryBuilder\AllowedFilter;
use Spatie\QueryBuilder\QueryBuilder;

class LogController extends Controller
{
    public function aktivitasIndex(Request $request)
    {
        $request->validate(['filter.id_pengguna' => ['nullable', 'uuid']]);

        $log = QueryBuilder::for(LogAktivitas::class)
            ->allowedFilters(['entitas', 'status', 'jenis', 'aksi', 'pesan', ...$this->filterUmum()])
            ->allowedSorts(['waktu', 'id_log'])
            ->defaultSort('-id_log')
            ->paginate(50);

        return $this->ok($log);
    }

    public function aktivitasShow(LogAktivitas $log)
    {
        return $this->ok($log->load('pengguna'));
    }

    public function keamananIndex(Request $request)
    {
        $request->validate(['filter.id_pengguna' => ['nullable', 'uuid']]);

        $log = QueryBuilder::for(LogKeamanan::class)
            ->allowedFilters([
                'event', 'endpoint', 'method', 'ip_address', 'pesan',
                AllowedFilter::exact('status_kode'),
                AllowedFilter::callback('status', fn ($q, $v) => $q->where('status_kode', (int) $v)),
                ...$this->filterUmum(),
            ])
            ->allowedSorts(['waktu', 'id_log', 'status_kode'])
            ->defaultSort('-id_log')
            ->paginate(50);

        return $this->ok($log);
    }

    public function keamananShow(LogKeamanan $log)
    {
        return $this->ok($log->load('pengguna'));
    }

    private function filterUmum(): array
    {
        return [
            AllowedFilter::callback('id_pengguna', fn ($q, $v) => $q->where('id_pengguna', (string) $v)),
            AllowedFilter::callback('from', fn ($q, $v) => $q->where('waktu', '>=', $v)),
            AllowedFilter::callback('to', fn ($q, $v) => $q->where('waktu', '<=', $v)),
        ];
    }
}
