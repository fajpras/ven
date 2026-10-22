# Dokumentasi Pengujian Aplikasi VEN

Kumpulan panduan menjalankan test aplikasi. Semua perintah dijalankan dari
folder backend:

```powershell
cd C:\laragon\www\VEN\ven\backend-api\backend
```

## Panduan umum

| Dokumen | Isi |
|---|---|
| [panduan-artisan-test.md](panduan-artisan-test.md) | Menjalankan test dengan `php artisan test`: semua, per suite, per file, per method |
| [panduan-unit-test.md](panduan-unit-test.md) | Test unit tanpa database: perintah, struktur, dan cara menambah test |
| [panduan-tinker.md](panduan-tinker.md) | Pengujian manual lewat `php artisan tinker`: enkripsi, dekripsi, data nyata, simulasi kegagalan |

## Modul transaksi

| Modul | Dokumen | Jumlah test | Database |
|---|---|---|---|
| Transaksi tambah pameran (Poin 13 & 14) | [test-transaksi-pameran.md](test-transaksi-pameran.md) | 2 | Ya (`ven_testing`) |
| Transaksi sederhana (COMMIT & ROLLBACK) | [test-transaksi-sederhana.md](test-transaksi-sederhana.md) | 2 | Ya (`ven_testing`) |

Modul ini membuktikan COMMIT pada jalur sukses dan ROLLBACK pada jalur gagal,
lengkap dengan kondisi data sebelum dan sesudah kedua skenario. Setiap test
juga mencetak bukti ke terminal dan menyimpannya di
`storage/app/testing/bukti-transaksi-sukses.md` serta
`storage/app/testing/bukti-transaksi-gagal.md`.

## Dokumen per test

### Suite Unit (tanpa database)

| Test | Dokumen | Jumlah | Database |
|---|---|---|---|
| `DekripsiGoogleIdTest` | [test-dekripsi-google-id.md](test-dekripsi-google-id.md) | 8 | Tidak |
| `LayananEnkripsiTest` | [test-layanan-enkripsi.md](test-layanan-enkripsi.md) | 6 | Tidak |
| `ExampleTest` | [test-example.md](test-example.md) | 1 | Tidak |

### Suite Feature (database `ven_testing`)

| Test | Dokumen | Jumlah | Database |
|---|---|---|---|
| `EnkripsiGoogleIdTest` | [test-enkripsi.md](test-enkripsi.md) | 3 | Ya |
| `AdminPanelAksesTest` | [test-admin-panel-akses.md](test-admin-panel-akses.md) | 3 | Ya |
| `LayananOtpRegistrasiTest` | [test-layanan-otp-registrasi.md](test-layanan-otp-registrasi.md) | 1 | Ya |
| `PenggunaSanksiSuksesTest` | [test-pengguna-sanksi-sukses.md](test-pengguna-sanksi-sukses.md) | 1 | Ya |
| `PenggunaSanksiGagalTest` | [test-pengguna-sanksi-gagal.md](test-pengguna-sanksi-gagal.md) | 1 | Ya |
| `TambahPameranCommitTest` | [test-transaksi-pameran.md](test-transaksi-pameran.md) | 1 | Ya |
| `TambahPameranRollbackTest` | [test-transaksi-pameran.md](test-transaksi-pameran.md) | 1 | Ya |
| `TransaksiSederhanaTest` | [test-transaksi-sederhana.md](test-transaksi-sederhana.md) | 2 | Ya |

Total: **28 test** (15 Unit + 13 Feature), **101 assertion**.

## Perintah cepat

```powershell
# Semua test
php artisan test

# Hanya unit (tanpa database)
php artisan test --testsuite=Unit

# Hanya feature (butuh ven_testing)
php artisan test --testsuite=Feature

# Satu file
php artisan test tests/Feature/EnkripsiGoogleIdTest.php

# Satu method
php artisan test --filter=test_blind_index_tersimpan_dan_pencarian_menemukan_pengguna

# Modul transaksi tambah pameran
php artisan test tests/Feature/Transaksi

# Test transaksi paling sederhana
php artisan test tests/Feature/Transaksi/TransaksiSederhanaTest.php

# Uji transaksi langsung di database asli (.env, biasanya ven)
php artisan tinker database/scripts/uji-transaksi-db-asli.php

# Uji transaksi TAMBAH PAMERAN langsung di database asli
php artisan tinker database/scripts/uji-transaksi-pameran-db-asli.php

# Uji transaksi + data dummy lewat Artisan command
php artisan uji:transaksi-pameran
```

## Prasyarat

1. Dependency terpasang: `composer install`
2. PHP 8.3. Jika `php` belum ada di PATH, gunakan:

   ```powershell
   & "C:\laragon\bin\php\php-8.3.33-Win32-vs16-x64\php.exe" artisan test
   ```

3. Untuk test Feature: MySQL berjalan dan database `ven_testing` tersedia.
4. Konfigurasi test ada di `phpunit.xml` dan `.env.testing`.
5. Jangan mengarahkan test ke database aplikasi (`ven`).

## Perbedaan singkat

| Aspek | Unit | Feature |
|---|---|---|
| Folder | `tests/Unit` | `tests/Feature` |
| Database | Tidak | `ven_testing` |
| Fokus | Logika, service, model di memori | HTTP, database, alur aplikasi |
| Contoh | `DekripsiGoogleIdTest` | `EnkripsiGoogleIdTest` |
