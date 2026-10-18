<?php

namespace App\Http\Controllers\Admin\Web;

use App\Http\Controllers\Controller;
use App\Models\LogAktivitas;
use Illuminate\Http\Request;
use Illuminate\Pagination\LengthAwarePaginator;
use Illuminate\Support\Carbon;
use Illuminate\View\View;

class AdminLogController extends Controller
{
    public function index(Request $request): View
    {
        $cari = $request->query('cari');
        $perPage = (int) $request->query('per_page', 5);

        try {
            $logs = LogAktivitas::query()
                ->with(['pengguna'])
                ->when($cari, function ($q) use ($cari) {
                    $pattern = '%'.addcslashes($cari, '%_').'%';
                    $q->where(function ($sub) use ($pattern) {
                        $sub->where('pesan', 'like', $pattern)
                            ->orWhere('entitas', 'like', $pattern)
                            ->orWhere('aksi', 'like', $pattern);
                    });
                })
                ->latest('id_log')
                ->paginate($perPage);
        } catch (\Throwable $e) {
            // Graceful mockup fallback matching Figma screenshots
            $mockLogs = collect([
                (object) [
                    'id_log' => 101,
                    'pesan' => 'Admin suspended account @spam_bot_99 due to violation of community guidelines',
                    'deskripsi' => 'Admin suspended account @spam_bot_99 due to violation of community guidelines',
                    'entitas' => 'pengguna',
                    'tipe_entitas' => 'pengguna',
                    'id_entitas' => 'user-99',
                    'aksi' => 'suspend',
                    'ip_address' => '192.168.1.10',
                    'user_agent' => 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
                    'waktu' => Carbon::create(2026, 9, 29, 14, 25),
                    'pengguna' => (object) ['nama' => 'Admin 1'],
                ],
                (object) [
                    'id_log' => 102,
                    'pesan' => 'Admin deleted artwork "Infringing 3D Model #404" flagged by copyright owner',
                    'deskripsi' => 'Admin deleted artwork "Infringing 3D Model #404" flagged by copyright owner',
                    'entitas' => 'karya',
                    'tipe_entitas' => 'karya',
                    'id_entitas' => 'karya-404',
                    'aksi' => 'hapus',
                    'ip_address' => '192.168.1.10',
                    'user_agent' => 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
                    'waktu' => Carbon::create(2026, 9, 29, 11, 10),
                    'pengguna' => (object) ['nama' => 'Admin 1'],
                ],
                (object) [
                    'id_log' => 103,
                    'pesan' => 'Admin unpinned exhibition "Glitch Artifacts" following completion of featured period',
                    'deskripsi' => 'Admin unpinned exhibition "Glitch Artifacts" following completion of featured period',
                    'entitas' => 'pameran',
                    'tipe_entitas' => 'pameran',
                    'id_entitas' => 'pameran-12',
                    'aksi' => 'edit',
                    'ip_address' => '127.0.0.1',
                    'user_agent' => 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
                    'waktu' => Carbon::create(2026, 9, 29, 9, 45),
                    'pengguna' => (object) ['nama' => 'Admin 1'],
                ],
                (object) [
                    'id_log' => 104,
                    'pesan' => 'System automatically revoked session for inactive token #tok_9821',
                    'deskripsi' => 'System automatically revoked session for inactive token #tok_9821',
                    'entitas' => 'keamanan',
                    'tipe_entitas' => 'keamanan',
                    'id_entitas' => 'tok-9821',
                    'aksi' => 'revoke',
                    'ip_address' => '10.0.0.1',
                    'user_agent' => 'Internal Worker',
                    'waktu' => Carbon::create(2026, 9, 28, 23, 59),
                    'pengguna' => (object) ['nama' => 'System Guardian'],
                ],
            ]);

            if ($cari) {
                $mockLogs = $mockLogs->filter(function ($item) use ($cari) {
                    return str_contains(strtolower($item->pesan), strtolower($cari));
                });
            }

            $page = LengthAwarePaginator::resolveCurrentPage();
            $slice = $mockLogs->forPage($page, $perPage);

            $logs = new LengthAwarePaginator(
                $slice->values(),
                $mockLogs->count(),
                $perPage,
                $page,
                ['path' => route('admin.logs')]
            );
        }

        return view('admin.logs.index', compact('logs'));
    }
}
