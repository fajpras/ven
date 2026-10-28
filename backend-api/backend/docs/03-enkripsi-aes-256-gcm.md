# 03. Enkripsi Dua Arah AES-256-GCM & Blind Index

## 1. Deskripsi Fitur
Data identitas sensitif seperti `google_id` disimpan dalam bentuk terenkripsi menggunakan algoritma standar militer **AES-256-GCM** (*Galois/Counter Mode*) yang menjamin kerahasiaan (*confidentiality*) dan keaslian (*integrity/authenticity*).

Pencarian cepat (*exact match*) tetap dapat dilakukan secara aman menggunakan **Blind Index (HMAC-SHA256)** tanpa harus mendekripsi seluruh database.

## 2. Implementasi Teknis
- **Service Utama**: `app/Services/Crypto/LayananEnkripsi.php`
- **Cipher**: `aes-256-gcm` dengan Authentication Tag (16 bytes) dan Initialization Vector / Nonce unik (12 bytes).
- **Kunci Enkripsi**: Variabel `VEN_CUSTOM_KEY` di file `.env`.
- **Blind Index**: HMAC-SHA256 yang ditransformasikan menjadi representasi bidx di kolom `google_id_bidx`.

## 3. Komponen Pendukung
- **Artisan Command Migrasi/Enkripsi**: `app/Console/Commands/EnkripsiGoogleId.php`
  ```bash
  php artisan ven:enkripsi-google-id
  ```
- **Model Attribute Casts / Accessor**: Otomatis mengenkripsi saat disimpan dan mendekripsi saat dibaca.

## 4. File Pengujian
- **Unit Test (Layanan Enkripsi)**: `tests/Unit/LayananEnkripsiTest.php` (6 test)
- **Unit Test (Dekripsi Google ID)**: `tests/Unit/DekripsiGoogleIdTest.php` (8 test)
- **Feature Test (Penyimpanan & Blind Index DB)**: `tests/Feature/EnkripsiGoogleIdTest.php` (3 test)

### Cara Menjalankan Test:
```bash
# Uji Layanan Enkripsi AES-256-GCM
php artisan test tests/Unit/LayananEnkripsiTest.php

# Uji Integrasi Database & Blind Index
php artisan test tests/Feature/EnkripsiGoogleIdTest.php
```
Hasil: **PASS (100% Comply)**

