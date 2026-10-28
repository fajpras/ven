# 🏛️ VEN (Virtual Exhibition Network)

Ekosistem platform galeri dan pameran virtual 3D interaktif yang mempertemukan kreator seni, kurator, dan pengunjung dalam ruang pameran digital imersif. Proyek ini memadukan aplikasi mobile Android modern berbasis **Jetpack Compose** dengan backend RESTful API berkinerja tinggi berbasis **Laravel 11**, dilengkapi audit keamanan database berlapis dan asisten cerdas **Google AI Studio (Gemini)**.

---

## 🏗️ Komponen Repositori

Repositori ini terdiri dari tiga modul utama:

| Modul | Direktori | Teknologi Utama | Deskripsi |
|---|---|---|---|
| 📱 **Mobile App** | [`ven-mobile/`](ven-mobile/) | Android, Kotlin, Jetpack Compose | Aplikasi klien mobile Android untuk menjelajahi galeri 3D, melihat pameran, berinteraksi di ruang diskusi, dan mengelola karya seni. |
| 🌐 **Backend API** | [`backend-api/backend/`](backend-api/backend/) | Laravel 11, PHP 8.3, MySQL 8.4 | RESTful API server, sistem autentikasi Sanctum, Web Admin Dashboard, orkestrasi database multi-role, dan integrasi AI Gemini. |
| 📬 **API Suite & Testing** | [`postman/`](postman/) | Postman v2.1, Postman CLI | Koleksi pengujian rute API komprehensif dan suite verifikasi 7 pilar kepatuhan audit keamanan database. |

---

## 🛡️ 7 Kategori Audit Keamanan & Kepatuhan Basis Data

Backend VEN telah diaudit dan memenuhi 7 standar ketat arsitektur basis data:

1. **Pembatasan Akses dengan View**: Memfilter data sensitif (`kata_sandi`, `google_id`, `google_id_bidx`, `role`) menggunakan view `v_pengguna_publik`.
2. **Hashing dan Salting**: Proteksi kata sandi dengan algoritma Bcrypt (`$2y$`) dan garam (*salt*) dinamis unik.
3. **Enkripsi AES-256-GCM**: Mengamankan data pihak ketiga (Google ID) secara simetris, disertai Blind Index (HMAC-SHA-256) untuk pencarian instan.
4. **Database Transaction (ACID)**: Eksekusi transaksi atomik (`DB::transaction`) menjamin sifat COMMIT pada alur sukses dan ROLLBACK total saat terjadi error.
5. **Concurrent Access & Locking**: Proteksi *race condition* saat moderasi/sanksi akun menggunakan *Pessimistic Row Locking* (`lockForUpdate`).
6. **Backup Database (Otomatis & Manual)**: Pencadangan berkala otomatis setiap tengah malam (00:00) dengan kebijakan retensi file 30 hari (03:00) serta perintah on-demand DBA (`ven:backup-db`).
7. **Restore Verification (Disaster Recovery)**: Mekanisme pengujian pemulihan cadangan database ke sandbox terisolasi (`ven:restore-db`).

> 📖 Dokumentasi teknis lengkap masing-masing kategori dapat dilihat di folder [`backend-api/backend/docs/`](backend-api/backend/docs/).

---

## ⚡ Panduan Memulai Cepat (Quick Start)

### 1. Menjalankan Backend API
Prasyarat: **PHP 8.3+**, **Composer**, dan **MySQL 8.0/8.4** (Laragon direkomendasikan pada Windows).

```powershell
# 1. Masuk ke direktori backend
cd backend-api/backend

# 2. Pasang dependensi
composer install
npm install

# 3. Konfigurasi file environment
copy .env.example .env
php artisan key:generate

# 4. Inisialisasi basis data multi-role (Least Privilege)
php artisan ven:setup-db

# 5. Jalankan migrasi skema database
php artisan migrate --database=mysql_dba

# 6. Jalankan seeder data awal (opsional)
php artisan db:seed

# 7. Jalankan development server
php artisan serve
```
Backend API akan berjalan di: `http://127.0.0.1:8000`

---

### 2. Menjalankan Aplikasi Android (Mobile)
Prasyarat: **Android Studio Ladybug / Meerkat**, **JDK 17**, dan **Android SDK 34/35**.

1. Buka folder [`ven-mobile/`](ven-mobile/) menggunakan **Android Studio**.
2. Salin `.env.example` ke `.env` di folder `ven-mobile/` (jika menggunakan konfigurasi API host dinamis).
3. Sambungkan perangkat fisik via USB Debugging atau jalankan Android Emulator.
4. Klik tombol **Run 'app'** (`Shift + F10`) di Android Studio.

---

### 3. Pengujian API Menggunakan Postman
Koleksi Postman siap pakai tersedia di direktori [`postman/`](postman/):

* **Impor File JSON Sekali Klik ke Postman:**
  * **Koleksi Audit Keamanan (7 Kategori)**: [`postman/VEN_Audit_Keamanan_Database.postman_collection.json`](postman/VEN_Audit_Keamanan_Database.postman_collection.json)
  * **Environment Lokal**: [`postman/VEN_Local.postman_environment.json`](postman/VEN_Local.postman_environment.json)
* **Koleksi Lengkap Semua Endpoint**: Tersedia di direktori `postman/collections/VEN API - Complete Routes Test`.

---

## 🧪 Pengujian Otomatis (Automated Testing)

Untuk memvalidasi integritas kode backend dan audit keamanan:
```powershell
cd backend-api/backend
php artisan test
```
**Status: 37 tests passed, 176 assertions (100% Lulus, 0 Error).**

---

## 📂 Struktur Direktori Proyek

```text
VEN/
├── backend-api/
│   └── backend/               # Proyek Laravel 11 Backend API
│       ├── app/               # Controllers, Models, Commands, Observers, Services
│       ├── config/            # Konfigurasi Backup, Layanan AI Gemini, Database
│       ├── database/          # Migrasi skema, Seeder, dan Tinker scripts
│       ├── docs/              # 7 Dokumen kepatuhan audit keamanan database
│       ├── routes/            # api.php, web.php, console.php (Scheduler)
│       └── tests/             # PHPUnit Automated Test Suites
├── ven-mobile/                # Proyek Android Kotlin (Jetpack Compose)
│   ├── app/src/main/java/     # Arsitektur UI (Screens, ViewModels, Theme)
│   └── docs/DESIGN.md         # Spesifikasi desain & interaksi UI Mobile
└── postman/                   # Koleksi & Environment Postman
    ├── collections/           # Koleksi workspace Postman terstruktur
    ├── environments/          # Variabel environment Postman
    └── *.json                 # File koleksi export siap impor
```

---

## 👥 Tim & Lisensi
Hak Cipta © 2026 Tim Pengembang **VEN (Virtual Exhibition Network)**. Seluruh hak cipta dilindungi undang-undang.

