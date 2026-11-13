# AI Agent Rules - VEN (Virtual Exhibition Navigator)

Dokumen ini memuat panduan utama bagi AI Coding Agent dalam mengembangkan dan memelihara ekosistem VEN (Backend Laravel & Mobile Android).

---

## 1. Arsitektur Proyek
- **Backend API**: `backend-api/backend` (Laravel 11/13, PHP 8.3, MySQL Multi-Role)
- **Mobile Client**: `ven-mobile` (Android Kotlin, Jetpack Compose, Material 3)

---

## 2. Prinsip Kerja AI Agent
1. **Koding Bersih & Struktur Rapi (Clean Code & Architecture)**:
   - Patuhi aturan di `.agents/rules/clean-code.md` dan skill `clean-code-architecture`.
   - Gunakan *skinny controller*, pisahkan logika bisnis ke `app/Services/`, dan validasi ke `app/Http/Requests/`.
   - Di Android, terapkan Unidirectional Data Flow (UDF) dengan memisahkan `Screen.kt` (stateless), `Route.kt` (stateful), `ViewModel.kt`, dan `UiState.kt` dalam subfolder fitur masing-masing.
   - Tidak meninggalkan dead code, import yang tidak terpakai, atau debug statement liar (`dd()`, `dump()`, `Log.d` sementara).
2. **Verifikasi Melalui Testing (TDD First)**:
   - Sebelum menyatakan tugas selesai, selalu jalankan test suite terkait di `backend-api/backend` via PHPUnit (`vendor/phpunit/phpunit/phpunit <path-test>`).
   - Testing wajib menggunakan database `ven_testing`, dilarang mengubah konfigurasi untuk menyentuh database produksi `ven`.
3. **Larangan Git Push Otomatis untuk AI (Manual by User Only)**:
   - AI Agent dilarang keras menjalankan perintah `git push` secara otomatis dalam bentuk apa pun.
   - Semua pekerjaan AI hanya sebatas commit lokal (`git commit`).
   - Eksekusi `git push` ke remote repository adalah hak eksklusif yang hanya dilakukan secara manual oleh Pengguna (Developer).
   - Jangan membuat hook atau konfigurasi sistem yang menghalangi Pengguna dalam menjalankan `git push`.
   - Commit yang sudah terjadi di masa lalu dibiarkan saja.
4. **Keamanan & Privasi Data**:
   - Seluruh data sensitif pihak ketiga (Google ID) wajib dienkripsi dengan AES-256-GCM dan didampingi Blind Index via `VEN_CUSTOM_KEY`.
   - Jangan pernah menampilkan plain kata sandi, token OTP, atau kunci enkripsi di logging atau JSON response publik.
   - Patuhi arsitektur MySQL Multi-Role (*Least Privilege*): `mysql_user`, `mysql_admin`, `mysql_dba`, `mysql` (app).
5. **Database Concurrency & Logging**:
   - Observer aktivitas harus mengimplementasikan `ShouldHandleEventsAfterCommit`.
   - Pemanggilan manual `LogSistem::aktivitas` tidak boleh dilakukan di dalam transaksi data yang belum di-commit untuk mencegah *lock-wait* antar koneksi PDO.
6. **Sinkronisasi Status Proyek (Wajib Saat Ada Fitur Baru)**:
   - Setiap penambahan, perubahan, atau penghapusan fitur **wajib memperbarui** `docs/STATUS-PROYEK-VEN.md` pada perubahan/commit yang sama.
   - Status modul harus diubah sesuai kondisi nyata: `✅ Selesai`, `⚠️ Sebagian`, atau `❌ Belum/Mock`.
   - Modul yang sebelumnya mock dan kini sudah tersambung API harus dipindahkan dari bagian "Masih Mock" ke "Sudah Selesai".
   - Angka hasil test (backend/Android), tanggal verifikasi, branch, dan commit wajib diperbarui.
   - Dilarang menandai fitur "selesai" tanpa bukti test/build/smoke test.
   - Patuhi detail aturan di `.agents/rules/status-proyek.md`.

---

## 3. Skills yang Tersedia
Bila menangani tugas spesifik, aktifkan skill di `.agents/skills/`:
- `clean-code-architecture`: Standar koding bersih, refactoring, dan penataan struktur folder profesional.
- `laravel-backend-expert`: Standar backend Laravel, controller, service, multi-koneksi DB VEN.
- `laravel-best-practices`: Indeks aturan mendalam untuk optimasi query, Eloquent, validasi, dan job queue.
- `android-compose-expert`: Standar Android Kotlin, Jetpack Compose, MVVM/MVI.
- `ven-security-audit`: Standar keamanan AES-256-GCM, Blind Index, OTP, RBAC.
- `ven-tdd-testing`: Standar pengujian PHPUnit dan isolasi fixture test.

---

## 4. Aturan Tambahan (Rules)
Selain prinsip kerja di atas, AI Agent wajib membaca dan mematuhi aturan berikut:
- `.agents/rules/clean-code.md`: Standar koding bersih dan struktur folder.
- `.agents/rules/no-git-push.md`: Larangan `git push` otomatis oleh AI.
- `.agents/rules/status-proyek.md`: Kewajiban memperbarui status proyek setiap ada penambahan fitur.

