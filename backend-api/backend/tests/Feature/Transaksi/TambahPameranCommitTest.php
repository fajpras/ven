<?php

namespace Tests\Feature\Transaksi;

use App\Models\Pameran;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Storage;
use Laravel\Sanctum\Sanctum;
use Tests\TestCase;

/**
 * Poin 13 — Transaksi pada proses "tambah pameran" + jalur sukses.
 *
 * Membuktikan bahwa:
 * 1. Framework memulai transaksi sebelum menyimpan data.
 * 2. Baris pameran dan file banner tersimpan bersama.
 * 3. Transaksi di-COMMIT dan tidak ada transaksi yang tertinggal.
 * 4. Log aktivitas baru ditulis setelah COMMIT.
 */
class TambahPameranCommitTest extends TestCase
{
    use PersiapanTransaksiPameran;

    protected function setUp(): void
    {
        parent::setUp();

        $this->siapkanData();
        Sanctum::actingAs($this->pengguna);
    }

    protected function tearDown(): void
    {
        $this->bersihkanData();

        parent::tearDown();
    }

    public function test_tambah_pameran_berhasil_dan_transaksi_di_commit(): void
    {
        // Kondisi sebelum request.
        $this->assertSame(0, $this->jumlahPameran(), 'Belum ada pameran dengan judul uji.');
        $this->assertSame(0, DB::transactionLevel(), 'Tidak ada transaksi berjalan sebelum request.');

        $berkasSebelum = $this->fileBanner();
        $idLogSebelum = (int) DB::table('log_aktivitas')->max('id_log');

        $respons = $this->kirimTambahPameran();

        // Jalur sukses: endpoint mengembalikan 201.
        $respons->assertCreated()
            ->assertJsonPath('data.judul', $this->judul)
            ->assertJsonPath('data.id_model', $this->model->id_model);

        // Kondisi sesudah request: data dan file tersimpan.
        $this->assertSame(1, $this->jumlahPameran(), 'Baris pameran harus tersimpan setelah COMMIT.');
        $this->assertSame(0, DB::transactionLevel(), 'Transaksi harus selesai dan tidak tertinggal.');

        $pameran = Pameran::query()->where('judul', $this->judul)->firstOrFail();
        $pathBanner = str_replace('/storage/', '', (string) parse_url($pameran->banner, PHP_URL_PATH));

        Storage::disk('public')->assertExists($pathBanner);

        $berkasBaru = array_values(array_diff($this->fileBanner(), $berkasSebelum));
        $this->assertCount(1, $berkasBaru, 'Tepat satu file banner baru harus tersimpan setelah COMMIT.');
        $this->assertContains($pathBanner, $berkasBaru, 'File yang tersimpan harus file milik pameran ini.');

        // Bukti urutan transaksi: BEGIN lalu COMMIT, tanpa ROLLBACK.
        $this->assertSame(1, $this->levelSaatInsert, 'INSERT pameran harus berjalan di dalam transaksi.');
        $this->assertContains('BEGIN', $this->peristiwaTransaksi, 'Transaksi harus dimulai.');
        $this->assertContains('COMMIT', $this->peristiwaTransaksi, 'Transaksi harus di-commit.');
        $this->assertNotContains('ROLLBACK', $this->peristiwaTransaksi, 'Jalur sukses tidak boleh rollback.');

        // Log aktivitas ditulis oleh observer setelah COMMIT.
        $this->assertTrue(
            DB::table('log_aktivitas')
                ->where('id_log', '>', $idLogSebelum)
                ->where('entitas', 'pameran')
                ->where('jenis', 'pameran')
                ->where('pesan', 'like', "%pameran #{$pameran->id_pameran}%")
                ->exists(),
            'Log aktivitas harus tercatat setelah transaksi berhasil di-commit.'
        );
    }
}
