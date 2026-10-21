# Test Enkripsi `google_id`

**File test:** `tests/Feature/EnkripsiGoogleIdTest.php`  
**Class:** `EnkripsiGoogleIdTest`  
**Jenis:** Feature  
**Jumlah test:** 3  
**Butuh database:** Ya (`ven_testing`)  
**Kunci yang dipakai:** `VEN_CUSTOM_KEY` dari `phpunit.xml`

## Tujuan

Memastikan `google_id` benar-benar tersimpan dalam bentuk terenkripsi di
database, tetap dapat dibaca kembali oleh aplikasi, serta memiliki penanda
pencarian (`google_id_bidx`) yang berfungsi.

## Cakupan test

| Method | Yang diuji |
|---|---|
| `test_google_id_tersimpan_terenkripsi_dan_accessor_mengembalikan_nilai_asli` | Kolom `google_id` di DB berisi `v1:...`, bukan nilai asli; accessor mengembalikan nilai asli |
| `test_blind_index_tersimpan_dan_pencarian_menemukan_pengguna` | `google_id_bidx` tersimpan dan pencarian memakai bidx menemukan pengguna |
| `test_data_rusak_di_database_tidak_membuat_crash` | Ciphertext rusak menghasilkan `null`, bukan error |

## Menjalankan dengan `php artisan test`

```powershell
cd C:\laragon\www\VEN\ven\backend-api\backend
```

Semua test di file ini:

```powershell
php artisan test tests/Feature/EnkripsiGoogleIdTest.php
```

Per method:

```powershell
php artisan test --filter=test_google_id_tersimpan_terenkripsi_dan_accessor_mengembalikan_nilai_asli
php artisan test --filter=test_blind_index_tersimpan_dan_pencarian_menemukan_pengguna
php artisan test --filter=test_data_rusak_di_database_tidak_membuat_crash
```

Seluruh class:

```powershell
php artisan test --filter=EnkripsiGoogleIdTest
```

## Menjalankan dengan PHPUnit langsung

```powershell
php vendor\bin\phpunit --testdox tests\Feature\EnkripsiGoogleIdTest.php
```

## Hasil yang diharapkan

```text
OK (3 tests, 10 assertions)
```

## Pengujian manual dengan tinker

Tinker memakai database `.env` (`ven`), bukan `ven_testing`. Gunakan data
dummy dan hapus kembali setelah selesai.

### 1. Simpan lalu baca

```php
$p = App\Models\Pengguna::create([
    'nama'      => 'Uji Enkripsi',
    'email'     => 'uji.enkripsi.'.time().'@example.test',
    'google_id' => '100000000000000000777',
]);

$p->getAttributes()['google_id'];   // ciphertext v1:...
$p->google_id;                      // 100000000000000000777
```

### 2. Cari lewat penanda pencarian

```php
$e = app(App\Services\Crypto\LayananEnkripsi::class);

App\Models\Pengguna::where('google_id_bidx', $e->blindIndex('100000000000000000777'))->first();
```

### 3. Simulasi data rusak

```php
Illuminate\Support\Facades\DB::table('pengguna')
    ->where('id_pengguna', $p->id_pengguna)
    ->update(['google_id' => 'v1:bukan-ciphertext-valid']);

$p->fresh()->google_id;   // null
```

### 4. Bersihkan data dummy

```php
$p->forceDelete();
```

## Catatan

- Test ini menulis ke `ven_testing` dan **tidak** memakai `RefreshDatabase`.
- `setUp()` memastikan koneksi bukan database aplikasi.
- Factory `denganGoogle()` dipakai untuk menyiapkan data uji.
- Untuk panduan umum, lihat [panduan-artisan-test.md](panduan-artisan-test.md).
