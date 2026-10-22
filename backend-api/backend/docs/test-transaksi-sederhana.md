# Test Transaksi Sederhana

Versi paling mudah untuk memahami COMMIT dan ROLLBACK.

**File test:** `tests/Feature/Transaksi/TransaksiSederhanaTest.php`  
**Jenis:** Feature (tanpa HTTP, login, atau upload)  
**Jumlah test:** 2  
**Assertion:** 6  
**Butuh database:** Ya (`ven_testing`)  
**Tabel yang dipakai:** `log_aktivitas` (tabel yang sudah ada)

---

## Kenapa lebih simpel?

Test ini tidak memerlukan:

- login atau `Sanctum::actingAs`
- upload file banner
- model hall
- endpoint HTTP
- file storage

Yang dilakukan hanya **dua INSERT** ke tabel `log_aktivitas` di dalam
`DB::transaction()`.

---

## Apa yang diuji?

| Method | Skenario | Hasil |
|---|---|---|
| `test_dua_insert_berhasil_di_commit` | Dua INSERT berhasil | 2 baris tersimpan, `BEGIN → COMMIT` |
| `test_insert_pertama_dibatalkan_saat_proses_gagal` | INSERT pertama berhasil, lalu error | 0 baris tersimpan, `BEGIN → ROLLBACK` |

---

## Cara menjalankan

```powershell
cd C:\laragon\www\VEN\ven\backend-api\backend
php artisan test tests/Feature/Transaksi/TransaksiSederhanaTest.php
```

Hasil yang diharapkan:

```text
OK (2 tests, 6 assertions)
```

Per method:

```powershell
php artisan test --filter=test_dua_insert_berhasil_di_commit
php artisan test --filter=test_insert_pertama_dibatalkan_saat_proses_gagal
```

Dengan nama test yang lebih jelas:

```powershell
php artisan test --testdox tests/Feature/Transaksi/TransaksiSederhanaTest.php
```

---

## Bukti sebelum dan sesudah

Test otomatis mencetak bukti seperti ini:

```text
BUKTI TRANSAKSI SEDERHANA — COMMIT
  sebelum : 0 baris
  sesudah : 2 baris
  event   : BEGIN -> COMMIT

BUKTI TRANSAKSI SEDERHANA — ROLLBACK
  sebelum : 0 baris
  sesudah : 0 baris
  event   : BEGIN -> ROLLBACK
```

---

## Kode inti

### Skenario berhasil

```php
DB::transaction(function (): void {
    $this->tambahLog('baris pertama');
    $this->tambahLog('baris kedua');
});
```

Semua INSERT selesai → `COMMIT` → dua baris tersimpan.

### Skenario gagal

```php
DB::transaction(function (): void {
    $this->tambahLog('baris pertama');

    throw new RuntimeException('Gagal setelah INSERT pertama.');
});
```

INSERT pertama sudah masuk, lalu error → `ROLLBACK` → baris pertama ikut
dibatalkan.

---

## Test langsung di database asli

Script siap pakai:

```text
database/scripts/uji-transaksi-db-asli.php
```

Jalankan:

```powershell
cd C:\laragon\www\VEN\ven\backend-api\backend
php artisan tinker database/scripts/uji-transaksi-db-asli.php
```

Contoh output:

```text
Database : ven
Marker   : UJI-TINKER-20261022005403-59b454

=== 1. COMMIT ===
Sebelum  : 0 baris
Sesudah  : 2 baris
  #13 UJI-TINKER-20261022005403-59b454 commit-1
  #14 UJI-TINKER-20261022005403-59b454 commit-2

=== 2. ROLLBACK ===
Sebelum  : 0 baris
Error    : Gagal sengaja setelah INSERT pertama.
Sesudah  : 0 baris

Bersih   : 2 baris dihapus.
Selesai.
```

Script ini:

- memakai database dari `.env` (biasanya `ven`);
- hanya menulis ke tabel `log_aktivitas`;
- memberi penanda unik pada setiap baris;
- menghapus datanya sendiri di akhir;
- otomatis keluar dari tinker setelah selesai.

Kalau ingin menjalankannya dari dalam tinker:

```php
require 'database/scripts/uji-transaksi-db-asli.php';
```

---

## Perbedaan dengan test pameran

| Aspek | Test sederhana | Test pameran |
|---|---|---|
| Login | Tidak | Ya |
| Upload banner | Tidak | Ya |
| Model hall | Tidak | Ya |
| Endpoint HTTP | Tidak | Ya |
| Tabel utama | `log_aktivitas` | `pameran` + file + log |
| Fokus | Mekanisme transaksi | Proses aplikasi nyata |
| Jumlah | 2 test, 6 assertion | 2 test, 26 assertion |

---

## Catatan

- Tidak memakai `RefreshDatabase`, sehingga COMMIT/ROLLBACK benar-benar
  terjadi di database.
- Data test diberi penanda unik dan dihapus lagi di `tearDown()`.
- Jika script DB asli terhenti paksa, hapus sisa datanya dengan:

  ```php
  DB::table('log_aktivitas')->where('pesan', 'like', 'UJI-TINKER-%')->delete();
  ```

- Test ini cocok untuk **menjelaskan konsep** transaksi.
- Untuk membuktikan proses aplikasi yang sebenarnya, gunakan
  [test-transaksi-pameran.md](test-transaksi-pameran.md).

---

## Referensi

- [Modul transaksi pameran](test-transaksi-pameran.md)
- [Panduan menjalankan test](panduan-artisan-test.md)
- [Daftar seluruh dokumen test](README.md)
