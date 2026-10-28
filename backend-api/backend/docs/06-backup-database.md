# 06. Backup Database (Otomatis & Manual)

## Deskripsi Fitur
Sistem VEN mengimplementasikan strategi pencadangan database (backup) berkala yang komprehensif, mencakup backup otomatis terjadwal (cron) dan perintah manual on-demand menggunakan utilitas `mysqldump`.

Proses backup ini menggunakan koneksi DBA terdedikasi (`mysql_dba` dengan user `ven_dba`) yang memiliki izin `PROCESS` dan akses dump penuh tanpa mengorbankan keamanan koneksi aplikasi utama (`ven`).

---

## Mekanisme & Konfigurasi

### 1. Backup Otomatis Terjadwal (Cron Job)
Dikonfigurasi di [routes/console.php](file:///c:/laragon/www/VEN/ven/backend-api/backend/routes/console.php):
- **Jadwal Eksekusi**: Setiap hari pukul 00:00 (Tengah malam)
  ```cron
  0 0 * * * php artisan ven:backup-db
  ```
- **Retensi File Otomatis**: Setiap hari pukul 03:00, file backup yang berusia lebih dari **30 hari** akan dihapus otomatis dari media penyimpanan untuk menghemat kapasitas disk.

### 2. Backup Manual (CLI Command)
Administrator atau DBA dapat menjalankan backup instan kapan saja melalui command Artisan:
```bash
php artisan ven:backup-db
```
Atau menyasar database spesifik:
```bash
php artisan ven:backup-db --db=ven
```

File output disimpan di:
`storage/app/backups/backup_ven_YYYY-MM-DD_HH-ii-ss.sql`

---

## Bukti Pengujian Otomatis
Pengujian automated test terdapat di [AuditBackupDanRestoreTest.php](file:///c:/laragon/www/VEN/ven/backend-api/backend/tests/Feature/AuditBackupDanRestoreTest.php):

```bash
php artisan test --filter=AuditBackupDanRestoreTest
```

**Hasil Verifikasi:**
- Perintah menghasilkan file `.sql` valid di direktori `storage/app/backups/`.
- File dump berisi struktur schema `CREATE TABLE` dan data transaksi.

