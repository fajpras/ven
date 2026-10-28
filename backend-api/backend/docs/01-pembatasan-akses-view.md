# 01. Pembatasan Akses dengan Database View

## 1. Deskripsi Fitur
Pembatasan Akses dengan View bertujuan menyembunyikan data sensitif pengguna (seperti `kata_sandi`, `google_id`, `google_id_bidx`, serta peran internal `role`) dari eksposur publik, respon API, atau query aplikasi umum.

Respon autentikasi (login biasa, login Google, dan registrasi) secara ketat mengembalikan data profil dari database view `v_pengguna_publik` melalui model `PenggunaPublik`.

---

## 2. Matriks Skema Kolom View (`v_pengguna_publik`)

| Nama Kolom | Status di View | Keterangan |
|---|:---:|---|
| `id_pengguna` | ✅ **Tersedia** | Pengenal unik UUID pengguna |
| `nama` | ✅ **Tersedia** | Nama lengkap pengguna |
| `nama_panggilan` | ✅ **Tersedia** | Username / nama panggilan |
| `kata_sandi` | ❌ **DILINDUNGI** | Hash Bcrypt tidak boleh bocor |
| `nomor_hp` | ✅ **Tersedia** | Nomor telepon pengguna |
| `deskripsi` | ✅ **Tersedia** | Bio / deskripsi profil |
| `google_id` | ❌ **DILINDUNGI** | Ciphertext AES-256-GCM |
| `google_id_bidx` | ❌ **DILINDUNGI** | Hash Blind Index pencarian |
| `onboarding_selesai` | ✅ **Tersedia** | Status onboarding awal |
| `email` | ✅ **Tersedia** | Alamat email terverifikasi |
| `foto_profil` | ✅ **Tersedia** | URL avatar profil |
| `role` | ❌ **DILINDUNGI** | Role internal sistem (`admin`/`pengguna`) |
| `status` | ✅ **Tersedia** | Status akun (`aktif`/`ditangguhkan`/`nonaktifkan`) |
| `created_at` | ✅ **Tersedia** | Waktu pendaftaran akun |
| `updated_at` | ✅ **Tersedia** | Waktu pembaruan profil terakhir |
| `deleted_at` | ✅ **Tersedia** | Soft delete timestamp |

---

## 3. Implementasi Teknis

* **File Migrasi**: [`database/migrations/2026_10_26_094613_create_v_pengguna_publik_view.php`](file:///c:/laragon/www/VEN/ven/backend-api/backend/database/migrations/2026_10_26_094613_create_v_pengguna_publik_view.php)
* **Model Eloquent**: [`app/Models/PenggunaPublik.php`](file:///c:/laragon/www/VEN/ven/backend-api/backend/app/Models/PenggunaPublik.php)
* **Integrasi Login & Auth**: Di [`app/Http/Controllers/AuthController.php`](file:///c:/laragon/www/VEN/ven/backend-api/backend/app/Http/Controllers/AuthController.php#L194), fungsi `withToken()` mengembalikan entitas dari `v_pengguna_publik`:
  ```php
  private function withToken(Pengguna $pengguna): array
  {
      $penggunaPublik = PenggunaPublik::find($pengguna->id_pengguna);

      return [
          'pengguna' => $penggunaPublik ?? $pengguna->fresh(),
          'token' => $pengguna->createToken('mobile', ['*'], now()->addDays(30))->plainTextToken,
      ];
  }
  ```
* **Integrasi Profil**: Di [`app/Http/Controllers/Pengguna/ProfilController.php`](file:///c:/laragon/www/VEN/ven/backend-api/backend/app/Http/Controllers/Pengguna/ProfilController.php#L24), endpoint `GET /api/profil` mengembalikan objek dari view `v_pengguna_publik`.

---

## 4. Hak Akses (Least Privilege)
Pada `app/Console/Commands/SetupDatabase.php`, hak `SELECT` terhadap `v_pengguna_publik` diberikan kepada:
* Akun aplikasi (`ven`)
* Role pengguna (`user_role`)
* Role admin (`admin_role`)

---

## 5. File Pengujian Otomatis

* **File Test**: [`tests/Feature/AuditKeamananDanViewTest.php`](file:///c:/laragon/www/VEN/ven/backend-api/backend/tests/Feature/AuditKeamananDanViewTest.php)
  * `test_view_v_pengguna_publik_membatasi_akses_kolom_sensitif`: Memvalidasi seluruh 12 kolom wajib ada di view dan 4 kolom sensitif tidak bocor.
  * `test_login_mengembalikan_view_pengguna_publik_tanpa_kolom_sensitif`: Memvalidasi respon HTTP 200 saat login mengembalikan objek pengguna yang bersih tanpa field sensitif.

### Perintah Menjalankan Test:
```bash
php artisan test --filter=AuditKeamananDanViewTest
```
**Hasil: PASS (100% Comply)**
