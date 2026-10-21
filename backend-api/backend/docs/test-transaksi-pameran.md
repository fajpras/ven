# Modul Pengujian Transaksi — Menambah Pameran

Modul ini membuktikan bahwa proses **menambah pameran** berjalan secara
**atomic**: berhasil berarti semua perubahan disimpan (COMMIT), gagal berarti
semua perubahan dibatalkan (ROLLBACK).

Modul dibagi menjadi dua poin:

| Poin | Fokus | File test |
|---|---|---|
| **13** | Transaksi diterapkan + jalur sukses (COMMIT) | `tests/Feature/Transaksi/TambahPameranCommitTest.php` |
| **14** | Bukti COMMIT dan ROLLBACK + kondisi data sebelum/sesudah | `tests/Feature/Transaksi/TambahPameranRollbackTest.php` |

Total: **2 test, 26 assertion**.

---

## 1. File yang digunakan

| File | Peran |
|---|---|
| `tests/Feature/Transaksi/PersiapanTransaksiPameran.php` | Trait persiapan: membuat pengguna, model hall, payload, dan pembersihan data |
| `tests/Feature/Transaksi/TambahPameranCommitTest.php` | Poin 13 — jalur sukses dan COMMIT |
| `tests/Feature/Transaksi/TambahPameranRollbackTest.php` | Poin 14 — jalur gagal dan ROLLBACK |

Kode aplikasi yang diuji:

- `app/Http/Controllers/Pengguna/PameranController.php` → method `store()`
- `app/Observers/AktivitasObserver.php` → pencatatan log setelah COMMIT
- `routes/api.php` → `POST /api/pameran`

---

## 2. Prasyarat

1. Berada di folder backend:

   ```powershell
   cd C:\laragon\www\VEN\ven\backend-api\backend
   ```

2. Dependency sudah terpasang:

   ```powershell
   composer install
   ```

3. MySQL berjalan dan database `ven_testing` tersedia.

4. Konfigurasi test ada di `phpunit.xml` dan `.env.testing`.
   Pastikan `DB_DATABASE=ven_testing`, **bukan** `ven`.

5. Ekstensi PHP `gd` aktif (dipakai untuk membuat gambar banner palsu).

6. Jika `php` belum ada di PATH:

   ```powershell
   & "C:\laragon\bin\php\php-8.3.33-Win32-vs16-x64\php.exe" artisan test
   ```

---

## 3. Tatacara penggunaan testing

### 3.1 Menjalankan seluruh modul

```powershell
php artisan test tests/Feature/Transaksi
```

Hasil yang diharapkan:

```text
OK (2 tests, 26 assertions)
```

### 3.2 Menjalankan Poin 13 saja

```powershell
php artisan test tests/Feature/Transaksi/TambahPameranCommitTest.php
```

Hasil:

```text
OK (1 test, 15 assertions)
```

### 3.3 Menjalankan Poin 14 saja

```powershell
php artisan test tests/Feature/Transaksi/TambahPameranRollbackTest.php
```

Hasil:

```text
OK (1 test, 11 assertions)
```

### 3.4 Menjalankan satu method saja

```powershell
php artisan test --filter=test_tambah_pameran_berhasil_dan_transaksi_di_commit
php artisan test --filter=test_kegagalan_setelah_insert_menyebabkan_rollback
```

### 3.5 Menjalankan berdasarkan nama class

```powershell
php artisan test --filter=TambahPameranCommitTest
php artisan test --filter=TambahPameranRollbackTest
```

### 3.6 Melihat nama test dengan format lebih jelas

```powershell
php artisan test --testdox tests/Feature/Transaksi
```

Contoh output:

```text
Tambah Pameran Commit
  ✔ Tambah pameran berhasil dan transaksi di commit

Tambah Pameran Rollback
  ✔ Kegagalan setelah insert menyebabkan rollback

OK (2 tests, 26 assertions)
```

### 3.7 Menjalankan lewat PHPUnit langsung

```powershell
php vendor\bin\phpunit --testdox tests\Feature\Transaksi
```

