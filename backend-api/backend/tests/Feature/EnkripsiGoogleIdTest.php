<?php

namespace Tests\Feature;

use App\Models\Pengguna;
use App\Services\Crypto\LayananEnkripsi;
use Illuminate\Support\Facades\DB;
use Tests\TestCase;

class EnkripsiGoogleIdTest extends TestCase
{
    protected function setUp(): void
    {
        parent::setUp();

        $this->assertSame(
            'ven_testing',
            DB::connection('mysql')->getDatabaseName(),
            'Test harus memakai database ven_testing, bukan database aplikasi.'
        );
    }

    public function test_google_id_tersimpan_terenkripsi_dan_accessor_mengembalikan_nilai_asli(): void
    {
        $asli = '100000000000000000777';

        $pengguna = Pengguna::factory()->denganGoogle()->create([
            'google_id' => $asli,
        ]);

        $mentah = DB::connection('mysql')->table('pengguna')
            ->where('id_pengguna', $pengguna->id_pengguna)
            ->value('google_id');

        $this->assertNotSame($asli, $mentah);
        $this->assertStringStartsWith('v1:', $mentah);
        $this->assertSame($asli, $pengguna->fresh()->google_id);
    }

    public function test_blind_index_tersimpan_dan_pencarian_menemukan_pengguna(): void
    {
        $asli = '100000000000000000888';

        $pengguna = Pengguna::factory()->denganGoogle()->create(['google_id' => $asli]);

        $bidx = app(LayananEnkripsi::class)->blindIndex($asli);

        $mentahBidx = DB::connection('mysql')->table('pengguna')
            ->where('id_pengguna', $pengguna->id_pengguna)
            ->value('google_id_bidx');

        $this->assertSame($bidx, $mentahBidx);

        $ditemukan = Pengguna::where('google_id_bidx', $bidx)->first();

        $this->assertNotNull($ditemukan);
        $this->assertSame($asli, $ditemukan->google_id);
    }

    public function test_data_rusak_di_database_tidak_membuat_crash(): void
    {
        $pengguna = Pengguna::factory()->denganGoogle()->create([
            'google_id' => '100000000000000000999',
        ]);

        DB::connection('mysql')->table('pengguna')
            ->where('id_pengguna', $pengguna->id_pengguna)
            ->update(['google_id' => 'v1:bukan-ciphertext-valid']);

        $this->assertNull($pengguna->fresh()->google_id);
    }
}
