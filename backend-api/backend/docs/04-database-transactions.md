# 04. Database Transactions (COMMIT & ROLLBACK)

## 1. Deskripsi Fitur
Menjamin sifat **ACID (Atomicity, Consistency, Isolation, Durability)** pada operasi database multitabel. Jika salah satu operasi gagal (misalnya upload file gagal atau validasi error di tengah proses), seluruh operasi database sebelumnya dibatalkan (**ROLLBACK**). Jika seluruh tahapan sukses, perubahan dipermanenkan (**COMMIT**).

## 2. Implementasi Teknis
- **Database Engine**: MySQL InnoDB (Mendukung transaksi dan Foreign Key).
- **Facade**: `Illuminate\Support\Facades\DB::transaction(function () { ... })`
- **Contoh Kasus**: Pembuatan Pameran (`app/Http/Controllers/Pengguna/PameranController.php`)
  - Simpan baris pameran
  - Simpan baris ruangan
  - Simpan file banner fisik
  - Catat log audit aktivitas
  - Jika salah satu gagal -> Otomatis ROLLBACK & hapus file banner fisik.

## 3. File Pengujian
- **Feature Test Transaksi Sederhana**: `tests/Feature/Transaksi/TransaksiSederhanaTest.php` (2 test)
  - `test_transaksi_sederhana_berhasil_commit`
  - `test_transaksi_sederhana_gagal_rollback`
- **Feature Test Transaksi Pameran (Real Case)**:
  - `tests/Feature/Transaksi/TambahPameranCommitTest.php` (1 test)
  - `tests/Feature/Transaksi/TambahPameranRollbackTest.php` (1 test)

### Cara Menjalankan Test:
```bash
# Uji seluruh modul transaksi
php artisan test tests/Feature/Transaksi
```

### Bukti Eksekusi:
File bukti transaksi otomatis di-generate ke:
- `storage/app/testing/bukti-transaksi-sukses.md`
- `storage/app/testing/bukti-transaksi-gagal.md`

Hasil: **PASS (100% Comply)**

