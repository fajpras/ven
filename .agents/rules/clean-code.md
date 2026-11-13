# Aturan Koding Bersih (Clean Code) & Struktur Folder Profesional

Aturan ini wajib ditaati oleh AI Agent saat menulis, merefaktor, atau mengorganisasi kode pada proyek VEN.

---

## 1. Prinsip Utama Clean Code

1. **Single Responsibility Principle (SRP)**:
   - Satu file / kelas / fungsi hanya bertanggung jawab atas satu hal.
   - Controller tidak boleh berisi query SQL mentah atau kalkulasi bisnis yang rumit; delegasikan ke Service class.
   - Composable UI tidak boleh memanggil network request secara langsung; delegasikan ke ViewModel.

2. **Keterbacaan & Penamaan yang Jelas (Self-Documenting Code)**:
   - Nama variabel, metode, dan kelas harus langsung mendeskripsikan tujuan, bukan singkatan ambigu.
   - PHP/Laravel:
     - Kelas / Trait / Enum: `PascalCase` (contoh: `PenggunaKategori`, `LayananOtp`).
     - Metode & Variabel: `camelCase` (contoh: `kirimOtpGantiEmail`, `statusVerifikasi`).
     - Kolom Basis Data: `snake_case` (contoh: `id_pengguna`, `onboarding_selesai`).
   - Kotlin/Android:
     - Kelas / Interface: `PascalCase` (contoh: `InterestViewModel`, `InterestUiState`).
     - Composable Functions: `PascalCase` (contoh: `InterestCategoryChips`, `LoginScreen`).
     - Fungsi & Properti: `camelCase` (contoh: `fetchCategories`, `selectedCategoryIds`).
     - Konstanta: `UPPER_SNAKE_CASE` (contoh: `DEFAULT_TIMEOUT_SECONDS`).

3. **KISS (Keep It Simple, Stupid) & DRY (Don't Repeat Yourself)**:
   - Jangan membuat abstraksi atau antarmuka jika baru digunakan di satu tempat (*no premature optimization*).
   - Ekstrak komponen berulang ke folder `components/` (Android) atau `Support/` / `Services/` (Laravel).

4. **Ketik Kuat & Null Safety (Strict Types & Safety)**:
   - PHP: Selalu tuliskan *type hint* lengkap pada parameter fungsi dan *return type* (`: JsonResponse`, `: bool`, `: void`).
   - Kotlin: Utamakan `val` daripada `var`. Hindari operator `!!` (*force unwrapping*); gunakan safe call `?.`, elvis operator `?:`, atau `requireNotNull`.

5. **Kebersihan Kode (Zero Dead Code)**:
   - Hapus *unused imports* / namespace yang tidak terpakai.
   - Jangan tinggalkan kode mati (*commented-out blocks*), panggilan `dd()`, `dump()`, atau log debugging liar di file produksi.

---

## 2. Struktur Folder Profesional: Backend Laravel (`backend-api/backend`)

Organisasi folder menggunakan perpaduan **Layered** dan **Domain Grouping**:

```text
app/
├── Contracts/              # Abstraksi antarmuka (contoh: PembuatKodeOtp)
├── Enums/                  # Nilai tetap berskala sistem (contoh: TujuanOtp, HasilVerifikasiOtp)
├── Exceptions/             # Custom exception per domain (contoh: KesalahanOtp)
├── Http/
│   ├── Controllers/        # Dikelompokkan per domain/fitur:
│   │   ├── Auth/           # AuthController, GoogleAuthController
│   │   ├── Pengguna/       # ProfilController, PenggunaController
│   │   ├── Admin/          # ModerasiController, LogController
│   │   └── Pameran/        # PameranController, KaryaController
│   ├── Middleware/         # Filter & otorisasi request (contoh: PilihKoneksi, CatatKeamanan)
│   └── Requests/           # Validasi payload FormRequest per domain (contoh: Profil/, Auth/)
├── Models/                 # Eloquent entities (casts, relasi, scopes)
├── Observers/              # Lifecycle event handler (contoh: AktivitasObserver)
├── Services/               # Logika bisnis terisolasi:
│   ├── Otp/                # LayananOtp, PembuatKodeAcak
│   ├── Enkripsi/           # LayananEnkripsi
│   └── LogSistem.php       # Fasade pencatatan log sistem
└── Support/                # Helper dan utility murni (contoh: AturanKataSandi)
```

**Aturan Penempatan File Backend**:
- **Jangan menaruh logika bisnis di Controller**: Pindahkan ke `app/Services/`.
- **Jangan menaruh validasi array panjang di Controller**: Buat `app/Http/Requests/<Domain>/<NamaAction>Request.php`.
- **Gunakan Model Casts modern**: Gunakan method `casts(): array` alih-alih properti `$casts`.

---

## 3. Struktur Folder Profesional: Mobile Client (`ven-mobile`)

Organisasi folder menggunakan pendekatan **Feature-First**:

```text
app/src/main/java/com/ven/app/
├── core/                   # Konfigurasi global & constants aplikasi
│   └── config/             # AppConfig.kt
├── data/                   # Lapisan Data & Jaringan
│   ├── api/                # Layanan Retrofit, ApiClient, SessionManager
│   │   └── models/         # DTO serializable data classes (AuthModels, ProfilModels)
│   └── preference/         # DataStore / SharedPreferences (ThemePreferences)
├── ui/                     # Lapisan Presentasi
│   ├── components/         # Reusable widgets antarmuka
│   │   ├── feature/        # Komponen spesifik domain yang dapat dipakai ulang (InterestCategoryChips)
│   │   ├── layout/         # Header, Footer, Scaffold containers
│   │   └── ui/             # Primitive UI buttons, dialogs, loaders
│   ├── icons/              # Vektor ikon kustom (VexIcons.kt)
│   ├── navigation/         # NavHost, Screen route definitions, BottomNav
│   ├── screens/            # Layar fitur (Feature-First):
│   │   └── [feature]/      # auth/, onboarding/, settings/, profile/, explore/
│   │       ├── [Feature]Screen.kt      # Stateless Composable (UI layout murni)
│   │       ├── [Feature]Route.kt       # Stateful Composable (koneksi ViewModel & Navigasi)
│   │       ├── [Feature]ViewModel.kt   # Pengelola State & Bisnis UI
│   │       └── [Feature]UiState.kt     # Immutable UI State data class
│   └── theme/              # Styling Material 3 (Color, Type, Theme, Shape, Dimens)
└── util/                   # Utilitas murni non-UI (DateFormatter, StringHelper)
```

**Aturan Penempatan File Mobile**:
- Setiap layar kompleks **wajib** memisahkan `Screen.kt` (stateless), `Route.kt` (stateful), `ViewModel.kt`, dan `UiState.kt`.
- Komponen visual kecil yang dipakai di lebih dari satu layar wajib ditaruh di `ui/components/`.
- Semua data class jaringan (DTO) diletakkan di `data/api/models/`.

