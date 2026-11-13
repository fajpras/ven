# Status Proyek VEN — Daftar Modul, Verifikasi, dan Kendala

> **Tanggal verifikasi:** 13 November 2026
>
> **Branch aktif:** `integration-test`
>
> **Commit terakhir:** `c19f7dc` — `feat(ui): redesign CreateOptionsBottomSheet with modern cards, badges, and gradient accents`
>
> **Perubahan terbaru:** `fix(api): selalu kembalikan JSON untuk endpoint yang tidak dapat diakses` — 13 November 2026 (commit lokal setelah `c19f7dc`)
>
> **Lingkungan:** Windows + Laragon, PHP 8.3.33, Laravel 13.34.0, MySQL, Android Gradle 9.6

Dokumen ini merangkum hasil audit langsung terhadap repositori: modul apa yang sudah selesai, apakah benar-benar bisa dijalankan, bagian mana yang masih berupa mock, serta kendala yang ditemukan beserta rekomendasi perbaikannya.

> ⚠️ **Dokumen hidup:** setiap penambahan/perubahan fitur wajib memperbarui file ini pada commit yang sama. Lihat aturan di [`.agents/rules/status-proyek.md`](../.agents/rules/status-proyek.md).

---

## 1. Ringkasan Hasil Verifikasi

| Pemeriksaan | Perintah | Hasil |
|---|---|---|
| Konfigurasi backend | `php artisan about` | ✅ Laravel 13.34.0, PHP 8.3.33, DB MySQL, storage link aktif |
| Daftar route backend | `php artisan route:list` | ✅ 99 route terdaftar |
| Test backend | `php artisan test` | ⚠️ **74 test — 73 lulus, 1 gagal** (357 assertion) |
| Fallback galat API | `php artisan test --filter=PenanganKesalahanApiTest` | ✅ **7 test lulus** — 401, 403, 404, 405, 422, 500 tanpa kebocoran detail internal |
| Smoke test API live | `artisan serve` + login `fajri@ven.test` | ✅ Login, `/api/pameran`, `/api/karya/rekomendasi`, `/api/profil`, `/api/pencarian`, `/api/pameran/23/room-dto` merespons normal |
| Admin Web Panel | `GET /admin/login` | ✅ HTTP 200 |
| Build Android | `.\gradlew.bat assembleDebug` | ✅ **BUILD SUCCESSFUL** |
| Artefak APK | `app/build/outputs/apk/debug/app-debug.apk` | ✅ Ada, 113.634.995 byte (~113,6 MB) |
| Test Android | `.\gradlew.bat testDebugUnitTest` | ⚠️ **66 test — 65 lulus, 1 gagal** |

**Catatan lingkungan:** perintah `php` tidak tersedia di PATH. Gunakan path Laragon:

```powershell
C:\laragon\bin\php\php-8.3.33-Win32-vs16-x64\php.exe artisan test
```

---

## 2. Modul yang Sudah Selesai dan Berjalan

### 2.1 Backend API (`backend-api/backend`)

| No | Modul | Implementasi / Endpoint | Status |
|:--:|---|---|:--:|
| 1 | Autentikasi & OTP | `auth/daftar/kirim-otp`, `auth/daftar`, `auth/masuk`, Google OAuth, `auth/lupa-kata-sandi`, `auth/reset-kata-sandi`, `auth/keluar`, throttle | ✅ |
| 2 | Keamanan data | AES-256-GCM + Blind Index (`google_id`), Bcrypt, view `v_pengguna_publik`, log keamanan | ✅ |
| 3 | Profil & minat | `profil` (GET/PUT), foto, ganti email via OTP, ganti kata sandi, status akun, kategori & minat | ✅ |
| 4 | Pameran | CRUD `pameran`, template `model-3d`, rekomendasi, upload banner, ruangan, `room-dto`, `exhibitions` (dari Unity) | ✅ |
| 5 | Karya | CRUD `karya`, upload media, penyajian file bertanda tangan, rekomendasi, update posisi objek | ✅ |
| 6 | Diskusi & Undangan | `undangan` (index/store/show/status/tanggapi), voice token | ✅ |
| 7 | Pencarian terpadu | `pencarian` (akun, karya, pameran) | ✅ |
| 8 | AI Chat | `ai/tanya` → Google Gemini / OpenAI-compatible fallback | ⚠️ kode siap, **API key kosong** |
| 9 | Admin API | pengguna, sanksi (`lockForUpdate`), moderasi karya/pameran, log aktivitas & keamanan | ✅ |
| 10 | Admin Web Panel | Login, akun, posts, exhibitions, activity-log (Blade + Tailwind) | ✅ |
| 11 | 7 Pilar Audit DB | View, hashing, enkripsi, transaksi ACID, locking, backup otomatis, restore verification | ✅ |
| 12 | Command & Scheduler | `ven:setup-db`, `ven:initialize-db`, `ven:backup-db`, `ven:restore-db`, `ven:enkripsi-google-id`, `ven:uji-transaksi-pameran` | ✅ |
| 13 | Penanganan galat API terpusat | `app/Exceptions/PenanganKesalahanApi.php` + `bootstrap/app.php`: semua `/api/*` selalu JSON `{data, message}` untuk 401/403/404/405/413/419/422/429/5xx, tanpa redirect `route('login')` dan tanpa stack trace | ✅ |

