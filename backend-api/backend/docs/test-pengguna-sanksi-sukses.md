# Test Pengguna Sanksi — Skenario Berhasil

**File test:** `tests/Feature/PenggunaSanksiSuksesTest.php`  
**Class:** `PenggunaSanksiSuksesTest`  
**Jenis:** Feature  
**Jumlah test:** 1  
**Butuh database:** Ya (`ven_testing`)

## Tujuan

Memastikan admin dapat memberikan sanksi kepada pengguna biasa, sanksi
tersimpan di database, dan status pengguna ikut berubah.

## Skenario test

| Langkah | Keterangan |
|---|---|
| 1 | Membuat/menyiapkan admin `admin@dummy.test` |
| 2 | Membuat/menyiapkan target `target@dummy.test` |
| 3 | Menghapus sanksi lama milik target dan mengembalikan status ke `aktif` |
| 4 | Admin mengirim sanksi `ditangguhkan` selama 5 hari |
| 5 | API mengembalikan status `201` |
| 6 | Tabel `sanksi_akun` berisi sanksi untuk target |
| 7 | Status pengguna target berubah menjadi `ditangguhkan` |

## Menjalankan dengan `php artisan test`

```powershell
cd C:\laragon\www\VEN\ven\backend-api\backend
```

```powershell
php artisan test tests/Feature/PenggunaSanksiSuksesTest.php
```

Per method:

```powershell
php artisan test --filter=test_berhasil_memberikan_sanksi_kepada_pengguna
```

## Menjalankan dengan PHPUnit langsung

```powershell
php vendor\bin\phpunit --testdox tests\Feature\PenggunaSanksiSuksesTest.php
```

## Hasil yang diharapkan

```text
OK (1 test, 4 assertions)
```

## Pengujian manual

Test ini memanggil endpoint API dengan sesi admin, sehingga pengujian manual
paling tepat dilakukan lewat klien HTTP. Setelah test dijalankan, hasilnya
dapat diperiksa di `ven_testing`:

```sql
SELECT id_pengguna, jenis_sanksi, alasan, mulai, berakhir
FROM ven_testing.sanksi_akun
WHERE id_pengguna = (
  SELECT id_pengguna FROM ven_testing.pengguna WHERE email = 'target@dummy.test'
);

SELECT email, status
FROM ven_testing.pengguna
WHERE email = 'target@dummy.test';
```

Hasil yang diharapkan:

- Ada baris sanksi dengan `jenis_sanksi = 'ditangguhkan'`.
- `status` pengguna target = `ditangguhkan`.

Lewat tinker (dengan database aplikasi `ven`, bukan `ven_testing`), logika
serupa dapat diperiksa pada model:

```php
$p = App\Models\Pengguna::where('email', 'target@dummy.test')->first();

$p?->sanksiAktif();       // sanksi yang masih berlaku
$p?->cekAksesLogin();     // pesan penolakan jika masih disanksi
```

## Catatan

- Test memakai `updateOrCreate`, jadi aman dijalankan berulang.
- Data uji tetap tersimpan di `ven_testing` setelah test selesai.
- Untuk panduan umum, lihat [panduan-artisan-test.md](panduan-artisan-test.md).
