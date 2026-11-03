---
name: ven-security-audit
description: >-
  Panduan keamanan data, enkripsi, otentikasi, dan audit trail pada sistem VEN.
  Gunakan skill ini saat mengaudit atau mengimplementasikan modul keamanan,
  enkripsi AES-256-GCM, Blind Index, OTP, password hashing, dan otentikasi Google OAuth.
---

# VEN Security & Audit Guide

Panduan standar keamanan informasi dan kriptografi di proyek VEN.

## 1. Enkripsi Sensitif (AES-256-GCM) & Blind Index
- **Kunci Rahasia**: `VEN_CUSTOM_KEY` di `.env` (32-byte base64).
- **Aturan Enkripsi Data Pribadi (PII)**:
  - Kolom seperti `google_id` disimpan terenkripsi menggunakan AES-256-GCM (dengan IV dan auth tag autentikasi).
  - Kolom pencarian cepat menggunakan **Blind Index** (`google_id_bidx`) yang di-hash dengan HMAC-SHA256 menggunakan `VEN_CUSTOM_KEY`.
  - Jangan pernah mendeskripsikan query `WHERE google_id = ?` secara plaintext; selalu gunakan pencarian pada kolom blind index: `WHERE google_id_bidx = blindIndex($id)`.

## 2. Otentikasi Kata Sandi & OTP
- **Kata Sandi**:
  - Hashing wajib menggunakan `Hash::make()` (Bcrypt).
  - Aturan kata sandi dikontrol oleh `App\Support\AturanKataSandi`.
- **OTP (One-Time Password)**:
  - Kode OTP dibuat acak (`random_int(100000, 999999)`).
  - OTP yang disimpan di database adalah hash (`hash('sha256', $kode)`).
  - Validasi ketat terhadap waktu kedaluwarsa (`expired_at`), status pemakaian (`used_at`), dan batas percobaan (`percobaan`).

## 3. Log Audit & Keamanan
- Seluruh insiden keamanan dicatat via `LogSistem::keamanan($event, $pesan, $statusKode, $idPengguna)`.
- Seluruh manipulasi data kritis (tambah/ubah/hapus) dicatat via `LogSistem::aktivitas(...)`.
- **Pembersihan Log**: Jangan pernah mencatat token otentikasi, kata sandi, OTP plaintext, atau IV/kunci enkripsi ke dalam kolom pesan log atau error report.

