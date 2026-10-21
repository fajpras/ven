# Test Admin Panel Akses

**File test:** `tests/Feature/AdminPanelAksesTest.php`  
**Class:** `AdminPanelAksesTest`  
**Jenis:** Feature  
**Jumlah test:** 3  
**Butuh database:** Ya (`ven_testing`, untuk test ketiga)

## Tujuan

Memastikan halaman admin hanya dapat diakses oleh pengguna dengan peran admin,
dan pengguna biasa tetap diarahkan ke halaman login.

## Cakupan test

| Method | Skenario | Hasil yang diharapkan |
|---|---|---|
| `test_halaman_utama_mengarahkan_ke_login_admin` | Membuka `/` | Redirect ke halaman login admin |
| `test_halaman_admin_tidak_dapat_diakses_tanpa_login` | Membuka halaman admin tanpa sesi | Redirect ke halaman login admin |
| `test_pengguna_biasa_tidak_dapat_membuka_halaman_admin` | Login sebagai pengguna biasa, membuka halaman admin | Redirect ke halaman login admin |

## Menjalankan dengan `php artisan test`

```powershell
cd C:\laragon\www\VEN\ven\backend-api\backend
```

```powershell
php artisan test tests/Feature/AdminPanelAksesTest.php
```

Per method:

```powershell
php artisan test --filter=test_halaman_utama_mengarahkan_ke_login_admin
php artisan test --filter=test_halaman_admin_tidak_dapat_diakses_tanpa_login
php artisan test --filter=test_pengguna_biasa_tidak_dapat_membuka_halaman_admin
```

## Menjalankan dengan PHPUnit langsung

```powershell
php vendor\bin\phpunit --testdox tests\Feature\AdminPanelAksesTest.php
```

## Hasil yang diharapkan

```text
OK (3 tests, 6 assertions)
```

## Pengujian manual dengan tinker

Test ini berfokus pada HTTP dan sesi web, sehingga padanan tinker-nya terbatas.
Yang dapat diperiksa lewat tinker adalah data pengguna dan perannya:

```php
$p = App\Models\Pengguna::where('email', 'admin@dummy.test')->first();

$p?->role;        // admin
$p?->isAdmin();   // true
```

Melihat daftar route admin:

```powershell
php artisan route:list --name=admin
```

Pengujian manual yang lebih tepat dilakukan lewat browser:

1. Buka `/` → harus diarahkan ke login admin.
2. Buka halaman admin tanpa login → harus diarahkan ke login.
3. Login sebagai pengguna biasa, lalu buka halaman admin → harus ditolak.

## Catatan

- Test ketiga membuat pengguna uji melalui factory di `ven_testing`.
- Halaman admin memakai sesi web, bukan token API.
- Untuk panduan umum, lihat [panduan-artisan-test.md](panduan-artisan-test.md).
