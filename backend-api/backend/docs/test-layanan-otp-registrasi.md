# Test Layanan OTP Registrasi

**File test:** `tests/Feature/LayananOtpRegistrasiTest.php`  
**Class:** `LayananOtpRegistrasiTest`  
**Jenis:** Feature  
**Jumlah test:** 1  
**Butuh database:** Ya (`ven_testing`)

## Tujuan

Memastikan kode OTP untuk registrasi dapat diterbitkan, dikirim melalui email,
dan diverifikasi dengan benar, serta ditandai sudah digunakan setelah verifikasi.

## Cakupan test

| Method | Yang diuji |
|---|---|
| `test_registration_otp_can_be_issued_and_verified` | Penerbitan OTP, pengiriman email, verifikasi kode, dan penandaan `used_at` |

Alur test:

1. Email difungsikan sebagai tiruan (`Mail::fake()`).
2. Pembuat kode OTP diganti dengan kode tetap `123456`.
3. OTP diterbitkan untuk alamat `new-user@example.com`.
4. Test memastikan email berisi kode `123456` terkirim.
5. OTP diverifikasi.
6. Test memastikan baris OTP sudah memiliki `used_at` (sudah dipakai).

## Menjalankan dengan `php artisan test`

```powershell
cd C:\laragon\www\VEN\ven\backend-api\backend
```

```powershell
php artisan test tests/Feature/LayananOtpRegistrasiTest.php
```

Per method:

```powershell
php artisan test --filter=test_registration_otp_can_be_issued_and_verified
```

## Menjalankan dengan PHPUnit langsung

```powershell
php vendor\bin\phpunit --testdox tests\Feature\LayananOtpRegistrasiTest.php
```

## Hasil yang diharapkan

```text
OK (1 test, 3 assertions)
```

## Pengujian manual dengan tinker

Test ini membuat dan menghapus tabel `reset_kata_sandi` secara otomatis
(`setUp` dan `tearDown`), sehingga pengujian manual lewat tinker tidak
disarankan untuk alur lengkapnya.

Yang dapat diperiksa secara manual adalah isi tabel setelah OTP diterbitkan:

```php
Illuminate\Support\Facades\DB::table('reset_kata_sandi')
    ->orderByDesc('created_at')
    ->limit(5)
    ->get();
```

Perhatikan bahwa yang tersimpan adalah hash OTP, bukan kode aslinya. Kode asli
hanya dikirim melalui email.

Jika perlu mencoba alur secara manual, gunakan lingkungan dengan konfigurasi
email aktif, lalu panggil layanan OTP dari tinker. Namun pengujian otomatis
lebih aman karena email tidak benar-benar dikirim.

## Catatan penting

- Test membuat tabel `reset_kata_sandi` sementara di `ven_testing`.
- Jika test berhenti di tengah, tabel bisa tertinggal. Hapus dengan:

  ```sql
  DROP TABLE ven_testing.reset_kata_sandi;
  ```

- OTP disimpan dalam bentuk hash dan memiliki masa berlaku.
- Untuk panduan umum, lihat [panduan-artisan-test.md](panduan-artisan-test.md).
