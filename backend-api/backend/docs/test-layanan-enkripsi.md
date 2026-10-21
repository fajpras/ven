# Test Layanan Enkripsi

**File test:** `tests/Unit/LayananEnkripsiTest.php`  
**Class:** `LayananEnkripsiTest`  
**Jenis:** Unit  
**Jumlah test:** 6  
**Butuh database:** Tidak

## Tujuan

Menguji perilaku dasar layanan enkripsi: enkripsi dua arah, ciphertext acak,
penanganan data rusak, blind index, dan pengenalan format.

## Cakupan test

| Method | Yang diuji |
|---|---|
| `test_enkripsi_lalu_dekripsi_menghasilkan_nilai_sama` | Round-trip enkripsi → dekripsi |
| `test_enkripsi_plaintext_sama_menghasilkan_ciphertext_berbeda` | IV acak membuat ciphertext selalu berbeda |
| `test_dekripsi_null_dan_plaintext_tanpa_prefix_mengembalikan_null` | Nilai kosong/plaintext tidak dianggap ciphertext |
| `test_dekripsi_data_rusak_menghasilkan_null_tanpa_crash` | Data rusak tidak membuat aplikasi berhenti |
| `test_blind_index_deterministik_dan_berbeda_untuk_nilai_berbeda` | Blind index konsisten dan membedakan nilai |
| `test_sudah_terenkripsi_mengenali_format` | Pengenalan format `v1:` |

## Menjalankan dengan `php artisan test`

```powershell
cd C:\laragon\www\VEN\ven\backend-api\backend
```

```powershell
php artisan test tests/Unit/LayananEnkripsiTest.php
```

Per method:

```powershell
php artisan test --filter=test_enkripsi_lalu_dekripsi_menghasilkan_nilai_sama
php artisan test --filter=test_enkripsi_plaintext_sama_menghasilkan_ciphertext_berbeda
php artisan test --filter=test_blind_index_deterministik_dan_berbeda_untuk_nilai_berbeda
```

## Menjalankan dengan PHPUnit langsung

```powershell
php vendor\bin\phpunit --testdox tests\Unit\LayananEnkripsiTest.php
```

## Hasil yang diharapkan

```text
OK (6 tests, 16 assertions)
```

## Pengujian manual dengan tinker

```php
$e = app(App\Services\Crypto\LayananEnkripsi::class);

$e->enkripsi('google-sub-abc');          // v1:...
$e->enkripsi('google-sub-abc');          // v1:... (berbeda)

$e->blindIndex('google-sub-abc');        // 64 hex
$e->blindIndex('google-sub-abc');        // sama dengan sebelumnya
$e->blindIndex('google-sub-xyz');        // berbeda

$e->sudahTerenkripsi('v1:abc');          // true
$e->sudahTerenkripsi('plaintext');       // false
```

## Catatan

- Test ini tidak memakai database.
- Blind index bersifat satu arah dan deterministik.
- Untuk panduan unit, lihat [panduan-unit-test.md](panduan-unit-test.md).
