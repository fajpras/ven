# Test Example

**File test:** `tests/Unit/ExampleTest.php`  
**Class:** `ExampleTest`  
**Jenis:** Unit  
**Jumlah test:** 1  
**Butuh database:** Tidak

## Tujuan

Test contoh bawaan Laravel untuk memastikan konfigurasi PHPUnit berjalan.
Isinya hanya `assertTrue(true)` dan tidak menguji fitur aplikasi.

## Menjalankan dengan `php artisan test`

```powershell
cd C:\laragon\www\VEN\ven\backend-api\backend
php artisan test tests/Unit/ExampleTest.php
```

Per method:

```powershell
php artisan test --filter=test_that_true_is_true
```

## Menjalankan dengan PHPUnit langsung

```powershell
php vendor\bin\phpunit --testdox tests\Unit\ExampleTest.php
```

## Hasil yang diharapkan

```text
OK (1 test, 1 assertion)
```

## Pengujian manual dengan tinker

Tidak diperlukan. Test ini hanya memeriksa bahwa PHPUnit dapat dijalankan.

## Catatan

- Boleh dihapus jika tidak diperlukan.
- Untuk panduan unit, lihat [panduan-unit-test.md](panduan-unit-test.md).
