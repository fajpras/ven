# Dokumentasi Audit & Pengujian Keamanan Aplikasi VEN

Kumpulan dokumen audit kepatuhan database & keamanan sistem VEN, panduan pengujian otomatis (*automated testing*), dan integrasi API testing.

Semua perintah pengujian dijalankan dari direktori backend:
```powershell
cd C:\laragon\www\VEN\ven\backend-api\backend
```

---

## 🛡️ 7 Kategori Audit Keamanan & Kepatuhan Database

Berikut adalah 7 pilar keamanan dan basis data yang telah diimplementasikan, diaudit, dan lulus pengujian:

| No | Kategori Audit | Dokumen Penjelasan | File Pengujian Utama (PHPUnit) | Status |
|:--:|---|---|---|:---:|
| 1 | **Pembatasan Akses dengan View** | [01-pembatasan-akses-view.md](01-pembatasan-akses-view.md) | `tests/Feature/AuditKeamananDanViewTest.php` | ✅ COMPLIED |
| 2 | **Hashing dan Salting (Bcrypt)** | [02-hashing-dan-salting.md](02-hashing-dan-salting.md) | `tests/Feature/AuditKeamananDanViewTest.php` | ✅ COMPLIED |
| 3 | **Enkripsi AES-256-GCM & Blind Index** | [03-enkripsi-aes-256-gcm.md](03-enkripsi-aes-256-gcm.md) | `tests/Feature/EnkripsiGoogleIdTest.php`<br>`tests/Unit/LayananEnkripsiTest.php` | ✅ COMPLIED |
| 4 | **Database Transaction (COMMIT & ROLLBACK)** | [04-database-transactions.md](04-database-transactions.md) | `tests/Feature/Transaksi/TransaksiSederhanaTest.php`<br>`tests/Feature/Transaksi/TambahPameranCommitTest.php` | ✅ COMPLIED |
| 5 | **Concurrent Access / Locking (`lockForUpdate`)** | [05-concurrent-access-locking.md](05-concurrent-access-locking.md) | `tests/Feature/PenggunaSanksiLockTest.php` | ✅ COMPLIED |
| 6 | **Backup Database (Otomatis & Manual)** | [06-backup-database.md](06-backup-database.md) | `tests/Feature/AuditBackupDanRestoreTest.php` | ✅ COMPLIED |
| 7 | **Restore Verification (Disaster Recovery)** | [07-restore-verification.md](07-restore-verification.md) | `tests/Feature/AuditBackupDanRestoreTest.php` | ✅ COMPLIED |

---

## 🚀 Menjalankan Pengujian Otomatis

### 1. Menjalankan Semua Test Sekaligus (37 Tests Passed)
```powershell
php artisan test
# Atau jika PHP belum ada di path environment:
& "C:\laragon\bin\php\php-8.3.33-Win32-vs16-x64\php.exe" artisan test
```

### 2. Pengujian Spesifik Fitur Audit
```powershell
# Uji View & Hashing
php artisan test tests/Feature/AuditKeamananDanViewTest.php

# Uji Enkripsi AES-256-GCM & Blind Index
php artisan test tests/Feature/EnkripsiGoogleIdTest.php
php artisan test tests/Unit/LayananEnkripsiTest.php

# Uji Transaksi (Commit & Rollback)
php artisan test tests/Feature/Transaksi/TransaksiSederhanaTest.php
php artisan test tests/Feature/Transaksi/TambahPameranCommitTest.php
php artisan test tests/Feature/Transaksi/TambahPameranRollbackTest.php

# Uji Concurrent Access Locking (Pessimistic Locking)
php artisan test tests/Feature/PenggunaSanksiLockTest.php

# Uji Backup Otomatis & Restore Verification
php artisan test tests/Feature/AuditBackupDanRestoreTest.php
```

---

## 📬 Koleksi Postman API (Audit 7 Kategori)

Koleksi dan environment Postman terstruktur per kategori audit telah disediakan di direktori `c:\laragon\www\VEN\ven\postman`:

### 1. Import Langsung ke Postman (Format JSON Standar)
- **File Koleksi**: [`VEN_Audit_Keamanan_Database.postman_collection.json`](file:///c:/laragon/www/VEN/ven/postman/VEN_Audit_Keamanan_Database.postman_collection.json)
- **File Environment**: [`VEN_Local.postman_environment.json`](file:///c:/laragon/www/VEN/ven/postman/VEN_Local.postman_environment.json)

### 2. Format Git / Workspace Postman
Tersimpan di [collections/VEN Audit Keamanan & Database](file:///c:/laragon/www/VEN/ven/postman/collections/VEN%20Audit%20Keamanan%20&%20Database) yang terbagi menjadi 7 folder kategori:
1. `1. Pembatasan Akses dengan View`
2. `2. Hashing dan Salting`
3. `3. Enkripsi AES-256-GCM`
4. `4. Transaction (COMMIT & ROLLBACK)`
5. `5. Concurrent Access & Locking`
6. `6. Backup Database`
7. `7. Restore Verification`

---

## 📚 Panduan Tambahan

| Dokumen | Deskripsi |
|---|---|
| [panduan-artisan-test.md](panduan-artisan-test.md) | Panduan lengkap opsi command `php artisan test` |
| [panduan-unit-test.md](panduan-unit-test.md) | Pengujian Unit tanpa database |
| [panduan-tinker.md](panduan-tinker.md) | Panduan simulasi interaktif lewat REPL Tinker |
| [test-transaksi-pameran.md](test-transaksi-pameran.md) | Log pembuktian COMMIT dan ROLLBACK modul pameran |
