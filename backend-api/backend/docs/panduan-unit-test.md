# Panduan Test Unit (Tanpa Basis Data)

Dokumen ini khusus untuk test di folder `tests/Unit`. Test unit di proyek ini
**tidak membuka koneksi database**, tidak membuat tabel, dan tidak menulis data.

Dokumen terkait:

- [panduan-artisan-test.md](panduan-artisan-test.md) — menjalankan test lewat `php artisan test`
- [panduan-tinker.md](panduan-tinker.md) — pengujian manual lewat tinker
- [README.md](README.md) — daftar seluruh dokumen test

---

## 1. Prasyarat

1. Berada di folder backend:

   ```powershell
   cd C:\laragon\www\VEN\ven\backend-api\backend
   ```

2. Dependency terpasang (`composer install`).
3. MySQL **tidak wajib** berjalan untuk test unit.

Jika `php` belum ada di PATH:

```powershell
& "C:\laragon\bin\php\php-8.3.33-Win32-vs16-x64\php.exe" vendor\bin\phpunit tests\Unit
```

---

## 2. Menjalankan test unit

### Semua test unit

```powershell
php artisan test --testsuite=Unit
```

Alternatif langsung ke PHPUnit:

```powershell
vendor\bin\phpunit tests\Unit
```

### Per file

| File | Perintah | Jumlah test |
|---|---|---|
| `tests/Unit/DekripsiGoogleIdTest.php` | `php artisan test tests/Unit/DekripsiGoogleIdTest.php` | 8 |
| `tests/Unit/LayananEnkripsiTest.php` | `php artisan test tests/Unit/LayananEnkripsiTest.php` | 6 |
| `tests/Unit/ExampleTest.php` | `php artisan test tests/Unit/ExampleTest.php` | 1 |

### Dengan output detail

```powershell
vendor\bin\phpunit --testdox tests\Unit\DekripsiGoogleIdTest.php
vendor\bin\phpunit --testdox tests\Unit\LayananEnkripsiTest.php
```

### Per method

```powershell
php artisan test --filter=test_dekripsi_membuka_ciphertext_yang_diketahui
php artisan test --filter=test_enkripsi_lalu_dekripsi_menghasilkan_nilai_sama
```

---

## 3. Isi test unit

### `DekripsiGoogleIdTest` — 8 test

Menguji fungsi dekripsi `google_id` tanpa menyentuh database.

| Method | Yang diuji |
|---|---|
| `test_dekripsi_membuka_ciphertext_yang_diketahui` | Ciphertext tetap berhasil didekripsi menjadi nilai asli |
| `test_hasil_dekripsi_konsisten_saat_diulang` | Dekripsi berulang menghasilkan nilai yang sama |
| `test_dekripsi_gagal_bila_kunci_berbeda` | Kunci lain tidak dapat membuka data |
| `test_dekripsi_menolak_isi_data_yang_diubah` | Perubahan 1 byte ciphertext ditolak (`null`) |
| `test_dekripsi_menolak_kode_verifikasi_yang_diubah` | Perubahan kode verifikasi ditolak (`null`) |
| `test_dekripsi_nilai_tidak_valid_mengembalikan_null` | `null`, kosong, plaintext, prefix salah, base64 rusak → `null` |
| `test_enkripsi_lalu_dekripsi_untuk_berbagai_nilai` | Round-trip untuk beberapa bentuk ID |
| `test_model_mendekripsi_tanpa_basis_data` | Model `Pengguna` mengenkripsi saat di-set dan mendekripsi saat dibaca, tanpa DB |

Contoh menjalankan satu method:

```powershell
php artisan test --filter=test_dekripsi_menolak_isi_data_yang_diubah
```

### `LayananEnkripsiTest` — 6 test

Menguji layanan enkripsi secara langsung.

| Method | Yang diuji |
|---|---|
| `test_enkripsi_lalu_dekripsi_menghasilkan_nilai_sama` | Enkripsi lalu dekripsi mengembalikan nilai awal |
| `test_enkripsi_plaintext_sama_menghasilkan_ciphertext_berbeda` | IV acak membuat ciphertext selalu berbeda |
| `test_dekripsi_null_dan_plaintext_tanpa_prefix_mengembalikan_null` | Nilai kosong/plaintext tidak dianggap ciphertext |
| `test_dekripsi_data_rusak_menghasilkan_null_tanpa_crash` | Data rusak tidak membuat aplikasi berhenti |
| `test_blind_index_deterministik_dan_berbeda_untuk_nilai_berbeda` | Blind index konsisten dan membedakan nilai |
| `test_sudah_terenkripsi_mengenali_format` | Pengenalan format `v1:` |

### `ExampleTest` — 1 test

Test contoh bawaan Laravel (`assertTrue(true)`), tidak terkait fitur aplikasi.

---

## 4. Struktur test unit

Semua test berada di namespace `Tests\Unit` dan mewarisi `Tests\TestCase`.

Contoh kerangka:

```php
<?php

namespace Tests\Unit;

use App\Services\Crypto\LayananEnkripsi;
use Tests\TestCase;

class NamaFiturTest extends TestCase
{
    public function test_sesuatu_berjalan_benar(): void
    {
        $this->assertTrue(true);
    }
}
```

Konvensi nama:

- File: `NamaFiturTest.php`
- Class: `NamaFiturTest`
- Method: `test_kondisi_yang_diuji()`
- Satu method menguji satu perilaku.

---

## 5. Menambah test unit baru

1. Buat file di `tests/Unit/`, misalnya `ValidasiKataSandiTest.php`.
2. Gunakan namespace `Tests\Unit`.
3. Warisi `Tests\TestCase`.
4. Tulis method dengan awalan `test_`.
5. Jalankan:

   ```powershell
   php artisan test tests/Unit/ValidasiKataSandiTest.php
   ```

---

## 6. Catatan penting

- **Tidak butuh database.** Jika test unit tiba-tiba membutuhkan MySQL,
  kemungkinan ada pemanggilan model yang menyentuh database.
- **Kunci uji.** Test memakai `VEN_CUSTOM_KEY` dari `phpunit.xml`:
  `testing-ven-custom-key-0123456789abcdef`.
- **Ciphertext tetap.** `DekripsiGoogleIdTest` menyimpan satu ciphertext
  (known-answer). Jika kunci di `phpunit.xml` diubah, test ini akan gagal —
  ini disengaja agar perubahan kunci terdeteksi.
- **Tanpa migrasi.** Jangan menambahkan kolom/tabel hanya untuk test unit.
- **Cepat.** Seluruh suite unit berjalan di bawah satu detik pada mesin lokal.

---

## 7. Perbedaan Unit dan Feature

| Aspek | Unit | Feature |
|---|---|---|
| Folder | `tests/Unit` | `tests/Feature` |
| Database | Tidak | Ya (`ven_testing`) |
| Fokus | Logika/service/model di memori | Alur aplikasi, HTTP, database |
| Contoh | `DekripsiGoogleIdTest` | `EnkripsiGoogleIdTest` |
| Kecepatan | Sangat cepat | Lebih lambat |
