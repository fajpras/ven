# Panduan Menjalankan Test dengan `php artisan test`

Dokumen ini menjelaskan cara menjalankan test aplikasi VEN memakai perintah
`php artisan test`, baik seluruh test, per suite, per file, maupun per method.

Dokumen terkait:

- [panduan-unit-test.md](panduan-unit-test.md) — test unit tanpa basis data
- [panduan-tinker.md](panduan-tinker.md) — pengujian manual lewat tinker
- [README.md](README.md) — daftar seluruh dokumen test

---

## 1. Prasyarat

1. Berada di folder backend:

   ```powershell
   cd C:\laragon\www\VEN\ven\backend-api\backend
   ```

2. Dependency sudah terpasang:

   ```powershell
   composer install
   ```

3. PHP 8.3 tersedia. Jika `php` belum ada di PATH, gunakan PHP Laragon:

   ```powershell
   & "C:\laragon\bin\php\php-8.3.33-Win32-vs16-x64\php.exe" artisan test
   ```

4. Untuk test **Feature**, MySQL harus berjalan dan database `ven_testing`
   harus sudah memiliki skema. Konfigurasi ada di `phpunit.xml`:

   ```xml
   <env name="DB_DATABASE" value="ven_testing"/>
   <env name="VEN_CUSTOM_KEY" value="testing-ven-custom-key-0123456789abcdef"/>
   ```

5. Jangan mengarahkan test ke database aplikasi (`ven`). Beberapa test Feature
   melakukan pengecekan dan akan gagal jika bukan `ven_testing`.

---

## 2. Perintah dasar

Menjalankan seluruh test:

```powershell
php artisan test
```

Output ringkas (jumlah test dan assertion):

```powershell
php artisan test --compact
```

Menampilkan daftar test yang lambat:

```powershell
php artisan test --profile
```

---

## 3. Menjalankan per suite

| Suite | Perintah | Kebutuhan |
|---|---|---|
| Semua | `php artisan test` | MySQL + `ven_testing` untuk Feature |
| Unit | `php artisan test --testsuite=Unit` | Tidak butuh basis data |
| Feature | `php artisan test --testsuite=Feature` | MySQL + `ven_testing` |

Contoh:

```powershell
php artisan test --testsuite=Unit
php artisan test --testsuite=Feature
```

---

## 4. Menjalankan per file test

### Suite Unit (15 test)

| File | Class | Jumlah | Butuh DB |
|---|---|---|---|
| `tests/Unit/DekripsiGoogleIdTest.php` | `DekripsiGoogleIdTest` | 8 | Tidak |
| `tests/Unit/LayananEnkripsiTest.php` | `LayananEnkripsiTest` | 6 | Tidak |
| `tests/Unit/ExampleTest.php` | `ExampleTest` | 1 | Tidak |

Perintah:

```powershell
php artisan test tests/Unit/DekripsiGoogleIdTest.php
php artisan test tests/Unit/LayananEnkripsiTest.php
php artisan test tests/Unit/ExampleTest.php
```

### Suite Feature (9 test)

| File | Class | Jumlah | Butuh DB |
|---|---|---|---|
| `tests/Feature/AdminPanelAksesTest.php` | `AdminPanelAksesTest` | 3 | Ya |
| `tests/Feature/EnkripsiGoogleIdTest.php` | `EnkripsiGoogleIdTest` | 3 | Ya |
| `tests/Feature/LayananOtpRegistrasiTest.php` | `LayananOtpRegistrasiTest` | 1 | Ya |
| `tests/Feature/PenggunaSanksiGagalTest.php` | `PenggunaSanksiGagalTest` | 1 | Ya |
| `tests/Feature/PenggunaSanksiSuksesTest.php` | `PenggunaSanksiSuksesTest` | 1 | Ya |

Perintah:

```powershell
php artisan test tests/Feature/EnkripsiGoogleIdTest.php
php artisan test tests/Feature/AdminPanelAksesTest.php
php artisan test tests/Feature/LayananOtpRegistrasiTest.php
php artisan test tests/Feature/PenggunaSanksiGagalTest.php
php artisan test tests/Feature/PenggunaSanksiSuksesTest.php
```

---

## 5. Menjalankan per method

Gunakan `--filter` dengan nama method atau nama class.

```powershell
# Satu class
php artisan test --filter=DekripsiGoogleIdTest

# Satu method
php artisan test --filter=test_dekripsi_membuka_ciphertext_yang_diketahui

# Semua method yang mengandung kata "dekripsi"
php artisan test --filter=dekripsi
```

Daftar method per class:

**`DekripsiGoogleIdTest`** (tanpa DB)

