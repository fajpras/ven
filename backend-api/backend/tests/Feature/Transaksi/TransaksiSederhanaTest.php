<?php

namespace Tests\Feature\Transaksi;

use Illuminate\Database\Events\TransactionBeginning;
use Illuminate\Database\Events\TransactionCommitted;
use Illuminate\Database\Events\TransactionRolledBack;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Event;
use Illuminate\Support\Str;
use RuntimeException;
use Tests\TestCase;

/**
 * Versi paling sederhana dari uji transaksi.
 *
 * Hanya ada dua INSERT ke tabel log_aktivitas di dalam DB::transaction.
 * Tidak perlu login, upload file, model hall, atau endpoint HTTP.
 */
class TransaksiSederhanaTest extends TestCase
{
    private string $penanda;

    /** @var array<int, string> */
    private array $peristiwa = [];

    protected function setUp(): void
    {
        parent::setUp();

        // Penanda unik agar data test mudah dibersihkan.
        $this->penanda = 'UJI-TRANSAKSI-'.Str::uuid();

        Event::listen(TransactionBeginning::class, fn () => $this->peristiwa[] = 'BEGIN');
        Event::listen(TransactionCommitted::class, fn () => $this->peristiwa[] = 'COMMIT');
        Event::listen(TransactionRolledBack::class, fn () => $this->peristiwa[] = 'ROLLBACK');
    }

    protected function tearDown(): void
    {
        // Hapus hanya baris milik test ini.
        DB::table('log_aktivitas')
            ->where('pesan', 'like', $this->penanda.'%')
            ->delete();

        parent::tearDown();
    }

    public function test_dua_insert_berhasil_di_commit(): void
    {
        // 1. Kondisi awal.
        $sebelum = $this->jumlahBaris();

        // 2. Dua INSERT dalam satu transaksi.
        DB::transaction(function (): void {
            $this->tambahLog('baris pertama');
            $this->tambahLog('baris kedua');
        });

        // 3. Kedua baris harus tersimpan.
        $sesudah = $this->jumlahBaris();

        $this->assertSame(0, $sebelum);
        $this->assertSame(2, $sesudah);
        $this->assertSame(['BEGIN', 'COMMIT'], $this->peristiwa);

        $this->cetakBukti('COMMIT', $sebelum, $sesudah);
    }

    public function test_insert_pertama_dibatalkan_saat_proses_gagal(): void
    {
        // 1. Kondisi awal.
        $sebelum = $this->jumlahBaris();

        // 2. INSERT pertama berhasil, lalu proses sengaja digagalkan.
        try {
            DB::transaction(function (): void {
                $this->tambahLog('baris pertama');

                throw new RuntimeException('Gagal setelah INSERT pertama.');
            });
        } catch (RuntimeException) {
            // Error memang disengaja; yang diuji adalah efek ROLLBACK.
        }

        // 3. INSERT pertama harus ikut dibatalkan.
        $sesudah = $this->jumlahBaris();

        $this->assertSame(0, $sebelum);
        $this->assertSame(0, $sesudah);
        $this->assertSame(['BEGIN', 'ROLLBACK'], $this->peristiwa);

        $this->cetakBukti('ROLLBACK', $sebelum, $sesudah);
    }

    private function tambahLog(string $keterangan): void
    {
        DB::table('log_aktivitas')->insert([
            'entitas' => 'uji_transaksi',
            'status' => 'berhasil',
            'pesan' => $this->penanda.' '.$keterangan,
        ]);
    }

    private function jumlahBaris(): int
    {
        return DB::table('log_aktivitas')
            ->where('pesan', 'like', $this->penanda.'%')
            ->count();
    }

    private function cetakBukti(string $jenis, int $sebelum, int $sesudah): void
    {
        fwrite(STDERR, PHP_EOL.implode(PHP_EOL, [
            'BUKTI TRANSAKSI SEDERHANA — '.$jenis,
            '  sebelum : '.$sebelum.' baris',
            '  sesudah : '.$sesudah.' baris',
            '  event   : '.implode(' -> ', $this->peristiwa),
        ]).PHP_EOL);
    }
}
