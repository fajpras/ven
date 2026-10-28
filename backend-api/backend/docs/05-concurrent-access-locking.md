# 05. Concurrent Access & Pessimistic Locking

## 1. Deskripsi Fitur
Mencegah terjadinya kondisi balapan (*race condition*) atau modifikasi ganda pada data penting saat diakses secara bersamaan (*concurrent access*).

Sistem menerapkan **Pessimistic Locking** (`SELECT ... FOR UPDATE`) pada level baris (*row-level lock*) di dalam blok transaksi InnoDB.

## 2. Implementasi Teknis
- **Keyword**: `->lockForUpdate()` di Eloquent Query Builder.
- **Lokasi Utama**: `app/Http/Controllers/Admin/PenggunaController.php`
  - Pemberian sanksi akun (`beriSanksi`)
  - Pembaruan sanksi akun (`perbaruiSanksi`)
  - Pencabutan sanksi akun (`cabutSanksi`)
- **Mekanisme**:
  Saat admin A sedang memproses sanksi untuk pengguna X, row pengguna X dikunci (`FOR UPDATE`). Request simultan dari admin B akan menunggu (*wait queue*) hingga transaksi admin A selesai di-COMMIT.

## 3. File Pengujian
- **Feature Test Pessimistic Lock**: `tests/Feature/PenggunaSanksiLockTest.php`
  - Method: `test_sanksi_store_update_dan_cabut_berhasil_dengan_lock`
- **Feature Test Sanksi Sukses**: `tests/Feature/PenggunaSanksiSuksesTest.php`
- **Feature Test Sanksi Gagal**: `tests/Feature/PenggunaSanksiGagalTest.php`

### Cara Menjalankan Test:
```bash
php artisan test tests/Feature/PenggunaSanksiLockTest.php
```
Hasil: **PASS (100% Comply)**

