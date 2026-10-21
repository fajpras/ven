# Test Dekripsi `google_id`

**File test:** `tests/Unit/DekripsiGoogleIdTest.php`  
**Class:** `DekripsiGoogleIdTest`  
**Jenis:** Unit  
**Jumlah test:** 8  
**Butuh database:** Tidak  
**Kunci yang dipakai:** `testing-ven-custom-key-0123456789abcdef`

## Tujuan

Menguji fungsi dekripsi `google_id` secara terpisah, tanpa database, tanpa
tabel atau kolom baru. Fokusnya: hasil dekripsi benar, konsisten, dan aman
saat kunci salah atau data rusak.

## Cakupan test

| Method | Yang diuji |
|---|---|
| `test_dekripsi_membuka_ciphertext_yang_diketahui` | Ciphertext tetap berhasil didekripsi menjadi nilai asli |
| `test_hasil_dekripsi_konsisten_saat_diulang` | Hasil dekripsi berulang selalu sama |
| `test_dekripsi_gagal_bila_kunci_berbeda` | Kunci lain tidak dapat membuka data |
| `test_dekripsi_menolak_isi_data_yang_diubah` | Perubahan ciphertext ditolak (`null`) |
| `test_dekripsi_menolak_kode_verifikasi_yang_diubah` | Perubahan kode verifikasi ditolak (`null`) |
| `test_dekripsi_nilai_tidak_valid_mengembalikan_null` | Input kosong/plaintext/format salah → `null` |
| `test_enkripsi_lalu_dekripsi_untuk_berbagai_nilai` | Round-trip beberapa bentuk ID |
| `test_model_mendekripsi_tanpa_basis_data` | Model mengenkripsi saat set dan mendekripsi saat baca |

## Menjalankan dengan `php artisan test`

```powershell
cd C:\laragon\www\VEN\ven\backend-api\backend
```

```powershell
php artisan test tests/Unit/DekripsiGoogleIdTest.php
```

Per method:

```powershell
php artisan test --filter=test_dekripsi_membuka_ciphertext_yang_diketahui
php artisan test --filter=test_dekripsi_gagal_bila_kunci_berbeda
php artisan test --filter=test_model_mendekripsi_tanpa_basis_data
```

## Menjalankan dengan PHPUnit langsung

```powershell
php vendor\bin\phpunit --testdox tests\Unit\DekripsiGoogleIdTest.php
```

## Hasil yang diharapkan

```text
OK (8 tests, 25 assertions)
```

## Pengujian manual dengan tinker

Tinker memakai kunci asli dari `.env`, sehingga ciphertext uji di file test
tidak akan terbuka. Untuk mencoba alurnya:

```php
$e = app(App\Services\Crypto\LayananEnkripsi::class);

$ct = $e->enkripsi('100000000000000000777');
$e->dekripsi($ct);          // 100000000000000000777

// kunci salah
(new App\Services\Crypto\LayananEnkripsi('kunci-salah'))->dekripsi($ct); // null
```

Uji model tanpa database:

```php
$p = new App\Models\Pengguna();
$p->google_id = '100000000000000000777';

$p->getAttributes()['google_id'];   // ciphertext
$p->google_id;                      // hasil dekripsi
```

## Catatan

- Tidak membuka koneksi database sama sekali.
- Ciphertext tetap pada test dibuat dengan kunci uji di `phpunit.xml`.
  Jika kunci uji diubah, test ini akan gagal (disengaja).
- Untuk panduan unit, lihat [panduan-unit-test.md](panduan-unit-test.md).
