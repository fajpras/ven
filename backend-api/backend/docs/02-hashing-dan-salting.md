# 02. Hashing dan Salting Kata Sandi

## 1. Deskripsi Fitur
Seluruh kata sandi (*password*) pengguna wajib di-hash menggunakan algoritma satu arah yang kuat dengan salt unik otomatis, sehingga tidak dapat didekripsi balik (*irreversible*) dan tahan terhadap serangan *rainbow table*.

## 2. Implementasi Teknis
- **Algoritma**: Bcrypt (Blowfish-based cipher) dengan prefix `$2y$`.
- **Salting**: Dibuat secara acak dan otomatis oleh Bcrypt pada setiap enkripsi. Dua kata sandi yang persis sama akan menghasilkan hash berbeda.
- **Implementasi**: `Illuminate\Support\Facades\Hash::make()` dan `Hash::check()`.
- **Rounds (Work Factor)**: 12 pada environment production/local, 4 pada testing (`phpunit.xml`).

## 3. Titik Penggunaan Utama
- **Registrasi Akun**: `app/Http/Controllers/AuthController.php` (method `register`).
- **Reset Kata Sandi**: `app/Http/Controllers/AuthController.php` (method `resetKataSandi`).
- **Seeder Pengguna**: `database/seeders/PenggunaSeeder.php`.

## 4. File Pengujian
- **File Test**: `tests/Feature/AuditKeamananDanViewTest.php`
- **Method**: `test_hashing_dan_salting_password`

### Verifikasi yang Diuji:
1. Format hash diawali `$2y$`.
2. Input password yang sama menghasilkan 2 hash yang berbeda (Salting unik terbukti aktif).
3. `Hash::check` berhasil mencocokkan password benar dan menolak password salah.

### Cara Menjalankan Test:
```bash
php artisan test --filter=test_hashing_dan_salting_password
```
Hasil: **PASS (100% Comply)**