Dokumentasi teknis: `backend-api/backend/docs/`.

### 2.2 Aplikasi Android (`ven-mobile`)

| No | Modul | Bukti Implementasi | Status |
|:--:|---|---|:--:|
| 1 | Splash, Login, Register, OTP, Forgot/Reset Password | `ui/screens/auth/*`, `ApiLogin`, `ApiRegister` | ✅ |
| 2 | Onboarding minat | `InterestViewModel` → `ApiProfil.kategori()` / `minat()` | ✅ |
| 3 | Home feed & rekomendasi | `HomeViewModel` → `ApiHome` (`karya/rekomendasi`, `pameran/rekomendasi`), pull-to-refresh, double-tap tab | ✅ |
| 4 | Explore & pencarian | `ExploreViewModel` → `ApiPencarian`, filter kategori, grid 3 kolom | ✅ |
| 5 | Profil & User Profile | `ProfileUiState` → `ApiProfil` (profil, jumlah karya/pameran, karya milik) | ✅ |
| 6 | Settings akun | Ganti email, ganti kata sandi, status akun, tema gelap/terang | ✅ |
| 7 | Buat pameran (template & info) | `CreateExhibitViewModel`, upload banner via `ApiPameran` | ✅ |
| 8 | Viewer 3D Unity | `UnityLoadingScreen` + `UnityLauncher` + endpoint `room-dto`, fallback DTO anti black screen | ✅ |
| 9 | AI Chat | `AiChatViewModel` → `ApiAiChat` + fallback jawaban | ⚠️ backend tanpa API key |
| 10 | Theme engine | `ThemePreferences`, Material 3 | ✅ |

### 2.3 Artefak Pendukung

- **Postman:** `postman/` berisi koleksi *VEN API - Complete Routes Test*, *VEN Audit Keamanan & Database* (7 folder), *VEN API - Pengguna & Auth Tests*, beserta environment.
- **Dokumentasi:** `backend-api/backend/docs/` (7 pilar + panduan test) dan `ven-mobile/docs/DESIGN.md`.
- **APK debug:** `ven-mobile/app/build/outputs/apk/debug/app-debug.apk`.
- **Unity:** modul `unityLibrary` sudah di-include di `settings.gradle.kts` dan activity Unity terdaftar di `AndroidManifest.xml`.

---

## 3. Bagian yang Masih Mock / Belum Tersambung ke Backend

| Fitur | Lokasi | Kondisi |
|---|---|---|
| Schedule (Approved / Waiting / Request) | `ven-mobile/.../ui/screens/schedule/ScheduleScreen.kt:47,60,77` (`dummyScheduleUiState`) | 100% data lokal, belum ada ViewModel/API — padahal endpoint `undangan` sudah tersedia |
| Request Meeting | `.../requestmeeting/RequestMeetingViewModel.kt:104-106` (`delay(800)`) | Pengiriman hanya simulasi, belum memanggil API |
| Create Post / unggah karya | `.../create/post/CreatePostViewModel.kt:76-78` (`delay(400)`) | Simulasi; belum ada `ApiKarya` di mobile |
| Publish pameran dari editor Compose | `.../create/exhibit/editor/ExhibitEditorViewModel.kt:156-158` (`delay(800)`) | Simulasi; hanya jalur Unity yang menyimpan ke database |
| Riwayat aktivitas di Settings | `.../settings/HistoryScreen.kt:153` (`dummyHistoryEntries`) | Belum mengambil log aktivitas dari API |
| Riwayat chat AI | `.../ai/AiChatViewModel.kt:21` + `AiChatDummyData` | Hanya di memori, tidak persisten |
| Pilihan bahasa | `ui/navigation/AppNavigation.kt` (`selectedLanguage` state lokal) | Tidak dipersist |
| Privacy Policy / Help | `SettingsPlaceholderScreen.kt` | Masih placeholder |

---

## 4. Kendala dan Rekomendasi Perbaikan

### Prioritas Tinggi

| No | Kendala | Bukti | Dampak | Rekomendasi |
|:--:|---|---|---|---|
| 1 | Tabel `ven_testing.reset_kata_sandi` hilang | Migrasi tercatat di tabel `migrations`, tetapi tabel tidak ada. Test gagal: `tests/Feature/ProfilMinatTest.php:291` | 1 test backend gagal | Jalankan `php artisan migrate:fresh --database=mysql` pada database **ven_testing** (jangan `ven`) |
| 2 | `GEMINI_API_KEY` kosong | Uji live `POST /api/ai/tanya` → `"Layanan AI belum dikonfigurasi. Hubungi administrator."` | Fitur AI Assistant tidak berfungsi nyata; mobile menampilkan jawaban dummy | Isi `GEMINI_API_KEY` di `.env`, lalu `php artisan config:clear` |
| 3 | Unit test Android usang | `app/src/test/.../ui/navigation/ScreenRouteTest.kt:20` masih mengharapkan `create_exhibit_editor/tpl_small`, sedangkan `Screen.kt:42-55` kini menambahkan query `title/description/bannerUrl/tipe` | 1 test Android gagal (build tetap sukses) | Perbarui ekspektasi test sesuai format route terbaru |

