---
name: ven-tdd-testing
description: >-
  Panduan eksekusi pengujian otomatis (TDD) dan PHPUnit untuk backend VEN.
  Gunakan skill ini sebelum menyelesaikan tugas pemrograman, saat membuat test baru,
  atau saat memverifikasi fungsionalitas backend di ven/backend-api/backend.
---

# VEN TDD & Testing Guide

Panduan pengujian unit dan fitur di direktori `ven/backend-api/backend`.

## 1. Lingkungan Basis Data Testing
- Pengujian PHPUnit **HARUS** menggunakan database `ven_testing` (didefinisikan di `phpunit.xml`).
- Dilarang keras mengarahkan pengujian ke database aplikasi `ven`.
- Pastikan assertion di awal test:
  ```php
  $this->assertSame('ven_testing', DB::connection('mysql')->getDatabaseName());
  ```

## 2. Cara Menjalankan Pengujian
Gunakan PHP CLI dan binary PHPUnit lokal:
```powershell
cd ven\backend-api\backend
C:\laragon\bin\php\php-8.3.33-Win32-vs16-x64\php.exe vendor/phpunit/phpunit/phpunit <path-file-test>
```

Contoh filter metode spesifik:
```powershell
C:\laragon\bin\php\php-8.3.33-Win32-vs16-x64\php.exe vendor/phpunit/phpunit/phpunit --filter test_nama_metode <path-file-test>
```

## 3. Penanganan Fixture & Pembersihan Data (TearDown)
- Hapus token Sanctum sebelum menghapus user dummy: `$user->tokens()->delete();`.
- Jika menggunakan `forceDelete()` pada model `Pengguna`, pastikan relasi `log_aktivitas` tidak melanggar foreign key constraint (observer otomatis menangani dengan `pelakuId = null`).
- Bersihkan data pivot sebelum menghapus model induk untuk mencegah constraint violation.

