---
name: laravel-backend-expert
description: >-
  Panduan dan standar arsitektur backend Laravel untuk projek VEN.
  Gunakan skill ini saat membuat atau mengubah controller, model, migration,
  service, middleware, observer, atau konfigurasi database Laravel.
---

# Laravel Backend Expert - VEN

Panduan arsitektur backend Laravel 11/13 pada projek VEN (`backend-api/backend`).

## 1. Multi-Role Database Connection (Least Privilege)
Backend VEN menggunakan pemisahan akun MySQL berdasarkan peran:
- `mysql` (`DB_APP_USERNAME`): Akun operasional aplikasi umum.
- `mysql_user` (`DB_USER_USERNAME`): Akun operasional pengguna terautentikasi (CRUD profil, minat, karya, pameran).
- `mysql_admin` (`DB_ADMIN_USERNAME`): Akun admin (moderasi, sanksi, log).
- `mysql_dba` (`DB_DBA_USERNAME`): Akun migrasi skema dan pencadangan.

### Middleware `PilihKoneksi`
- Koneksi default dialihkan otomatis oleh `App\Http\Middleware\PilihKoneksi` saat pengguna login.
- Perhatikan jika melakukan operasi lintas koneksi: koneksi log (`config('database.log_connection')`) terpisah dari koneksi data pengguna.

## 2. Transaksi & Logging Aktivitas
- **PENTING**: Observer aktivitas (`AktivitasObserver`) mengimplementasikan `ShouldHandleEventsAfterCommit`.
- Jangan pernah memanggil `LogSistem::aktivitas(...)` di dalam blok `DB::transaction(...)` jika di dalamnya ada update baris pengguna, karena koneksi log yang berbeda akan mengalami **lock wait / deadlock** saat memverifikasi foreign key `pengguna(id_pengguna)`.
- Jalankan logging aktivitas **setelah** transaksi commit:
  ```php
  DB::transaction(function () use ($data): void {
      // update data utama
  });

  // Logging dilakukan di luar transaksi
  LogSistem::aktivitas(
      entitas: $user->nama,
      jenis: 'kategori',
      pesan: 'Memperbarui minat kategori',
      idPengguna: $user->id_pengguna,
      aksi: 'ubah'
  );
  ```

## 3. Standar Controller & Response
- Controller mewarisi `App\Http\Controllers\Controller`.
- Gunakan response helper:
  - Sukses: `$this->ok($data, $message = 'OK', $status = 200)`
  - Gagal: `$this->gagal($message, $status = 400, $errors = null)`
- Gunakan Form Request untuk validasi payload yang kompleks (`app/Http/Requests/...`).

