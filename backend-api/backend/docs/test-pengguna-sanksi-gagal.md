# Test Pengguna Sanksi — Skenario Gagal

**File test:** `tests/Feature/PenggunaSanksiGagalTest.php`  
**Class:** `PenggunaSanksiGagalTest`  
**Jenis:** Feature  
**Jumlah test:** 1  
**Butuh database:** Ya (`ven_testing`)

## Tujuan

Memastikan admin tidak dapat memberikan sanksi kepada sesama admin, dan tidak
ada data sanksi yang tersimpan akibat percobaan tersebut.

## Skenario test

| Langkah | Keterangan |
|---|---|
| 1 | Menyiapkan admin pertama `admin@dummy.test` |
| 2 | Menyiapkan admin kedua `admin2@dummy.test` |
| 3 | Admin pertama mencoba memberi sanksi permanen kepada admin kedua |
| 4 | API menolak dengan status `403` |
| 5 | Tabel `sanksi_akun` tidak memiliki baris untuk admin kedua |
| 6 | Status admin kedua tetap `aktif` |

## Menjalankan dengan `php artisan test`

```powershell
cd C:\laragon\www\VEN\ven\backend-api\backend
```

```powershell
php artisan test tests/Feature/PenggunaSanksiGagalTest.php
```

Per method:

```powershell
php artisan test --filter=test_gagal_memberikan_sanksi_pada_akun_admin
```

## Menjalankan dengan PHPUnit langsung

```powershell
php vendor\bin\phpunit --testdox tests\Feature\PenggunaSanksiGagalTest.php
```

## Hasil yang diharapkan

```text
OK (1 test, 4 assertions)
```

## Pengujian manual

Setelah test dijalankan, periksa `ven_testing`:

```sql
SELECT COUNT(*) AS jumlah_sanksi
FROM ven_testing.sanksi_akun
WHERE id_pengguna = (
  SELECT id_pengguna FROM ven_testing.pengguna WHERE email = 'admin2@dummy.test'
);

SELECT email, role, status
FROM ven_testing.pengguna
WHERE email = 'admin2@dummy.test';
```

Hasil yang diharapkan:

- `jumlah_sanksi = 0`
- `role = admin`
- `status = aktif`

Lewat tinker pada database aplikasi:

```php
$p = App\Models\Pengguna::where('email', 'admin2@dummy.test')->first();

$p?->sanksiAktif();   // null
$p?->cekAksesLogin(); // null (tidak ada penolakan)
```

## Catatan

- Test ini memastikan aturan “admin tidak dapat menyanksi admin” berjalan.
- Data uji tetap tersimpan di `ven_testing`.
- Untuk panduan umum, lihat [panduan-artisan-test.md](panduan-artisan-test.md).
