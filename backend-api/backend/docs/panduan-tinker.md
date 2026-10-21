# Panduan Pengujian Manual dengan Tinker

Dokumen ini menjelaskan cara menguji fitur aplikasi secara manual memakai
`php artisan tinker` (sering salah ditulis "thinker"). Tinker berguna untuk
mengecek data nyata di database tanpa membuat file test.

Dokumen terkait:

- [panduan-artisan-test.md](panduan-artisan-test.md) — menjalankan test otomatis
- [panduan-unit-test.md](panduan-unit-test.md) — test unit tanpa database
- [README.md](README.md) — daftar seluruh dokumen test

---

## 1. Prasyarat

1. Berada di folder backend:

   ```powershell
   cd C:\laragon\www\VEN\ven\backend-api\backend
   ```

2. Tinker terpasang (sudah termasuk dependency Laravel).

3. Jika `php` belum ada di PATH:

   ```powershell
   & "C:\laragon\bin\php\php-8.3.33-Win32-vs16-x64\php.exe" artisan tinker
   ```

4. Tinker memakai `.env` asli, sehingga database yang diakses adalah `ven`
   dan kunci yang dipakai adalah `VEN_CUSTOM_KEY` dari `.env` — berbeda dengan
   test otomatis yang memakai `ven_testing` dan kunci uji dari `phpunit.xml`.

Masuk ke tinker:

```powershell
php artisan tinker
```

Keluar dengan `exit` atau `Ctrl+D`.

---

## 2. Enkripsi dan dekripsi dasar

```php
$e = app(App\Services\Crypto\LayananEnkripsi::class);

$ct = $e->enkripsi('100000000000000000777');
$ct;
```

Contoh hasil:

```text
"v1:BKsyEuBtsmfGuz7rhjOs1PqGl2GuGxrPzxprgTeIzuxOa0OkaByc3n0b30uEi4SFyw=="
```

Dekripsi:

```php
$e->dekripsi($ct);
```

Contoh hasil:

```text
"100000000000000000777"
```

Enkripsi dua kali akan menghasilkan ciphertext berbeda, tetapi hasil
dekripsinya tetap sama:

```php
$e->enkripsi('100000000000000000777');
$e->enkripsi('100000000000000000777');
```

---

## 3. Uji model tanpa database

Cocok untuk memastikan accessor/mutator bekerja tanpa menulis apa pun ke DB.

```php
$p = new App\Models\Pengguna();
$p->google_id = '100000000000000000777';

$p->getAttributes()['google_id'];   // ciphertext
$p->google_id;                      // hasil dekripsi
```

Contoh hasil:

```text
"v1:8rUc03r85PSq7N1vqJvbzMyG6pTAIy3MCcZhGwGYG6icgtW5/kdhHBpMM4yAwb+qpw=="
"100000000000000000777"
```

---

## 4. Uji data nyata di database

### Berdasarkan email

```php
$p = App\Models\Pengguna::where('email', 'uji.enkripsi.1792574922@example.test')->first();

dump([
    'email'      => $p->email,
    'ciphertext' => $p->getAttributes()['google_id'],
    'dekripsi'   => $p->google_id,
    'bidx'       => $p->getAttributes()['google_id_bidx'],
]);
```

Contoh hasil:

```text
array:4 [
  "email" => "uji.enkripsi.1792574922@example.test"
  "ciphertext" => "v1:svhBTIs55v/WWQwwFPKuTI8EdIOgM0XOAPy3WPrSCVlFxajaiAHocx9Q4C5YEnBg9g=="
  "dekripsi" => "100000000000000000999"
  "bidx" => "8305b3022c2fa98d09adf283a32adc6835fa3748f44d36b9da061aa49bece3e0"
]
```

### Berdasarkan pola email

```php
App\Models\Pengguna::where('email', 'like', 'uji.%')
    ->orderBy('email')
    ->get()
    ->each(function ($p) {
        dump([
            'email'     => $p->email,
            'google_id' => $p->google_id,
            'status'    => $p->google_id ? 'OK' : 'GAGAL',
        ]);
    });
```

### Semua pengguna

```php
$e = app(App\Services\Crypto\LayananEnkripsi::class);

App\Models\Pengguna::orderBy('email')->get()->map(function ($p) use ($e) {
    $ct   = $p->getAttributes()['google_id'];
    $asli = $p->google_id;
    $bidx = $p->getAttributes()['google_id_bidx'];

    return [
        'email'     => $p->email,
        'google_id' => $asli ?? '-',
        'status'    => $ct === null ? 'tidak ada' : ($asli !== null ? 'OK' : 'GAGAL'),
        'bidx'      => ($bidx && $asli)
            ? ($bidx === $e->blindIndex($asli) ? 'cocok' : 'tidak cocok')
            : '-',
    ];
});
```

Arti `status`:

| Nilai | Arti |
|---|---|
| `OK` | Ciphertext berhasil didekripsi |
| `GAGAL` | Ciphertext tidak bisa dibuka (kunci salah/data rusak) |
| `tidak ada` | Pengguna tidak memiliki `google_id` |

Arti `bidx`:

