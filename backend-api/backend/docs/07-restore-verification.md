# 07. Restore Verification (Disaster Recovery)

## Deskripsi Fitur
Pencadangan database tidak berguna tanpa verifikasi pemulihan (restore) yang teruji. VEN menyediakan mekanisme pemulihan database dari file SQL dump secara aman dan terisolasi menggunakan utilitas `mysql` client command.

Fitur ini mendukung pemulihan ke database live maupun database sandbox/verifikasi (seperti `ven_restore` atau `ven_testing`) untuk memastikan integritas data dan kelayakan cadangan (*Disaster Recovery Preparedness*).

---

## Mekanisme & Konfigurasi

### 1. Command Artisan
Perintah restore dijalankan melalui Artisan:
```bash
php artisan ven:restore-db --path=storage/app/backups/backup_ven_2026-10-27_12-00-00.sql --db=ven_restore
```

**Parameter:**
- `--path`: (Wajib) Lokasi file SQL dump yang valid.
- `--db`: (Opsional) Nama database tujuan pemulihan (default: database utama di `.env`).

### 2. Kredensial DBA
Proses restore dijalankan dengan akun `ven_dba` (`mysql_dba`) yang telah diberikan hak akses:
```sql
GRANT ALL PRIVILEGES ON ven.* TO 'ven_dba'@'localhost';
GRANT ALL PRIVILEGES ON ven_restore.* TO 'ven_dba'@'localhost';
```

---

## Bukti Pengujian Otomatis
Pengujian automated test terdapat di [AuditBackupDanRestoreTest.php](file:///c:/laragon/www/VEN/ven/backend-api/backend/tests/Feature/AuditBackupDanRestoreTest.php):

```bash
php artisan test --filter=AuditBackupDanRestoreTest
```

**Skenario yang Diuji:**
1. Backup file dump sementara dibuat.
2. Perintah `ven:restore-db` dijalankan menyasar database pengujian.
3. Assertions memvalidasi kembalinya tabel dan record data secara utuh tanpa error sintaks SQL.