> Jalankan perintah test **satu per satu**. Jangan menjalankan dua perintah
> test secara bersamaan pada satu folder, karena `Storage::fake()` memakai
> direktori testing yang sama.

---

## 4. Dasar transaksi yang dipakai

Transaksi adalah satu unit kerja yang harus selesai seluruhnya atau tidak
sama sekali. Sifat ini disebut **atomic**.

Di MySQL, alurnya:

```sql
START TRANSACTION;
-- perubahan 1
-- perubahan 2
COMMIT;      -- semua perubahan disimpan
-- atau
ROLLBACK;    -- semua perubahan dibatalkan
```

Laravel menyediakan pembungkus `DB::transaction()`:

```php
return DB::transaction(function () use ($request, $validated) {
    $path = null;

    if ($request->hasFile('banner')) {
        $path = $request->file('banner')->store('pameran/banner', 'public');
        $validated['banner'] = Storage::url($path);
    }

    try {
        $pameran = $request->user()->pameran()->create($validated);

        return $this->created($pameran->load('model3d'), 'Pameran dibuat.');
    } catch (\Throwable $e) {
        if ($path) {
            Storage::disk('public')->delete($path);
        }

        throw $e;
    }
});
```

Yang dilakukan framework:

1. `BEGIN` / `START TRANSACTION` sebelum closure dijalankan.
2. Jika closure selesai tanpa error → `COMMIT`.
3. Jika closure melempar error → `ROLLBACK`, lalu error diteruskan.

Proses tambah pameran mengubah dua hal di dalam transaksi:

1. **File banner** disimpan ke storage.
2. **Baris `pameran`** di-INSERT ke database.

Perubahan ketiga, yaitu **log aktivitas**, sengaja ditulis oleh observer
setelah COMMIT. Jadi log hanya muncul jika transaksi benar-benar berhasil.

---

## 5. Poin 13 — Transaksi dan jalur sukses

### 5.1 Yang dibuktikan

- Framework memulai transaksi sebelum menyimpan data.
- Baris pameran dan file banner tersimpan bersama.
- Transaksi berakhir dengan **COMMIT**.
- Tidak ada transaksi yang tertinggal setelah request.
- Log aktivitas baru ditulis setelah COMMIT.

### 5.2 Skenario test

| Langkah | Keterangan |
|---|---|
| 1 | Membuat pengguna uji dan satu model hall |
| 2 | Menyiapkan file banner palsu |
| 3 | Login sebagai pengguna uji (`Sanctum::actingAs`) |
| 4 | Mengirim `POST /api/pameran` |
| 5 | Endpoint memvalidasi input, membuka transaksi, menyimpan banner, dan INSERT pameran |
| 6 | Transaksi di-COMMIT |
| 7 | Observer menulis log aktivitas setelah COMMIT |
| 8 | Test memeriksa response, database, file, event transaksi, dan log |

### 5.3 Kondisi data sebelum request

| Yang diperiksa | Nilai |
|---|---|
| Baris `pameran` dengan judul uji | 0 |
| File banner baru di storage uji | 0 |
| Level transaksi (`DB::transactionLevel()`) | 0 |
| Log aktivitas untuk pameran ini | belum ada |

### 5.4 Kondisi data sesudah request

| Yang diperiksa | Nilai | Arti |
|---|---|---|
| Response HTTP | `201 Created` | Pameran berhasil dibuat |
| Baris `pameran` dengan judul uji | 1 | INSERT di-COMMIT |
| File banner baru | 1 | File tersimpan |
| Level transaksi | 0 | Transaksi sudah selesai |
| Event transaksi | `BEGIN`, `COMMIT` | Transaksi dimulai dan di-commit |
| Event `ROLLBACK` | tidak ada | Jalur sukses tidak rollback |
| Log aktivitas `pameran` | 1 baris baru | Log ditulis setelah COMMIT |

### 5.5 Bukti event transaksi

Test mendengarkan event bawaan Laravel:

```php
Event::listen(TransactionBeginning::class, fn () => $this->peristiwaTransaksi[] = 'BEGIN');
Event::listen(TransactionCommitted::class, fn () => $this->peristiwaTransaksi[] = 'COMMIT');
Event::listen(TransactionRolledBack::class, fn () => $this->peristiwaTransaksi[] = 'ROLLBACK');
```