### Prioritas Sedang

| No | Kendala | Dampak | Rekomendasi |
|:--:|---|---|---|
| 4 | Modul Schedule, Request Meeting, Create Post, dan Publish editor masih simulasi | Fitur terlihat jalan tetapi data tidak tersimpan ke server | Buat `ApiKarya` dan `ApiUndangan`, lalu hubungkan ViewModel ke endpoint yang sudah ada |
| 5 | Source Unity (`.cs`) tidak ada di repo, hanya binary `il2cpp` | Integrasi Unity tidak dapat diaudit dari source; verifikasi butuh perangkat/emulator | Simpan proyek Unity sebagai submodule/folder terpisah atau dokumentasikan prosedur build |
| 6 | Perubahan binary Unity belum di-commit | `libil2cpp.so` (arm64) termodifikasi dan `compile-data.json` masih untracked | Tinjau ulang lalu commit secara terkontrol (hindari commit binary besar tanpa kebutuhan) |

### Prioritas Rendah / Lingkungan

| No | Kendala | Rekomendasi |
|:--:|---|---|
| 7 | `BASE_URL` mobile default `http://10.0.2.2:8000/api/` | Hanya untuk emulator; ubah `ven-mobile/.env` untuk perangkat fisik |
| 8 | `android:usesCleartextTraffic="true"` aktif | Wajar untuk development; matikan dan gunakan HTTPS untuk rilis |
| 9 | README menyebut "37 tests passed" | Perbarui angka menjadi 74 test backend / 66 test Android |
| 10 | APK berukuran ~113,6 MB karena Unity | Pertimbangkan Android App Bundle atau ABI split |
| 11 | `php` tidak ada di PATH | Gunakan path Laragon atau tambahkan ke environment variable |

### Kendala yang Sudah Diperbaiki

| Tanggal | Kendala | Perbaikan | Bukti |
|---|---|---|---|
| 13 Nov 2026 | Request `/api/*` tanpa header `Accept: application/json` menghasilkan **HTTP 500** `Route [login] not defined.` | `bootstrap/app.php`: `redirectGuestsTo` mengembalikan `null` untuk prefix `api`; `app/Exceptions/PenanganKesalahanApi.php` merender semua galat API sebagai JSON `{data, message}` (401/403/404/405/413/419/422/429/5xx) tanpa membocorkan detail internal | `tests/Feature/PenanganKesalahanApiTest.php` — 7 test lulus; smoke test live `GET /api/profil` tanpa header → `401 application/json` |

---

## 5. Kesimpulan

- **Backend:** selesai dan terbukti berjalan. Seluruh modul inti (auth, profil, pameran, karya, undangan, pencarian, admin, audit keamanan, backup/restore) merespons normal pada smoke test live.
- **Android:** alur utama selesai dan berhasil di-build (APK tersedia). Autentikasi, home, explore, profil, settings, create exhibit, dan viewer Unity sudah terhubung API.
- **Belum selesai:** Schedule, Request Meeting, Create Post, publish dari editor Compose, riwayat aktivitas, dan AI live.
- **Kegagalan test** yang ada (1 backend, 1 Android) disebabkan masalah environment/test usang, bukan kegagalan build aplikasi.
- **Bug yang sudah diperbaiki (13 Nov 2026):** request API tanpa autentikasi kini selalu mengembalikan **401 JSON**, bukan 500 `Route [login] not defined`. Seluruh galat `/api/*` (404, 405, 422, 429, 5xx, dll.) memakai envelope JSON yang sama dan tidak menampilkan stack trace.

---

## 6. Cara Verifikasi Ulang

```powershell
# Backend
cd backend-api/backend
$php = "C:\laragon\bin\php\php-8.3.33-Win32-vs16-x64\php.exe"
& $php artisan about
& $php artisan route:list
& $php artisan test
& $php vendor\phpunit\phpunit\phpunit tests/Feature/PenanganKesalahanApiTest.php

# Android — build & unit test
cd ..\..\ven-mobile
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest

# Smoke test API live (terminal terpisah)
cd ..\backend-api\backend
& $php artisan serve --host=127.0.0.1 --port=8000
# Login: POST http://127.0.0.1:8000/api/auth/masuk
# Body: {"identitas":"fajri@ven.test","kata_sandi":"password123"}
```

**Database:** test wajib memakai `ven_testing`; database aplikasi adalah `ven`. Jangan arahkan test ke `ven`.