| Nilai | Arti |
|---|---|
| `cocok` | Penanda pencarian sesuai dengan nilai hasil dekripsi |
| `tidak cocok` | `google_id` dan `google_id_bidx` tidak sinkron |

### Membaca kolom mentah tanpa accessor

```php
$ct = Illuminate\Support\Facades\DB::table('pengguna')
    ->where('email', 'uji.enkripsi.1792574922@example.test')
    ->value('google_id');

$e->dekripsi($ct);
```

---

## 5. Simulasi kegagalan

### Kunci berbeda

```php
$ct = app(App\Services\Crypto\LayananEnkripsi::class)
    ->enkripsi('100000000000000000777');

(new App\Services\Crypto\LayananEnkripsi('kunci-yang-salah'))->dekripsi($ct);
```

Hasil: `null` (dan tercatat di log).

### Data diubah

```php
$ct = app(App\Services\Crypto\LayananEnkripsi::class)
    ->enkripsi('100000000000000000777');

$mentah = base64_decode(substr($ct, 3), true);
$mentah[28] = chr(ord($mentah[28]) ^ 0xFF);

app(App\Services\Crypto\LayananEnkripsi::class)
    ->dekripsi('v1:'.base64_encode($mentah));
```

Hasil: `null`.

### Nilai tidak valid

```php
$e = app(App\Services\Crypto\LayananEnkripsi::class);

$e->dekripsi(null);
$e->dekripsi('');
$e->dekripsi('100000000000000000777');
$e->dekripsi('v1:not-base64!!!');
$e->dekripsi('v1:'.base64_encode('terlalu-pendek'));
```

Semua menghasilkan `null`.

---

## 6. Membuat pengguna dummy

### Klon dari baris yang sudah ada

Cocok jika ingin mempertahankan `google_id` yang sama dan hanya mengganti email.

```php
function klonPengguna(string $email): App\Models\Pengguna
{
    $sumber = App\Models\Pengguna::whereNotNull('google_id')->firstOrFail();

    $baru = $sumber->replicate(['id_pengguna', 'deleted_at']);
    $baru->email = $email;
    $baru->nama_panggilan = null;
    $baru->nomor_hp = null;
    $baru->save();

    return $baru;
}

$p = klonPengguna('dummy@example.test');
$p->google_id;
```

Penting: gunakan `replicate()` agar ciphertext dan bidx disalin apa adanya.
Jangan lakukan `$baru->google_id = $sumber->google_id`, karena mutator akan
mengenkripsi ulang ciphertext (enkripsi ganda).

### Buat dengan identitas Google baru

```php
function buatPenggunaUji(string $email, string $googleId): App\Models\Pengguna
{
    return App\Models\Pengguna::create([
        'nama'      => 'Pengguna Uji',
        'email'     => $email,
        'google_id' => $googleId,
    ]);
}

buatPenggunaUji('uji2@example.test', '100000000000000000123');
```

### Hapus data dummy

```php
App\Models\Pengguna::where('email', 'like', 'uji%')->forceDelete();
```

Atau per baris:

```php
$p->forceDelete();
```

---

## 7. Menjalankan tinker satu baris (PowerShell)

PowerShell membuang tanda kutip ganda. Gunakan kutip tunggal yang digandakan
(`''`) di dalam perintah:

```powershell
php artisan tinker --execute='echo App\Models\Pengguna::whereNotNull(''google_id'')->first()->google_id;'
```

Menampilkan ciphertext dan hasil dekripsi:

```powershell
php artisan tinker --execute='$p = App\Models\Pengguna::whereNotNull(''google_id'')->first(); $ct = $p->getAttributes()[''google_id'']; echo ''ciphertext: ''.PHP_EOL; echo $ct.PHP_EOL; echo ''dekripsi: ''.PHP_EOL; echo app(App\Services\Crypto\LayananEnkripsi::class)->dekripsi($ct).PHP_EOL;'
```

---

## 8. Kesalahan umum

| Gejala | Penyebab | Solusi |
|---|---|---|
| `Undefined constant "google_id"` | Kutip ganda dibuang PowerShell | Pakai mode interaktif atau kutip tunggal digandakan |
| `VEN_CUSTOM_KEY belum diisi` | `.env` tidak terbaca | Jalankan dari folder `backend-api/backend` |
| Hasil dekripsi `null` | Kunci berbeda atau data rusak | Pastikan kunci sama dengan saat enkripsi |
| Perubahan `.env` tidak berlaku | Config ter-cache | `php artisan config:clear` |
| `Call to undefined method` | Method/class salah tulis | Periksa namespace `App\Models\...` |

---

## 9. Catatan keamanan

- Tinker menampilkan data asli ke terminal. Jalankan hanya di lingkungan lokal.
- Jangan menyalin output berisi `google_id` ke laporan publik.
- Jangan menjalankan perintah tinker di server produksi tanpa kebutuhan jelas.
- Riwayat terminal dapat menyimpan perintah, tetapi bukan hasil output.
- Tinker memakai kunci asli dari `.env`, jadi hasilnya berbeda dengan test
  otomatis yang memakai kunci uji.