Selain itu, test memeriksa level transaksi tepat saat model `Pameran`
hendak di-INSERT:

```php
Event::listen('eloquent.creating: '.Pameran::class, function (): void {
    $this->levelSaatInsert = DB::transactionLevel();
});

$this->assertSame(1, $this->levelSaatInsert);
```

Isi `$this->peristiwaTransaksi` pada jalur sukses:

```text
['BEGIN', 'COMMIT']
```

Itulah bukti bahwa framework benar-benar menjalankan `START TRANSACTION`
lalu `COMMIT`, bukan hanya menyimpan data tanpa transaksi. Level `1` saat
INSERT menunjukkan baris pameran masuk di dalam transaksi yang sama.

### 5.6 Hasil eksekusi

```text
OK (1 test, 15 assertions)
```

### 5.7 Cara membaca hasil

- Jika test lulus, artinya data dan file konsisten setelah COMMIT.
- Jika baris pameran ada tetapi file tidak ada, penyimpanan file perlu diperiksa.
- Jika file ada tetapi baris pameran tidak ada, proses COMMIT/INSERT bermasalah.
- Jika event `ROLLBACK` muncul pada jalur sukses, ada error yang tidak terduga.

---

## 6. Poin 14 — COMMIT dan ROLLBACK

### 6.1 Yang dibuktikan

- Keberhasilan menyebabkan **COMMIT**.
- Kegagalan menyebabkan **ROLLBACK**.
- Data sebelum dan sesudah kedua skenario dibandingkan.
- Tidak ada data atau file yang tertinggal setelah ROLLBACK.

### 6.2 Cara membuat kegagalan yang sah

Kegagalan tidak dibuat dengan mengirim input tidak valid, karena validasi
terjadi **sebelum** transaksi dibuka. Yang dibutuhkan adalah error yang
terjadi **setelah INSERT**, di dalam transaksi.

Test memasang listener model:

```php
Event::listen('eloquent.created: '.Pameran::class, function (): void {
    throw new RuntimeException('Simulasi kegagalan setelah INSERT pameran.');
});
```

Urutannya:

1. `BEGIN`
2. File banner disimpan.
3. Baris `pameran` di-INSERT.
4. Event `created` dipicu.
5. Listener melempar `RuntimeException`.
6. Controller menangkap error, menghapus file banner, lalu meneruskan error.
7. `DB::transaction()` melakukan `ROLLBACK`.
8. Framework mengembalikan response `500`.

Karena INSERT sudah benar-benar terjadi sebelum error, test ini membuktikan
bahwa ROLLBACK membatalkan data yang sudah masuk, bukan sekadar mencegah
INSERT sejak awal.

### 6.3 Perbandingan skenario

| Aspek | Skenario sukses | Skenario gagal |
|---|---|---|
| Response HTTP | `201 Created` | `500 Server Error` |
| Event transaksi | `BEGIN` → `COMMIT` | `BEGIN` → `ROLLBACK` |
| Baris `pameran` sesudah | 1 | 0 |
| File banner sesudah | 1 | 0 |
| Log aktivitas pameran | 1 | 0 |
| Level transaksi akhir | 0 | 0 |

### 6.4 Kondisi data sebelum dan sesudah — skenario sukses

| Tahap | Baris pameran | File banner | Log pameran | Event |
|---|---|---|---|---|
| Sebelum | 0 | 0 | belum ada | — |
| Sesudah | 1 | 1 | 1 | `BEGIN`, `COMMIT` |

### 6.5 Kondisi data sebelum dan sesudah — skenario gagal

| Tahap | Baris pameran | File banner | Log pameran | Event |
|---|---|---|---|---|
| Sebelum | 0 | 0 | belum ada | — |
| Sesudah | 0 | 0 | 0 | `BEGIN`, `ROLLBACK` |

### 6.6 Bukti di dalam test

