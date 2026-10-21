<?php

namespace Tests\Feature\Transaksi;

use App\Models\Pameran;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Event;
use Laravel\Sanctum\Sanctum;
use RuntimeException;
use Tests\TestCase;

/**
 * Poin 14 — Bukti COMMIT dan ROLLBACK.
 *
 * Skenario gagal dibuat dengan memicu error tepat setelah baris pameran
 * di-INSERT, yaitu di dalam transaksi yang sama. Dengan begitu kita dapat
 * membuktikan bahwa data yang sudah sempat masuk benar-benar dibatalkan.
 */
class TambahPameranRollbackTest extends TestCase
{
    use PersiapanTransaksiPameran;

    protected function setUp(): void
    {
        parent::setUp();

        $this->siapkanData();
        Sanctum::actingAs($this->pengguna);

        Event::listen('eloquent.created: '.Pameran::class, function (): void {
            throw new RuntimeException('Simulasi kegagalan setelah INSERT pameran.');
        });
    }

    protected function tearDown(): void
    {
        $this->bersihkanData();

        parent::tearDown();
    }

    public function test_kegagalan_setelah_insert_menyebabkan_rollback(): void
    {
        // Kondisi sebelum request.
        $this->assertSame(0, $this->jumlahPameran(), 'Belum ada pameran dengan judul uji.');
        $this->assertSame(0, DB::transactionLevel(), 'Tidak ada transaksi berjalan sebelum request.');

        $berkasSebelum = $this->fileBanner();
        $idLogSebelum = (int) DB::table('log_aktivitas')->max('id_log');

        $respons = $this->kirimTambahPameran();

        // Kegagalan diteruskan sebagai respons server error.
        $respons->assertStatus(500);

        // Kondisi sesudah request: semua perubahan dibatalkan.
        $this->assertSame(0, $this->jumlahPameran(), 'Baris pameran tidak boleh tersimpan setelah ROLLBACK.');
        $this->assertSame(0, DB::transactionLevel(), 'Transaksi harus selesai dan tidak tertinggal.');

        $berkasBaru = array_values(array_diff($this->fileBanner(), $berkasSebelum));
        $this->assertCount(0, $berkasBaru, 'File banner tidak boleh tertinggal setelah ROLLBACK.');

        // Bukti urutan transaksi: BEGIN lalu ROLLBACK, tanpa COMMIT.
        $this->assertSame(1, $this->levelSaatInsert, 'INSERT pameran harus berjalan di dalam transaksi.');
        $this->assertContains('BEGIN', $this->peristiwaTransaksi, 'Transaksi harus dimulai.');
        $this->assertContains('ROLLBACK', $this->peristiwaTransaksi, 'Transaksi harus di-rollback.');
        $this->assertNotContains('COMMIT', $this->peristiwaTransaksi, 'Jalur gagal tidak boleh commit.');

        // Observer aktivitas berjalan after-commit, jadi tidak boleh menulis log.
        $this->assertSame(
            0,
            DB::table('log_aktivitas')
                ->where('id_log', '>', $idLogSebelum)
                ->where('entitas', 'pameran')
                ->count(),
            'Log aktivitas tidak boleh ditulis jika transaksi di-rollback.'
        );
    }
}
