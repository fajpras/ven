# 🏛️ VEN Backend REST API

Backend API untuk **VEN (Virtual Exhibition Network)** — platform galeri dan pameran seni virtual 3D interaktif. Dibangun di atas **Laravel 11** dan **PHP 8.3**, dirancang dengan arsitektur keamanan berlapis, pemisahan hak akses basis data (*least privilege*), integrasi AI multimodal (Google AI Studio Gemini), serta dukungan WebRTC real-time.

---

## 🛡️ 7 Kategori Kepatuhan Audit Keamanan & Basis Data

Sistem backend VEN telah diaudit dan memenuhi 7 pilar kepatuhan basis data:

| No | Kategori Audit | Implementasi Teknis | Status | Dokumentasi |
|:--:|---|---|:---:|---|
| 1 | **Pembatasan Akses dengan View** | `v_pengguna_publik` memfilter kolom sensitif (`kata_sandi`, `google_id`, `role`) agar tidak bocor ke publik. | ✅ PASS | [01-pembatasan-akses-view.md](docs/01-pembatasan-akses-view.md) |
| 2 | **Hashing dan Salting** | Algoritma Bcrypt (`$2y$`) dengan salt dinamis unik untuk seluruh kata sandi. | ✅ PASS | [02-hashing-dan-salting.md](docs/02-hashing-dan-salting.md) |
| 3 | **Enkripsi AES-256-GCM** | Enkripsi simetris data sensitif Google ID + Blind Index (HMAC-SHA-256) untuk pencarian instan. | ✅ PASS | [03-enkripsi-aes-256-gcm.md](docs/03-enkripsi-aes-256-gcm.md) |
| 4 | **Database Transaction** | Eksekusi atomik `DB::transaction()` menjamin sifat ACID (COMMIT pada sukses, ROLLBACK total pada kegagalan). | ✅ PASS | [04-database-transactions.md](docs/04-database-transactions.md) |
| 5 | **Concurrent Access / Locking** | *Pessimistic Row Locking* (`lockForUpdate`) saat moderasi/sanksi untuk mencegah *race condition*. | ✅ PASS | [05-concurrent-access-locking.md](docs/05-concurrent-access-locking.md) |
| 6 | **Backup Database** | Pencadangan berkala otomatis (cron tengah malam) + manual on-demand via DBA (`mysqldump`). | ✅ PASS | [06-backup-database.md](docs/06-backup-database.md) |
| 7 | **Restore Verification** | Pengujian pemulihan berkala (*Disaster Recovery Preparedness*) ke database terisolasi. | ✅ PASS | [07-restore-verification.md](docs/07-restore-verification.md) |

---

## 📋 Prasyarat Sistem

