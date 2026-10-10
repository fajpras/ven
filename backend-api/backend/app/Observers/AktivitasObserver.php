<?php

namespace App\Observers;

use App\Models\Pengguna;
use App\Services\LogSistem;
use Illuminate\Contracts\Events\ShouldHandleEventsAfterCommit;
use Illuminate\Database\Eloquent\Model;

class AktivitasObserver implements ShouldHandleEventsAfterCommit
{
    public function created(Model $m): void
    {
        $this->catat($m, 'membuat');
    }

    public function updated(Model $m): void
    {
        $kolom = array_values(array_diff(array_keys($m->getChanges()), ['updated_at', 'created_at']));

        if ($kolom) {
            $this->catat($m, 'mengubah', implode(', ', $kolom));
        }
    }

    public function deleted(Model $m): void
    {
        $this->catat($m, 'menghapus');
    }

    public function restored(Model $m): void
    {
        $this->catat($m, 'memulihkan');
    }

    private function catat(Model $m, string $aksi, ?string $detail = null): void
    {
        $key = $m->getKey();
        $label = (string) $key;

        $pelaku = ($m instanceof Pengguna && $aksi === 'membuat') ? $key : null;

        LogSistem::aktivitas(
            entitas: $m->getTable(),
            idEntitas: $key,
            pesan: "{$aksi} {$m->getTable()} #{$label}".($detail ? " ({$detail})" : ''),
            idPengguna: $pelaku,
        );
    }
}