```powershell
php artisan test --filter=test_dekripsi_membuka_ciphertext_yang_diketahui
php artisan test --filter=test_hasil_dekripsi_konsisten_saat_diulang
php artisan test --filter=test_dekripsi_gagal_bila_kunci_berbeda
php artisan test --filter=test_dekripsi_menolak_isi_data_yang_diubah
php artisan test --filter=test_dekripsi_menolak_kode_verifikasi_yang_diubah
php artisan test --filter=test_dekripsi_nilai_tidak_valid_mengembalikan_null
php artisan test --filter=test_enkripsi_lalu_dekripsi_untuk_berbagai_nilai
php artisan test --filter=test_model_mendekripsi_tanpa_basis_data
```

**`LayananEnkripsiTest`** (tanpa DB)

```powershell
php artisan test --filter=test_enkripsi_lalu_dekripsi_menghasilkan_nilai_sama
php artisan test --filter=test_enkripsi_plaintext_sama_menghasilkan_ciphertext_berbeda
php artisan test --filter=test_dekripsi_null_dan_plaintext_tanpa_prefix_mengembalikan_null
php artisan test --filter=test_dekripsi_data_rusak_menghasilkan_null_tanpa_crash
php artisan test --filter=test_blind_index_deterministik_dan_berbeda_untuk_nilai_berbeda
php artisan test --filter=test_sudah_terenkripsi_mengenali_format
```

**`EnkripsiGoogleIdTest`** (butuh DB)

```powershell
php artisan test --filter=test_google_id_tersimpan_terenkripsi_dan_accessor_mengembalikan_nilai_asli
php artisan test --filter=test_blind_index_tersimpan_dan_pencarian_menemukan_pengguna
php artisan test --filter=test_data_rusak_di_database_tidak_membuat_crash
```

**`AdminPanelAksesTest`**

```powershell
php artisan test --filter=test_halaman_utama_mengarahkan_ke_login_admin
php artisan test --filter=test_halaman_admin_tidak_dapat_diakses_tanpa_login
php artisan test --filter=test_pengguna_biasa_tidak_dapat_membuka_halaman_admin
```

**`PenggunaSanksiSuksesTest`**

```powershell
php artisan test --filter=test_berhasil_memberikan_sanksi_kepada_pengguna
```

**`PenggunaSanksiGagalTest`**

```powershell
php artisan test --filter=test_gagal_memberikan_sanksi_pada_akun_admin
```

**`LayananOtpRegistrasiTest`**

```powershell
php artisan test --filter=test_registration_otp_can_be_issued_and_verified
```

---

## 6. Opsi yang berguna

| Opsi | Fungsi |
|---|---|
| `--testsuite=Unit` | Hanya menjalankan suite Unit |
| `--testsuite=Feature` | Hanya menjalankan suite Feature |
| `--filter=Nama` | Menjalankan test yang namanya cocok |
| `--compact` | Output lebih ringkas |
| `--profile` | Menampilkan test paling lambat |
| `-v` | Output lebih detail |
| `--coverage` | Menampilkan coverage (butuh driver coverage) |

Opsi lain seperti `--stop-on-failure` dan `--testdox` diteruskan ke PHPUnit.

---

## 7. Membaca hasil

| Simbol | Arti |
|---|---|
| `PASS` / `OK` | Semua test berhasil |
| `FAIL` | Ada assertion yang tidak terpenuhi |
| `ERROR` | Test gagal karena exception/konfigurasi |
| Exit code `0` | Berhasil |
| Exit code `1` | Ada test yang gagal |

Contoh hasil lengkap:

```text
OK (24 tests, 69 assertions)
```

---

## 8. Troubleshooting

| Gejala | Penyebab | Solusi |
|---|---|---|
| `php: command not found` | PHP tidak ada di PATH | Pakai path Laragon atau buka Terminal Laragon |
| `Access denied for user 'root'` | Kredensial DB di `phpunit.xml`/`.env.testing` tidak cocok | Sesuaikan `DB_APP_USERNAME` dan `DB_APP_PASSWORD` |
| `Unknown database 'ven_testing'` | Database test belum ada | Buat database dan jalankan migrasi ke `ven_testing` |
| `VEN_CUSTOM_KEY belum diisi` | Env test tidak terbaca | Pastikan menjalankan dari folder backend dan `phpunit.xml` tidak diubah |
| Test menulis data ke DB | Test Feature tidak memakai `RefreshDatabase` | Wajar; data uji tersimpan di `ven_testing`, bukan `ven` |
| `Table 'reset_kata_sandi' already exists` | Sisa test yang berhenti di tengah | `DROP TABLE ven_testing.reset_kata_sandi;` lalu jalankan ulang |
| Perubahan `.env` tidak berlaku | Config ter-cache | `php artisan config:clear` |

---

## 9. Catatan penting

- Test **Feature** berjalan pada database `ven_testing` dan **tidak** memakai
  `RefreshDatabase`. Data uji dapat tertinggal setelah test selesai.
- Test **Unit** tidak membuka koneksi database.
- `AdminPanelAksesTest` dan test sanksi membuat data pengguna uji.
- `LayananOtpRegistrasiTest` membuat dan menghapus tabel `reset_kata_sandi`
  secara otomatis.
- Jangan menjalankan test dengan `DB_DATABASE=ven` (database aplikasi).
