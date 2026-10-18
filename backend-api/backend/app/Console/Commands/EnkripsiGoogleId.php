<?php

namespace App\Console\Commands;

use App\Services\Crypto\LayananEnkripsi;
use Illuminate\Console\Command;
use Illuminate\Support\Facades\DB;

class EnkripsiGoogleId extends Command
{
    protected $signature = 'ven:enkripsi-google-id';

    protected $description = 'Mengenkripsi kolom google_id yang masih plaintext dan mengisi blind index-nya';

    public function handle(LayananEnkripsi $enkripsi): int
    {
        $total = 0;

        DB::table('pengguna')
            ->select('id_pengguna', 'google_id')
            ->whereNotNull('google_id')
            ->orderBy('id_pengguna')
            ->chunk(200, function ($baris) use ($enkripsi, &$total): void {
                foreach ($baris as $pengguna) {
                    if ($enkripsi->sudahTerenkripsi($pengguna->google_id)) {
                        continue;
                    }

                    DB::table('pengguna')
                        ->where('id_pengguna', $pengguna->id_pengguna)
                        ->update([
                            'google_id' => $enkripsi->enkripsi($pengguna->google_id),
                            'google_id_bidx' => $enkripsi->blindIndex($pengguna->google_id),
                        ]);

                    $total++;
                }
            });

        $this->info("Selesai. {$total} baris google_id dienkripsi.");

        return self::SUCCESS;
    }
}