```php
$respons->assertStatus(500);

$this->assertSame(0, $this->jumlahPameran());
$this->assertSame(0, DB::transactionLevel());
$this->assertCount(0, $berkasBaru);

$this->assertSame(1, $this->levelSaatInsert);
$this->assertContains('BEGIN', $this->peristiwaTransaksi);
$this->assertContains('ROLLBACK', $this->peristiwaTransaksi);
$this->assertNotContains('COMMIT', $this->peristiwaTransaksi);
```

Log aktivitas juga diperiksa: observer berjalan **after-commit**, sehingga
tidak boleh ada log baru saat transaksi di-rollback.

### 6.7 Hasil eksekusi

```text
OK (1 test, 11 assertions)
```

### 6.8 Cara membaca hasil

- Response `500` pada test ini **normal** dan disengaja.
- Yang penting adalah tidak ada baris pameran, file banner, atau log baru
  setelah response.
- Jika baris pameran masih ada, transaksi tidak mencakup INSERT atau
  ROLLBACK tidak berjalan.
- Jika file banner masih ada, pembersihan file di blok `catch` perlu diperiksa.

---

## 7. Mengapa test tidak memakai `RefreshDatabase`

`RefreshDatabase` membungkus setiap test dengan transaksi milik PHPUnit.
Akibatnya, `DB::transaction()` di controller hanya menjadi **savepoint**.
COMMIT yang terlihat di test bukan COMMIT sungguhan ke database.

Modul ini sengaja:

1. Membuat data unik per test (`Str::uuid()`).
2. Membersihkan data di `tearDown()`.
3. Tidak memakai `RefreshDatabase`.

Dengan begitu, event `COMMIT` dan `ROLLBACK` yang direkam adalah transaksi
nyata, sesuai yang diminta pada Poin 14.

---

## 8. Mengapa log aktivitas tidak ikut di-rollback

`AktivitasObserver` mengimplementasikan `ShouldHandleEventsAfterCommit`.
Artinya, pencatatan log ditunda sampai transaksi utama selesai:

- Jika COMMIT → log ditulis.
- Jika ROLLBACK → callback log dibuang, tidak ada log.

Ini bukan kekurangan, melainkan bukti tambahan bahwa sistem tidak mencatat
aktivitas yang gagal. Log juga menjadi indikator kedua bahwa COMMIT benar-
benar terjadi.

---

## 9. Troubleshooting

| Gejala | Penyebab umum | Solusi |
|---|---|---|
| `Unknown database 'ven_testing'` | Database test belum dibuat | Buat database `ven_testing` |
| `Class "GD" not found` | Ekstensi GD nonaktif | Aktifkan `extension=gd` di `php.ini` |
| Test gagal saat membuat gambar | GD tidak mendukung format | Pastikan GD aktif, lalu jalankan ulang |
| Response bukan 500 pada test rollback | Listener gagal dipasang | Pastikan listener dipasang di `setUp()` setelah aplikasi siap |
| Data lama menumpuk | Test terhenti sebelum `tearDown()` | Hapus data dengan judul `Pameran Uji Transaksi %` dan file di `storage/framework/testing/disks/public/pameran/banner` |
| `php` tidak dikenali | PHP belum ada di PATH | Gunakan path PHP Laragon seperti contoh di atas |
| Dua test dijalankan bersamaan | `Storage::fake()` berbagi direktori | Jalankan test secara berurutan |

---

## 10. Checklist untuk laporan

- [ ] Kode `PameranController@store()` memakai `DB::transaction()`.
- [ ] Poin 13: test sukses menghasilkan `201`, baris pameran, file banner,
      event `BEGIN` dan `COMMIT`.
- [ ] Poin 14: test gagal menghasilkan `500`, tidak ada baris pameran, tidak
      ada file banner, event `BEGIN` dan `ROLLBACK`.
- [ ] Tabel kondisi sebelum/sesudah untuk kedua skenario.
- [ ] Bukti event transaksi (`BEGIN`, `COMMIT`, `ROLLBACK`).
- [ ] Output test: `OK (2 tests, 26 assertions)`.

---

## 11. Referensi

- [Panduan menjalankan test](panduan-artisan-test.md)
- [Panduan test unit](panduan-unit-test.md)
- [Panduan tinker](panduan-tinker.md)
- [Daftar seluruh dokumen test](README.md)