* **PHP** >= 8.3 (dengan ekstensi `pdo_mysql`, `openssl`, `mbstring`, `fileinfo`, `curl`)
* **MySQL** >= 8.0 atau 8.4 (Direkomendasikan menggunakan [Laragon](https://laragon.org/) pada Windows)
* **Composer** >= 2.x
* **Node.js** >= 18.x & NPM (opsional untuk asset panel admin web)

---

## 🚀 Panduan Instalasi & Setup Cepat

Jalankan langkah-langkah berikut dari terminal di folder backend:
```powershell
cd C:\laragon\www\VEN\ven\backend-api\backend
```

### 1. Pasang Dependensi
```powershell
composer install
npm install
```

### 2. Konfigurasi Environment File
Salin file template `.env.example` menjadi `.env`:
```powershell
copy .env.example .env
php artisan key:generate
```

Buka file `.env` dan pastikan konfigurasi dasar telah terisi:
* `DB_DATABASE=ven`
* `VEN_CUSTOM_KEY=` *(kunci acak minimal 32 karakter untuk enkripsi AES-256-GCM)*
* `GEMINI_API_KEY=` *(API key gratis dari [Google AI Studio](https://aistudio.google.com/app/apikey))*

### 3. Inisialisasi Database & Multi-Role User (Least Privilege)
Perintah ini akan membuat database `ven`, akun user database terpisah (`ven`, `ven_user`, `ven_admin`, `ven_dba`), serta membatasi hak akses granular per tabel:
```powershell
php artisan ven:setup-db
```
*(Bila MySQL root Anda menggunakan password, masukkan saat diminta. Jika tanpa password di Laragon, tekan Enter).*

### 4. Jalankan Migrasi Database
Gunakan koneksi DBA (`mysql_dba`) yang memiliki izin skema penuh:
```powershell
php artisan migrate --database=mysql_dba
```

### 5. Jalankan Seeder Data Awal (Opsional)
Mengisi data awal kategori seni, pameran virtual, dan akun pengguna/admin dummy:
```powershell
php artisan db:seed
```

### 6. Hubungkan Symlink Storage
Agar banner pameran dan gambar dapat diakses oleh publik:
```powershell
php artisan storage:link
```

---

## 💻 Menjalankan Server Aplikasi

### Menjalankan API Server
```powershell
php artisan serve
```
Aplikasi berjalan secara lokal di: `http://127.0.0.1:8000`

### Menjalankan Task Scheduler (Cron Job Otomatis)
Di lingkungan produksi atau untuk simulasi backup database otomatis (pukul 00:00) dan retensi pembersihan file (pukul 03:00):
```powershell
php artisan schedule:work
```

---

## 🛠️ Perintah Konsol Artisan VEN Khusus

| Perintah | Deskripsi |
|---|---|
| `php artisan ven:setup-db` | Membuat user, database, role MySQL dan hak akses granular (*Least Privilege*). Tambahkan opsi `--fresh` untuk reset total. |
| `php artisan ven:initialize-db` | Inisialisasi menyeluruh dalam satu langkah: backup otomatis -> setup db -> migrate skema. |
| `php artisan ven:backup-db` | Mencadangkan database VEN menggunakan `mysqldump` dengan user DBA ke `storage/app/backups/`. |
| `php artisan ven:restore-db --path=...` | Memulihkan file SQL dump cadangan ke database tujuan (opsi `--db=ven_restore`). |
| `php artisan ven:enkripsi-google-id` | Utilitas batch untuk mengenkripsi kolom Google ID lama ke format AES-256-GCM & Blind Index. |
| `php artisan uji:transaksi-pameran` | Menjalankan simulasi transaksi pembuatan pameran langsung di database aktif. |

---

## 🧪 Menjalankan Pengujian Otomatis (Automated Testing)

Seluruh modul telah dilengkapi pengujian otomatis dengan **PHPUnit**:

```powershell
# Jalankan seluruh test suite (37 tests, 176 assertions)
php artisan test

# Menggunakan path PHP Laragon langsung:
& "C:\laragon\bin\php\php-8.3.33-Win32-vs16-x64\php.exe" artisan test
```

### Pengujian per Kategori Spesifik:
```powershell
# 1. Uji View & Hashing Bcrypt
php artisan test tests/Feature/AuditKeamananDanViewTest.php

# 2. Uji Enkripsi AES-256-GCM & Blind Index
php artisan test tests/Feature/EnkripsiGoogleIdTest.php
php artisan test tests/Unit/LayananEnkripsiTest.php

# 3. Uji Transaksi Database (COMMIT & ROLLBACK)
php artisan test tests/Feature/Transaksi/TransaksiSederhanaTest.php
php artisan test tests/Feature/Transaksi/TambahPameranCommitTest.php

# 4. Uji Concurrency & Pessimistic Row Locking
php artisan test tests/Feature/PenggunaSanksiLockTest.php

# 5. Uji Backup Otomatis & Restore Verification
php artisan test tests/Feature/AuditBackupDanRestoreTest.php

# 6. Uji Integrasi Google AI Studio (Gemini)
php artisan test tests/Feature/AIChatGeminiTest.php
```

---

## 📬 Pengujian API Menggunakan Postman

Koleksi dan environment Postman siap pakai tersedia di folder [postman/](file:///c:/laragon/www/VEN/ven/postman):

1. **Import File JSON Sekali Klik ke Postman:**
   * **Koleksi 7 Kategori Audit**: [`postman/VEN_Audit_Keamanan_Database.postman_collection.json`](file:///c:/laragon/www/VEN/ven/postman/VEN_Audit_Keamanan_Database.postman_collection.json)
   * **Environment Lokal**: [`postman/VEN_Local.postman_environment.json`](file:///c:/laragon/www/VEN/ven/postman/VEN_Local.postman_environment.json)
2. **Koleksi Rute Lengkap**: Tersedia di direktori `postman/collections/VEN API - Complete Routes Test`.

---

## 📁 Struktur Direktori Utama

```text
backend/
├── app/
│   ├── Console/Commands/      # Perintah CLI (Backup, Restore, Setup DB, Enkripsi)
│   ├── Http/
│   │   ├── Controllers/       # Pengendali API & Admin Web
│   │   └── Middleware/        # Filter Keamanan, Role Admin, Pemilihan Koneksi DB
│   ├── Models/                # Eloquent Model (Pengguna, Pameran, Karya, Log, dll.)
│   └── Services/              # Layanan Kriptografi (AES-256-GCM, Blind Index), OTP
├── config/                    # Konfigurasi aplikasi, database, backup, services (Gemini)
├── database/
│   ├── migrations/            # Migrasi skema DDL & View database
│   ├── seeders/               # Seeder data awal pameran, kategori & pengguna
│   └── scripts/               # Script pengujian interaktif Tinker
├── docs/                      # Dokumentasi kepatuhan audit keamanan & basis data
├── routes/
│   ├── api.php                # Rute API RESTful (Mobile & Frontend)
│   ├── console.php            # Jadwal tugas otomatis (Task Scheduling & Retensi Backup)
│   └── web.php                # Rute web panel admin
├── storage/app/backups/       # Direktori penyimpanan cadangan SQL dump
└── tests/                     # Automated Test Suites (Feature & Unit Tests)
```

---

## 📄 Lisensi
Hak Cipta © 2026 Tim Pengembang VEN. Seluruh hak cipta dilindungi undang-undang.
