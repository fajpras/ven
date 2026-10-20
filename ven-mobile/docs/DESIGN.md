# design.md: VEN / VEX (Virtual Exhibition), Android (Kotlin + Jetpack Compose) & Admin Web Panel

> **Versi:** 0.6.9 (Penyelarasan Total Tampilan & Alur Create Exhibition Editor 3D sesuai Wireframe Resmi Figma: Main Screen Checklist, Details View, Panels, Furniture, dan Color Docked Bottom Panels)
> **Pembaca utama:** AI/developer yang mengimplementasikan UI.
> **Penanda:**
> - `[OBSERVED]` = diambil langsung dari wireframe/Figma.
> - `[ASSUMPTION]` = keputusan sementara karena wireframe tidak menunjukkannya. Boleh diubah, tapi ubah di dokumen ini dulu.
> - `[PLACEHOLDER]` = nilai sementara yang memang akan diganti (mis. warna aksen).
> - `[TODO]` = belum diputuskan / belum ada wireframe. **Jangan menebak; tanyakan atau beri placeholder.**

---

## 1. Konteks Produk

- **Produk:** Aplikasi Android untuk pameran virtual karya (poster, desain, proyek 3D/game/software). Pengguna mengunggah karya (*post*), membuat ruang pameran 3D (*exhibit*), menjadwalkan/menerima permintaan pertemuan (*room*), dan memakai asisten AI untuk menemukan karya.
- **Stack:** Kotlin + Jetpack Compose. Frame acuan wireframe **412 × 943** → anggap **1 px wireframe = 1 dp**. Teks dalam `sp`.
- **Tema default: DARK.** Light mode tetap didukung. Wireframe dark sudah diobservasi langsung dari aset desain resmi untuk: **Login (Auth - node 3946:17), Register, Onboard (Interest), Forgot Password (node 4372:689), Home, Request Meeting, Schedule (6 wireframe: Approved, Waiting, Request + Bottom Sheets), AI Chat (drawer), Create Post, Create Exhibit**; halaman lain masih light dan diturunkan dari token yang sama.
- **Kesan visual:** bersih, ruang kosong lega, bentuk membulat (pill), konten karya pengguna (berwarna-warni) jadi bintang. UI harus netral supaya tidak bersaing dengan konten. Satu warna aksen: ungu.
- **Bahasa UI:** Inggris `[OBSERVED]`, tetapi ada pengaturan *Language*, jadi **semua teks lewat `strings.xml`**, tidak boleh hardcode di Composable.

---

## 2. Aturan Emas (wajib dipatuhi AI)

1. **Dilarang** `Color(0xFF…)`, `.dp`, `.sp`, `RoundedCornerShape(n.dp)`, atau `FontFamily` langsung di screen/komponen. Semua lewat token di `ui/theme/` (`VexTheme.colors`, `VexSpace`, `VexSize`, `VexRadius`, `MaterialTheme.typography`).
2. Butuh nilai baru? **Tambahkan dulu ke `ui/theme/`** (dan dokumentasikan di sini), baru dipakai.
3. Komponen dipindah ke `ui/components/` **hanya jika dipakai di 2+ screen**. Komponen khusus satu screen tinggal di folder screen itu.
4. Cek `ui/components/` sebelum membuat komponen baru.
5. Jangan ubah komponen bersama tanpa menyebut screen mana saja yang terdampak.
6. Setiap layar berdata wajib punya state: **Loading (skeleton), Empty, Error, Content**. Komponen interaktif wajib punya: enabled, pressed, disabled (dan loading bila async).
7. Composable **stateless** (state hoisting). Parameter `modifier: Modifier = Modifier` sebagai parameter opsional pertama. Setiap komponen wajib punya `@Preview` **dark dan light**.
8. Pakai warna **semantik** (`VexTheme.colors.surface`), bukan nama warna mentah.
9. Jangan menambah library (gambar, UI, dsb.) tanpa persetujuan. **Ikon & logo hanya dari aset Figma milik sendiri lewat `VexIcons`** (§7); jangan memakai `Icons.Default.*` atau library ikon lain.
10. **Jangan memakai default tampilan Material 3** untuk komponen yang punya gaya khusus di bawah (BottomNav, Tabs, TextField, Button). Material 3 dipakai sebagai fondasi (theming, ripple, semantics, ModalBottomSheet, dsb.), bukan sebagai tampilan.

---

## 3. Struktur Proyek & Arsitektur Sistem

Proyek **VEN (Virtual Exhibition)** menggunakan arsitektur terintegrasi yang memadukan **Backend RESTful API & Admin Web Panel** di dalam Laravel, serta **Mobile Client** Android. Laravel difungsikan ganda sebagai penyedia **RESTful API** untuk aplikasi mobile dan **Web Dashboard Admin** berbasis Blade Views yang dikompilasi dengan **Vite + Tailwind CSS v4**. Antarmuka mobile dibangun sebagai aplikasi Android native (**Kotlin + Jetpack Compose**).

### 3.1 Struktur Repositori Keseluruhan (`ven`)

```
ven/
├── backend-api/
│   └── backend/                     # BACKEND & ADMIN WEB: Laravel 12 + Blade + Vite + Tailwind CSS v4
│       ├── app/
│       │   ├── Http/Controllers/    # Endpoint API & Web Admin Controllers (Admin\Web\*)
│       │   ├── Models/              # Eloquent Models (Pengguna, Karya, Pameran, Objek, SanksiAkun, LogAktivitas)
│       │   └── Middleware/          # Sanctum Authentication, HanyaAdmin & HanyaAdminWeb Middleware
│       ├── public/
│       │   └── assets/admin/        # Static SVG assets (balok-1.svg, balok-2.svg, logo-ven.svg, icon-*.svg)
│       ├── resources/
│       │   ├── css/app.css          # Tailwind CSS v4 & custom floating keyframes
│       │   └── views/admin/         # FRONTEND: Admin Blade Views (layouts, auth, accounts, posts, exhibitions, logs)
│       ├── routes/
│       │   ├── api.php              # Definisi route RESTful API (/api/admin/*)
│       │   └── web.php              # Definisi route Web Admin Panel (/admin/*)
│       ├── database/
│       │   └── migrations/          # Skema database relasional
│       └── storage/app/public/      # Media storage (gambar karya, foto profil, model 3D)
│
├── ven-mobile/                      # FRONTEND: Android Native (Kotlin + Jetpack Compose)
│   ├── app/
│   │   ├── build.gradle.kts         # Dependensi (Compose, Hilt, Retrofit, DataStore, Coil, dll.)
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       ├── java/com/ven/app/    # Source code aplikasi (Clean Architecture)
│   │       ├── res/                 # Resource standar (font Elms Sans, strings, themes)
│   │       └── res-icons/           # Folder aset ikon & logo khusus dari Figma (lihat §7)
│   ├── build.gradle.kts             # Root Gradle build script
│   ├── settings.gradle.kts
│   └── local.properties
│
├── unity/                           # (Opsional) Proyek Unity Engine untuk 3D Virtual Room
└── unity-export/                    # Export asset/library Unity untuk diintegrasikan ke Android
```

---

### 3.2 Struktur Lengkap `ven-mobile` (Kotlin + Jetpack Compose)

Arsitektur aplikasi Android mengikuti **Clean Architecture + MVVM/MVI** dengan pemisahan layer yang tegas:

```
ven-mobile/app/src/main/
├── AndroidManifest.xml
├── java/com/ven/app/
│   ├── di/                                # Dependency Injection (Hilt / Koin Modules)
│   │   ├── NetworkModule.kt               # Retrofit, OkHttpClient, Sanctum AuthInterceptor, JSON parser
│   │   ├── RepositoryModule.kt            # Bind Repository interfaces ke implementasi
│   │   ├── DatabaseModule.kt              # DataStore Preferences provider
│   │   └── UseCaseModule.kt               # Domain Use Cases provider
│   │
│   ├── data/                              # Data Layer: Komunikasi ke Backend Laravel & Local Storage
│   │   ├── remote/
│   │   │   ├── api/                       # Retrofit API Service (sinkron dengan routes/api.php Laravel)
│   │   │   │   ├── AuthApiService.kt      # POST /auth/login, /register, /logout
│   │   │   │   ├── ProfileApiService.kt   # GET/PUT /profile, /profile/email, /profile/password
│   │   │   │   ├── KaryaApiService.kt     # GET/POST/PUT/DELETE /karya (CRUD karya pengguna)
│   │   │   │   ├── PameranApiService.kt   # GET/POST/PUT/DELETE /pameran, /pameran/{id}/ruangan
│   │   │   │   ├── ObjekApiService.kt     # GET/POST /objek, PATCH /objek/{id}/posisi
│   │   │   │   ├── Model3DApiService.kt   # GET /model-3d, /model-3d/{id}
│   │   │   │   ├── UndanganApiService.kt  # GET/POST/PATCH /undangan (Schedule / Inbox request)
│   │   │   │   ├── AiChatApiService.kt    # POST /ai-chat (Asisten rekomendasi AI)
│   │   │   │   ├── RekomendasiApiService.kt # GET /rekomendasi (Feed discovery)
│   │   │   │   └── KategoriApiService.kt  # GET/PUT /pengguna/kategori-minat
│   │   │   ├── dto/                       # Data Transfer Objects (mapping JSON response & request)
│   │   │   │   ├── common/                # ApiResponse<T>, PaginatedResponse<T>, ErrorResponse
│   │   │   │   ├── auth/                  # LoginRequest, RegisterRequest, AuthResponse, UserDto
│   │   │   │   ├── karya/                 # KaryaDto, CreateKaryaRequest
│   │   │   │   ├── pameran/               # PameranDto, RuanganDto, ObjekDto, PosisiObjekDto
│   │   │   │   ├── undangan/              # UndanganDto, CreateUndanganRequest, UpdateUndanganRequest
│   │   │   │   └── aichat/                # AiChatRequest, AiChatResponse
│   │   │   └── interceptor/
│   │   │       ├── AuthInterceptor.kt     # Menambahkan header "Authorization: Bearer <token>"
│   │   │       └── ErrorInterceptor.kt    # Menangani error HTTP 401 (token kedaluwarsa), 422 (validasi)
│   │   ├── local/
│   │   │   ├── datastore/
│   │   │   │   ├── TokenManager.kt        # Simpan & baca token Sanctum secara aman
│   │   │   │   └── UserPreferences.kt     # Preferensi tema, bahasa, dsb.
│   │   │   └── preference/
│   │   │       └── ThemePreferences.kt    # StateFlow reaktif Dark/Light/System via SharedPreferences
│   │   └── repository/                    # Implementasi Repository (Single Source of Truth)
│   │       ├── AuthRepositoryImpl.kt
│   │       ├── ProfileRepositoryImpl.kt
│   │       ├── KaryaRepositoryImpl.kt
│   │       ├── PameranRepositoryImpl.kt
│   │       ├── UndanganRepositoryImpl.kt
│   │       ├── AiChatRepositoryImpl.kt
│   │       └── RekomendasiRepositoryImpl.kt
│   │
│   ├── domain/                            # Domain Layer: Pure Kotlin (independen dari framework & UI)
│   │   ├── model/                         # Entity Domain
│   │   │   ├── User.kt
│   │   │   ├── Karya.kt                   # Post karya seni / poster / 3D
│   │   │   ├── Pameran.kt                 # Virtual Exhibition
│   │   │   ├── Ruangan.kt / Objek3D.kt    # Tata letak ruangan & penempatan objek
│   │   │   ├── Undangan.kt                # Permintaan pertemuan / room meeting
│   │   │   ├── ChatMessage.kt             # Pesan percakapan asisten AI
│   │   │   └── Category.kt                # Kategori karya & minat
│   │   ├── repository/                    # Interface kontrak repository
│   │   │   ├── AuthRepository.kt
│   │   │   ├── ProfileRepository.kt
│   │   │   ├── KaryaRepository.kt
│   │   │   ├── PameranRepository.kt
│   │   │   ├── UndanganRepository.kt
│   │   │   ├── AiChatRepository.kt
│   │   │   └── RekomendasiRepository.kt
│   │   └── usecase/                       # Interactors / Business Logic
│   │       ├── auth/                      # LoginUseCase, RegisterUseCase, LogoutUseCase
│   │       ├── feed/                      # GetHomeFeedUseCase, GetComingSoonExhibitsUseCase
│   │       ├── karya/                     # CreateKaryaUseCase, GetKaryaDetailUseCase
│   │       ├── pameran/                   # CreateExhibitUseCase, SaveObjectPositionUseCase
│   │       ├── schedule/                  # GetApprovedEventsUseCase, GetWaitingEventsUseCase, HandleRequestUseCase
│   │       └── aichat/                    # SendChatMessageUseCase, GetChatHistoryUseCase
│   │
│   ├── ui/                                # UI / Presentation Layer (Jetpack Compose)
│   │   ├── theme/                         # Design Tokens (lihat §4)
│   │   │   ├── Color.kt                   # BrandPurple, DarkVexColors, LightVexColors, VexColors
│   │   │   ├── Type.kt                    # FontFamily Elms Sans + Typography M3
│   │   │   ├── Shape.kt                   # VexRadius + Shapes
│   │   │   ├── Dimens.kt                  # VexSpace, VexSize
│   │   │   ├── Motion.kt                  # Durasi & easing transisi
│   │   │   ├── CategoryColors.kt          # Token warna tag kategori (§4.3)
│   │   │   └── Theme.kt                   # VexTheme + CompositionLocal provider
│   │   ├── icons/
│   │   │   └── VexIcons.kt                # Akses ikon bertipe + VexIcon() (lihat §7)
│   │   ├── components/
│   │   │   ├── ui/                        # Primitif reusable (lihat §6.1)
│   │   │   │   ├── VexButton.kt
│   │   │   │   ├── VexTextField.kt
│   │   │   │   ├── OtpInput.kt
│   │   │   │   ├── VexChip.kt
│   │   │   │   ├── VexTabs.kt
│   │   │   │   ├── SearchBar.kt
│   │   │   │   ├── SegmentedPill.kt
│   │   │   │   ├── Skeleton.kt
│   │   │   │   ├── Avatar.kt
│   │   │   │   ├── VexBottomSheet.kt
│   │   │   │   ├── VexListRow.kt
│   │   │   │   ├── VexSectionLabel.kt
│   │   │   │   ├── VexRadioRow.kt
│   │   │   │   └── VexCheckRow.kt
│   │   │   ├── layout/                    # Kerangka layout (lihat §6.2)
│   │   │   │   ├── AppScaffold.kt
│   │   │   │   ├── VexTopBar.kt
│   │   │   │   ├── BottomNav.kt
│   │   │   │   ├── AuthLayout.kt
│   │   │   │   ├── VexBottomActionBar.kt
│   │   │   │   └── PageContainer.kt
│   │   │   └── feature/                   # Komponen lintas-layar (lihat §6.3)
│   │   │       ├── CarouselExhibitions.kt
│   │   │       ├── PostCard.kt
│   │   │       ├── EventBanner.kt
│   │   │       ├── EventListItem.kt
│   │   │       ├── HistoryListItem.kt
│   │   │       ├── RequestListItem.kt
│   │   │       ├── MediaGrid.kt
│   │   │       ├── ProfileHeader.kt
│   │   │       └── ChatBubble.kt
│   │   ├── screens/                       # Screen-level composables (Screen, ViewModel, UiState)
│   │   │   ├── auth/                      # LoginScreen, RegisterScreen, ForgotPasswordScreen
│   │   │   ├── onboarding/                # InterestScreen (pemilihan kategori minat)
│   │   │   ├── home/                      # HomeScreen (Coming soon carousel + feed)
│   │   │   ├── schedule/                  # ScheduleScreen, ScheduleUiState, ScheduleDummyData (Approved, Waiting, Request tabs + Detail Sheet)
│   │   │   ├── ai/                        # AiChatScreen (Chat view + History drawer)
│   │   │   ├── explore/                   # ExploreScreen, ExploreUiState, ExploreDummyData, PostsFeedScreen
│   │   │   │   ├── ExploreScreen.kt       # Discovery + Search History + Search Results + Filter Sheet
│   │   │   │   ├── PostsFeedScreen.kt     # Detail post scrollable (LazyColumn + PostCard)
│   │   │   │   ├── ExploreUiState.kt      # MVI state (tab, query, filter, hasil)
│   │   │   │   └── ExploreDummyData.kt    # Dummy data lokal (riwayat, discovery, hasil)
│   │   │   ├── profile/                   # ProfileScreen, ProfileUiState, ProfileDummyData (Own profile gra._.aph)
│   │   │   ├── settings/                  # Settings flow & sub-pages
│   │   │   │   ├── SettingsScreen.kt
│   │   │   │   ├── ProfileSettingsScreen.kt
│   │   │   │   ├── ChangeEmailScreen.kt
│   │   │   │   ├── VerifyEmailScreen.kt
│   │   │   │   ├── ChangePasswordScreen.kt
│   │   │   │   ├── ThemeScreen.kt
│   │   │   │   ├── LanguageScreen.kt
│   │   │   │   ├── AccountStatusScreen.kt
│   │   │   │   ├── HistoryScreen.kt
│   │   │   │   ├── SettingsPlaceholderScreen.kt
│   │   │   │   ├── SettingsUiState.kt
│   │   │   │   └── SettingsDummyData.kt
│   │   │   └── create/
│   │   │       ├── post/                  # CreatePostScreen (Media picker, metadata form)
│   │   │       └── exhibit/               # CreateExhibitScreen (Template picker, 3D Canvas Editor)
│   │   └── navigation/
│   │       ├── Screen.kt                  # Definisi route & argumen navigasi (sealed class)
│   │       ├── NavGraph.kt                # NavHost root (AuthGraph, MainGraph, CreateGraph, SettingsGraph)
│   │       └── BottomNavTab.kt            # 5 item navigasi tab bar
│   │
│   ├── bridge3d/                          # Bridge interop untuk 3D Viewer & Exhibit Editor
│   │   └── UnityPlayerView.kt             # Composable wrapper untuk render scene 3D ruang pameran
│   │
│   └── util/                              # Utility & Helper
│       ├── Resource.kt                    # Sealed class status: Success, Error, Loading
│       ├── UiState.kt                     # State wrapper untuk Composable Screen
│       ├── DateFormatter.kt               # Parser & formatter tanggal sesuai spesifikasi UI
│       └── Constants.kt                   # Base URL backend, timeouts, dsb.
│
├── res/                                   # Resource Standar Android
│   ├── font/                              # elms_sans_*.ttf (400, 500, 600, 700)
│   ├── values/                            # strings.xml, themes.xml (Splash theme)
│   ├── values-in/                         # strings.xml (Bahasa Indonesia)
│   └── drawable-nodpi/                    # poster_*.webp (gambar dummy yang telah dioptimasi)
└── res-icons/                             # FOLDER KHUSUS ikon & logo dari Figma (lihat §7)
    └── drawable/                          # ic_nav_*.xml, ic_action_*.xml, logo_*.xml
```

---

### 3.3 Integrasi Mobile (Kotlin) ke Backend (Laravel)

1. **Komunikasi REST API:**
   - Seluruh data dikirim dan diterima dalam format JSON standar melalui Retrofit 2 + OkHttp.
   - Base URL development:
     - Android Emulator: `http://10.0.2.2:8000/api/`
     - Perangkat Fisik (LAN): `http://<IP-Host-Komputer>:8000/api/`
     - Staging/Production: `https://api.ven.app/api/`
2. **Autentikasi (Laravel Sanctum):**
   - Autentikasi menggunakan Personal Access Token (`auth:sanctum`).
   - Token disimpan di sisi Android menggunakan Jetpack **DataStore Preferences** (`TokenManager`).
   - `AuthInterceptor` secara otomatis menyematkan header `Authorization: Bearer <token>` pada setiap HTTP request yang membutuhkan otentikasi.
   - Response `401 Unauthorized` memicu reset token di DataStore dan navigasi otomatis ke `LoginScreen`.
3. **Upload Media (Multipart):**
   - Unggah gambar karya (pada layar *Create Post* §8.13) dan foto profil menggunakan format `multipart/form-data`.
   - File disimpan di Laravel storage (`storage/app/public/...`) dan diakses oleh Android melalui library pemuat gambar **Coil**.
4. **AI Assistant Integration:**
   - Android mengirim prompt pertanyaan via `POST /api/ai-chat`.
   - Backend memproses logika asisten/rekomendasi dan mengembalikan response teks + ID karya terkait untuk ditampilkan di UI percakapan Compose (§8.12).

---

### 3.4 Library & Tech Stack

| Kategori | Library / Tool | Keterangan |
|---|---|---|
| **Language & Platform** | Kotlin 2.x, Android SDK (minSdk 24, targetSdk 35) | Native Android |
| **UI Framework** | Jetpack Compose (BOM), Material 3 | Declarative UI, fondasi token |
| **Architecture** | Clean Architecture + MVVM/MVI | Unidirectional Data Flow, StateFlow |
| **Dependency Injection** | Hilt (Dagger) / Koin | Pengelolaan dependensi antar layer |
| **Networking** | Retrofit 2, OkHttp 4 | HTTP Client, Logging & Auth Interceptors |
| **Serialization** | Kotlinx Serialization / Moshi | JSON parser DTO |
| **Image Loading** | Coil Compose | Asynchronous image loading & memory caching |
| **Local Persistence** | Jetpack DataStore Preferences | Penyimpanan token Sanctum & preferensi tema |
| **Navigation** | Jetpack Navigation Compose | Routing antar screen & nested graph |
| **3D Engine** | Unity as a Library (UaaL) / Filament | Engine render & editor ruang 3D (§8.14) |
| **Async & Concurrency** | Kotlin Coroutines & Flow | Asynchronous programming |

---

## 4. Design Tokens

### 4.1 Font
- **Elms Sans** `[OBSERVED]`. Taruh file TTF di `res/font/` (cek lisensi; pakai file statis 400/500/600/700 atau variable font).
- Weight dipakai: 400 (body), 500 (label/tombol), 600 (judul bar), 700 (judul halaman/auth).
- Fallback otomatis: `FontFamily.SansSerif`.

### 4.2 Warna: `Color.kt`

Material `ColorScheme` tidak punya semua peran yang dibutuhkan, jadi buat `VexColors` sendiri dan sediakan lewat `CompositionLocal`. Isi `ColorScheme` M3 dipetakan dari `VexColors` (lihat 4.6).

```kotlin
@Immutable
data class VexColors(
    val bg: Color, val surface: Color, val navBar: Color, val surfaceInput: Color,
    val border: Color, val borderInput: Color,
    val text: Color, val textSecondary: Color, val textMuted: Color, val textDisabled: Color,
    val primary: Color, val onPrimary: Color, val primaryText: Color, val primaryDisabled: Color,
    val danger: Color, val onDanger: Color, val success: Color,
    val inverseBg: Color, val inverseText: Color, val scrim: Color,
)

val BrandPurple = Color(0xFFBA18F5)          // OBSERVED

val DarkVexColors = VexColors(               // DEFAULT
    bg            = Color(0xFF1F1F1F),       // OBSERVED (Figma 1F1F1F)
    navBar        = Color(0xFF252525),       // OBSERVED & DIKONFIRMASI (Figma 252525): latar bottom nav
    surface       = Color(0xFF252525),       // ASSUMPTION: sheet, tombol melayang, toggle (sementara sama dengan navBar)
    surfaceInput  = Color.White.copy(0.10f), // OBSERVED (Figma FFFFFF 10%): input, search, chip netral, bubble
    border        = Color.White.copy(0.10f), // OBSERVED: divider & garis app bar
    borderInput   = Color.White.copy(0.20f), // ASSUMPTION: outline input berbingkai
    text          = Color.White,             // OBSERVED
    textSecondary = Color.White.copy(0.60f), // ASSUMPTION: meta penting (tanggal, "optional")
    textMuted     = Color.White.copy(0.40f), // OBSERVED (Figma FFFFFF 40%): placeholder, tab nonaktif
    textDisabled  = Color.White.copy(0.25f), // ASSUMPTION
    primary       = BrandPurple,
    onPrimary     = Color.White,
    primaryText   = Color(0xFFD257FF),       // ASSUMPTION: ungu untuk TEKS di atas gelap (kontras)
    primaryDisabled = BrandPurple.copy(0.28f),
    danger        = Color(0xFFFF0000),       // OBSERVED ≈ (Reject, Logout)
    onDanger      = Color.White,
    success       = Color(0xFF00E85C),       // OBSERVED ≈ (centang "title & description")
    inverseBg     = Color.White,             // tombol "hitam" di light → putih di dark
    inverseText   = Color(0xFF1F1F1F),
    scrim         = Color.Black.copy(0.60f),
)

val LightVexColors = VexColors(              // OBSERVED dari wireframe light
    bg = Color.White, surface = Color.White, navBar = Color.White,
    surfaceInput = Color(0xFFE5E5E5), border = Color(0xFFDADADA), borderInput = Color(0xFFBDBDBD),
    text = Color.Black, textSecondary = Color(0xFF5C5C63), textMuted = Color(0xFF8A8A92),
    textDisabled = Color(0xFFB5B5BD),
    primary = BrandPurple, onPrimary = Color.White, primaryText = BrandPurple,
    primaryDisabled = BrandPurple.copy(0.25f),
    danger = Color(0xFFFF0000), onDanger = Color.White, success = Color(0xFF00A843),
    inverseBg = Color.Black, inverseText = Color.White, scrim = Color.Black.copy(0.50f),
)
```

> ⚠️ **Kontras (hasil hitung kasar):**
> - Ungu `#BA18F5` untuk **teks kecil di atas `#1F1F1F` ≈ 3.6:1** (di bawah AA 4.5). Karena itu link/aksi teks ungu di dark ("Post", "Next", "Resend", "Maybe later") memakai `primaryText`. Wireframe memakai ungu brand langsung; ini sengaja menyimpang sedikit dan **sudah disetujui pemilik desain**.
> - Teks putih di atas tombol `#BA18F5` ≈ 4.6:1 (lolos).
> - `textMuted` (putih 40%) di atas `#1F1F1F` ≈ 3.8:1. Cukup untuk placeholder/tab nonaktif, **bukan** untuk informasi penting. Pakai `textSecondary` untuk itu.

### 4.3 Warna kategori (tag): `[PLACEHOLDER]`
Warna aksen belum ditentukan. Empat warna dari wireframe dipertahankan, sisanya sementara. Simpan di **satu file** (`ui/theme/CategoryColors.kt`) supaya mudah diganti.

| Kategori | Warna | Status |
|---|---|---|
| 3D | `#FF9A00` | OBSERVED |
| 2D | `#FF1F00` | OBSERVED |
| Animation | `#7ABF00` | OBSERVED |
| Game Dev | `#2A0FD6` | OBSERVED |
| Cybersecurity | `#0E7C66` | PLACEHOLDER |
| Software | `#1565C0` | PLACEHOLDER |
| UI/UX | `#D81B8C` | PLACEHOLDER |
| Videography | `#00838F` | PLACEHOLDER |
| Photography | `#6D4C41` | PLACEHOLDER |
| Internet Of Things | `#546E7A` | PLACEHOLDER |
| Automation System | `#7B5E00` | PLACEHOLDER |
| Fabrication | `#9E4A00` | PLACEHOLDER |
| Manufacturing | `#37474F` | PLACEHOLDER |
| Others | `#616161` | PLACEHOLDER |

Teks di tag: putih, **kecuali** latar terang (3D oranye, Animation hijau muda) yang kontras putihnya rendah (±2:1). Buat fungsi `onCategoryColor(bg)` yang memilih hitam/putih berdasar luminans, jangan hardcode.
Tag keahlian pada halaman Profil (§8.7) menggunakan pemetaan warna `CategoryColorMap` dan kalkulasi warna teks kontras `onCategoryColor()`. Dua keahlian tambahan yang mengisi indikator `+2` (`UI/UX`, `Software`) berstatus `[PLACEHOLDER]`.


### 4.4 Dimensi, radius, tipografi, motion

```kotlin
object VexSpace {   // basis 4dp
    val s1 = 4.dp;  val s2 = 8.dp;  val s3 = 12.dp; val s4 = 16.dp; val s5 = 20.dp
    val s6 = 24.dp; val s8 = 32.dp; val s10 = 40.dp; val s12 = 48.dp; val s16 = 64.dp
}
object VexSize {
    val minTouch = 48.dp          // standar Android (bukan 44)
    val control = 48.dp           // tinggi tombol & input pill
    val search = 46.dp
    val topBar = 56.dp
    val bottomNav = 64.dp         // + inset navigation bar
    val iconSm = 20.dp; val icon = 24.dp; val iconLg = 28.dp
    val avatarSm = 24.dp; val avatarMd = 40.dp; val avatarLg = 96.dp
    val fab = 48.dp               // tombol bulat melayang (editor exhibit)
    val drawerWidth = 300.dp      // OBSERVED ≈ 301dp (73% layar)
    val contentMaxWidth = 480.dp  // pusatkan konten jika layar lebih lebar
}
object VexRadius {
    val xs = 4.dp      // thumbnail di list
    val md = 8.dp      // input berbingkai, kotak OTP, kartu template
    val lg = 16.dp     // bubble chat
    val xl = 28.dp     // sisi atas bottom sheet, sisi bawah canvas editor
    val full = 100.dp  // pill: tombol, chip, search, input auth, avatar
}
```

**Tipografi** (isi `Typography` M3; wireframe ≈ nilai ini):

| Slot M3 | Ukuran/Weight | Dipakai di |
|---|---|---|
| `displayMedium` | 32sp / 700 | Judul Login, Register, Interest |
| `titleLarge` | 20sp / 500–600 | Judul top bar, "Coming Soon", tanggal di list |
| `titleMedium` | 18sp / 500 | Nama di profil, nama di request |
| `titleSmall` | 16sp / 500 | Label field di atas input |
| `bodyLarge` | 16sp / 400 | Teks utama, nilai input |
| `bodyMedium` | 14sp / 400 | Label seksi, meta, link kecil |
| `bodySmall` | 12sp / 400 | Helper text |
| `labelLarge` | 16sp / 500 | Teks tombol |
| (khusus) `chatBody` | 18sp / 400, line-height 1.4 | Isi pesan di AI Chat `[ASSUMPTION ukuran]` |

Line-height default ≈ 1.4–1.5× ukuran. Pengguna bisa membesarkan font sistem: layout **tidak boleh bergantung pada tinggi teks tetap**.

**Motion** (`Motion.kt`): `durFast = 150`, `durBase = 250`, `durSheet = 320` (ms); easing `CubicBezierEasing(0.2f, 0f, 0f, 1f)`. Jika `ANIMATOR_DURATION_SCALE == 0` (reduced motion), semua transisi dilewati/instan.

### 4.5 Z-order
Konten < TopBar < BottomNav < scrim < Sheet/Drawer < Toast (Compose mengurus lewat struktur; pakai `Scaffold` dan komponen modal M3, jangan `zIndex` manual kecuali perlu).

### 4.6 `Theme.kt` (acuan)

```kotlin
enum class ThemeMode { Dark, Light, System }   // default = Dark

@Composable
fun VexTheme(mode: ThemeMode = ThemeMode.Dark, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.Dark -> true
        ThemeMode.Light -> false
        ThemeMode.System -> isSystemInDarkTheme()
    }
    val vex = if (dark) DarkVexColors else LightVexColors
    val scheme = (if (dark) darkColorScheme() else lightColorScheme()).copy(
        primary = vex.primary, onPrimary = vex.onPrimary,
        background = vex.bg, onBackground = vex.text,
        surface = vex.surface, onSurface = vex.text,
        outline = vex.border, error = vex.danger, onError = vex.onDanger,
        scrim = vex.scrim,
    )
    CompositionLocalProvider(LocalVexColors provides vex) {
        MaterialTheme(colorScheme = scheme, typography = VexTypography, shapes = VexShapes, content = content)
    }
}

object VexTheme {
    val colors: VexColors @Composable get() = LocalVexColors.current
}
```

### 4.7 Perilaku tema
- **Default dark.** *Settings → Theme*: Dark (default) / Light / System `[ASSUMPTION]`. Simpan di DataStore; baca **sebelum** UI pertama tampil (splash screen API dengan background `#1F1F1F` agar tidak berkedip putih).
- Status bar & navigation bar: edge-to-edge (`enableEdgeToEdge`), ikon terang di dark, gelap di light.
- Gambar karya pengguna **tidak** difilter di dark mode.
- **Logo VEX:** putih di dark, ungu di light `[OBSERVED]`. Satu vector, diwarnai lewat tint dari token (`text` di dark, `primary` di light).

---

## 5. Pola Layout Global

### 5.1 Jenis kerangka halaman

| Kerangka | Dipakai di | Top bar | Bottom nav |
|---|---|---|---|
| **AuthLayout** | Login, Register, Interest | tidak ada | tidak ada |
| **AppScaffold (utama)** | Home, Schedule, AI, Explore, Profile | bervariasi | **ada, tetap diam** |
| **SubPage** | Settings & sub-setting, Forgot Password, Change Email/Password | back + judul | tidak ada |
| **CreateFlow (modal penuh)** | New Post, New Exhibit, editor Exhibit | X/back + judul + aksi teks | **tidak ada** (diganti toggle Post/Exhibit di 2 layar awal) |

### 5.2 Scroll, inset, keyboard
- Layar utama: `Scaffold(bottomBar = { BottomNav(...) })`. **Konten di-scroll (`LazyColumn`/`LazyVerticalGrid`), bottom nav tetap diam.** Pakai `innerPadding` dari Scaffold sebagai `contentPadding` agar item terakhir tidak tertutup.
- Edge-to-edge: hormati `WindowInsets.statusBars` (TopBar) dan `navigationBars` (BottomNav: tinggi `VexSize.bottomNav` + inset).
- Form dengan tombol CTA di bawah ("Send OTP", "Publish", "Save"): CTA di slot bawah, `Modifier.navigationBarsPadding().imePadding()` supaya naik di atas keyboard. Margin horizontal 20dp `[OBSERVED]`.
- **Sticky Bottom CTA Bar (layar SubPage / CreateFlow tanpa bottom nav):** CTA **bukan** bagian dari konten scroll. Dipasang lewat `Scaffold(bottomBar = { VexBottomActionBar(...) })` sehingga tetap diam saat konten di-scroll (perilaku persis seperti `BottomNav`).
  - Konten memakai `innerPadding` dari Scaffold sebagai `contentPadding` agar elemen terbawah form tidak tertutup bar.
  - Bar: lebar penuh, latar `navBar` (`#252525`) + garis atas 1dp `border` (`#313131`); padding horizontal 20dp, atas 12dp, bawah 24dp + `navigationBarsPadding()`; `imePadding()` supaya naik secara mulus di atas keyboard.
  - Berbeda dari `BottomNav`: tidak ada tab/ikon navigasi, melainkan satu slot aksi (biasanya tombol Primary tunggal). Hanya muncul pada layar tanpa `BottomNav`.
  - Dipakai di: §8.6.1 Request Meeting (dan menjadi acuan standardisasi untuk §8.9, §8.10, §8.11, §8.13, §8.14).
- Top bar: tinggi 56dp + status bar inset, garis bawah 1dp `border` (terlihat di Profile, Settings, Create). Di Home/Explore boleh ikut scroll `[TODO]`.
- Bottom nav: latar `navBar` (dark `#252525` `[OBSERVED]`), tinggi 64dp + inset navigation bar. Di dark **tanpa** garis atas; di light tambah garis atas 1dp `border` `[ASSUMPTION]`.

### 5.3 Margin & jarak
- Padding horizontal halaman standar: **24dp** (`s6`) untuk list/settings/profil `[OBSERVED ≈ 22–26]`.
- Form auth: konten ±52dp kiri-kanan di frame 412 → `padding(horizontal = s12)`, pusatkan, `widthIn(max = 304.dp)`.
- Jarak antar field: `s4`; antar kelompok: `s8`.
- Grid media: 3 kolom, celah **2dp** `[OBSERVED]`.
- Padding bawah halaman yang punya toggle melayang (Create): tinggi toggle + 16dp.

---

## 6. Komponen

### 6.1 `ui/components/ui/`

| Composable | Varian / State | Catatan |
|---|---|---|
| **VexButton** | `Primary`, `Inverse` (hitam di light, putih di dark), `Outline` (Google), `Danger`, `TextLink`. State: enabled, pressed, loading, disabled | Tinggi 48dp, `RoundedCornerShape(full)`, teks `labelLarge` rata tengah. Outline: latar putih (`#FFFFFF`) dengan border hitam 1dp (`#000000`) & teks hitam di kedua mode. Disabled Primary = `primaryDisabled` + teks pucat (lihat "Join Room" status Waiting). Lebar penuh di auth, form, sheet |
| **VexIconButton** | default, pressed | Area sentuh min 48dp walau ikon 24dp |
| **VexTextField** | `Pill` (auth: tanpa border, fill solid `#FFFFFF` pada Dark Auth screen `[OBSERVED 3946:17]` dengan teks `#000000` & placeholder abu-abu; atau fill `surfaceInput` pada screen umum), `Boxed` (settings & create: radius 8dp, fill `surfaceInput`, outline `borderInput`). State: default, focus, error, disabled | Label di atas untuk `Boxed`; placeholder di dalam untuk `Pill`. Fokus: outline 2dp `primary`. `KeyboardOptions` sesuai tipe (email, password, number), `imeAction` Next/Done. Autofill via `semantics { contentType = … }` |
| **VexPasswordField** | turunan | Tombol tampil/sembunyikan `[ASSUMPTION]` |
| **VexTextArea** | `Boxed`, multiline | Tinggi min ≈ 190dp (Description) |
| **VexDropdown** | `Boxed` + ikon chevron-bawah | `ExposedDropdownMenu`; opsi = daftar kategori |
| **OtpInput** | 5 kotak, radius 8dp, outline | **5 digit** `[OBSERVED]`; auto-pindah, dukung paste, `KeyboardType.Number`, kotak error berwarna `danger` |
| **SearchBar** | default (Discovery), fokus (Search Active), dengan tombol filter, dengan leading back | Pill (`VexRadius.full`), tinggi 44–46dp, fill `surfaceInput`, leading search icon `ic_action_search`/`ic_nav_explore_outline` 20dp warna `textMuted`, placeholder "Search" `textMuted`, teks input `text`. Varian **Search Active** (Explore §8.5.2): leading berubah jadi panah kembali `ic_action_back` + trailing tombol hapus "✕" (`ic_action_close`) saat query tidak kosong |
| **VexTabs** | `Text` (`VexTextTabs`: Approved/Waiting/Request; dan Explore: For you/Accounts/Posts/Exhibits), `Icon` (`VexIconTabs`: foto / kubus 3D) `[TERIMPLEMENTASI]` | Aktif: teks/ikon `text` (ikon terisi / teks tebal) + garis bawah 1dp putih selebar tab; nonaktif: `textMuted`. Bangun sendiri tanpa M3 TabRow default. Varian 4 tab Explore bisa **scrollable** bila lebar tidak cukup |
| **VexChip** | `Selectable`, `Tag`, `Counter` ("+2"), `Skill` (`VexSkillChip`) | Pill. Selectable default: fill `surfaceInput`; **selected: fill `primary`, teks putih** `[ASSUMPTION]`. Tag: fill warna kategori (§4.3). Skill: clickable berlatar warna kategori |
| **Avatar** | `sm` 24, `md` 40, `lg` 96, bulat `[TERIMPLEMENTASI]` | `ContentScale.Crop`, fallback inisial |
| **VexBottomSheet** | konten bebas `[TERIMPLEMENTASI]` | Bungkus `ModalBottomSheet`: radius atas 28dp, handle abu 40×4dp, container `surface` (dark) / putih (light), scrim `scrim` |
| **VexDrawer** | – | Bungkus `ModalNavigationDrawer`, lebar 300dp, container `bg` |
| **VexListRow** | ikon + label + chevron; varian `Danger` `[TERIMPLEMENTASI]` | Baris item menu Settings. Tinggi min 56dp, ikon 24dp, jarak ikon-label 16dp, chevron kanan (Normal), merah danger tanpa chevron (Logout) |
| **VexSectionLabel** | label kategori seksi `[TERIMPLEMENTASI]` | `bodyMedium` Medium 500 warna `textMuted`, padding horizontal 24dp, atas 12dp, bawah 8dp |
| **VexRadioRow** | label (+ deskripsi) + radio bulatan `[TERIMPLEMENTASI]` | Pilihan tunggal Theme & Account Status. Lingkaran luar 22dp, dot aktif 12dp warna `primary`, deskripsi `bodySmall` warna `textMuted` |
| **VexCheckRow** | label + tanda centang kanan `[TERIMPLEMENTASI]` | Pilihan bahasa (Language). Tanda centang `ic_action_check` 20dp warna `text` bila terpilih |
| **FloatingToolButton** | default, selected | Lingkaran 48dp, fill `surface`, ikon putih (editor exhibit) |
| **VexIcon** | tint, ukuran | Pembungkus `Icon` untuk drawable dari `VexIcons` (§7). Satu-satunya cara menampilkan ikon |
| **SegmentedPill** | 2 opsi (Post / Exhibit) | Pill melayang, fill `bg`/`surface` + border `border`; terpilih: teks `text`, tidak: `textMuted` |
| **Skeleton** | `Rect`, `Circle`, `Text` | Shimmer halus; dipakai sebagai loading (tanpa spinner layar penuh) |
| **EmptyState / ErrorState** | ikon + teks + aksi | `[TODO copy per halaman]` |
| **Toast/Snackbar** | success, error | `[TODO]` |

### 6.2 `ui/components/layout/`

| Composable | Isi |
|---|---|
| **AppScaffold** | `Scaffold` + `BottomNav` + penanganan inset |
| **BottomNav** | 5 tab (§7), dibuat **custom** (bukan `NavigationBar` M3), container warna `navBar` |
| **VexBottomActionBar** | Slot CTA tetap di bawah (layar tanpa BottomNav): container lebar penuh, latar `navBar` (`#252525`), garis atas 1dp `border`, padding horizontal 20dp, atas 12dp, bawah 24dp + `navigationBarsPadding()` + `imePadding()`. Anak utama = `VexButton`. Dipakai di §8.6.1 |
| **VexTopBar** | varian: `Home` (+ kiri, logo VEX kanan), `Profile` (+ kiri `ic_action_add`, username tengah, ikon menu kanan `ic_action_menu` -> Settings), `Ai` (ikon menu/drawer kiri, logo kanan, tanpa garis bawah), `Back` (panah + judul + opsional `trailingContent` aksi teks/ikon kanan), `Create` (X/panah + judul kiri, aksi teks kanan), `Search` (SearchBar penuh) |
| **AuthLayout** | judul besar tengah + slot form + blok bawah (teks + tombol) menempel di bawah |
| **PageContainer** | padding horizontal + `widthIn(max = contentMaxWidth)` |

### 6.3 `ui/components/feature/` (lintas screen)

| Composable | Dipakai di | Anatomi |
|---|---|---|
| **CarouselExhibitions** | Home (Coming Soon & carousel pameran lainnya) | Judul seksi (`titleLarge` SemiBold) + `LazyRow` dengan snap behavior (`SnapPosition.Start`), padding 24dp, gap item 12dp, garis pembatas 1dp `#313131` |
| **EventBanner** | Home (carousel "Coming Soon"), sheet | Gambar landscape radius 8–12dp; `LazyRow` + snap |
| **EventListItem** | Schedule (Approved/Waiting) | Thumbnail kiri lebar ~140dp rasio 16:9 (radius 8–12dp); kanan: tanggal bold (`titleSmall`), jam (`bodyMedium`), "See details" (kanan-bawah, `bodySmall`, `textMuted`). Tap item membuka `EventDetailSheet` (§8.4) |
| **HistoryListItem** | Settings / History `[TERIMPLEMENTASI]` | Thumbnail banner 150dp rasio 16:9 (radius 4dp); kanan: tanggal bold (`titleMedium`), jam (`bodyMedium`), "See details" kanan bawah (`bodySmall`, `textMuted`). Klik menampilkan `VexBottomSheet` detail status request |
| **RequestListItem** | Schedule/Request | Avatar lingkaran 48dp kiri; kanan: username bold + tanggal & jam (`27/9/26 02.00 PM`) abu-abu kecil kanan atas, cuplikan pesan 1–2 baris ellipsis. Tap membuka `RequestDetailSheet` (§8.4) |
| **EventDetailSheet** | Schedule (Approved & Waiting) | Isi `VexBottomSheet`: banner besar 16:9, teks status dengan tanggal/jam tebal via `AnnotatedString`, tombol "Join Room" (Primary aktif untuk Approved, disabled untuk Waiting) (§8.4) |
| **RequestDetailSheet** | Schedule (Request / Inbox) | Isi `VexBottomSheet`: banner 16:9, label "Requested for {tanggal jam}", baris pemohon (avatar + username), teks pesan lengkap, dua tombol sejajar Approve (Primary) & Reject (Danger) (§8.4) |
| **PostCard** | Home | Header `@username` kiri + ikon kalender & play kanan; gambar penuh lebar tanpa radius |
| **MediaGrid** | Explore, Profile, picker media `[TERIMPLEMENTASI]` | Grid default 3 kolom, celah 2dp; dukung tile 2-kolom (`GridItemSpan(2)`), rasio poster `VexAspect.poster`. Parameter `columns` fleksibel: 3 kolom untuk Explore Discovery, **2 kolom** untuk Search Results tab `Posts` & seksi `Posts` tab `For you` (§8.5.3) |
| **ExploreTopBar** | Explore (§8.5) `[BARU]` | Baris horizontal berisi `SearchBar` (weight 1f) + tombol filter (`ic_action_filter`, 24dp, `text`). Mode Search Active: leading `ic_action_back` + `SearchBar` mengisi sisa lebar. Padding horizontal 24dp |
| **SearchHistoryItem** | Explore Search History (§8.5.2) `[BARU]` | Dua varian: **Query** = lingkaran 36dp `surfaceInput` + ikon jam `ic_action_history` + teks query + tombol hapus "✕"; **Account** = `Avatar(sm)` + `@username` SemiBold + nama lengkap `textMuted` + tombol hapus "✕" |
| **SearchAccountRow** | Explore Search Results (§8.5.3) `[BARU]` | Baris hasil akun: `Avatar(md)` 40dp + `@username` SemiBold `text` + nama lengkap/peran `textMuted`. Tap membuka profil akun |
| **ExhibitBannerCard** | Explore tab Exhibits (§8.5.3) `[BARU]` | Banner pameran landscape ~16:9, radius 8–12dp (`VexRadius.md`), selebar layar dengan padding horizontal 16dp. Tap membuka 3D Virtual Exhibition Room |
| **CategoryFilterSheet** | Explore (§8.5.4) `[BARU]` | Isi `VexBottomSheet`: header "Category" (kiri, 18sp SemiBold `text`) + aksi teks "Reset" (kanan, `primary`); body `FlowRow` 14 chip kategori (`VexChip` selectable). Unselected `surfaceInput`, selected `primary` teks putih |
| **ProfileHeader / StatBlock** | Profile `[TERIMPLEMENTASI]` | Avatar 96dp, nama (maks 2 baris), statistik (angka tebal + label muted), baris chip skill clickable + sheet, bio, email, phone |

| **AiChatTopBar** | AI Assistant (§8.12.1) `[BARU]` | Header khusus AI: tombol drawer menu (`ic_action_menu`, 24dp) di kiri + logo VEX (`VexIcons.LogoVex`) di kanan, transparan tanpa divider bawah |
| **AiChatEmptyState** | AI Assistant (§8.12.2) `[BARU]` | Tampilan layar awal: watermark sparkle besar bintang 4 sudut (putih transparan ±8%) di tengah-atas, kolom input pill di tengah layar vertikal, teks label "AI assistant" di bawah pill |
| **ChatBubbleUser** | AI Assistant (§8.12.3) `[BARU]` | Balon pesan pengguna rata kanan, lebar maks 87% layar, fill `surfaceInput` (`#363636`), radius sudut 16dp kecuali sudut kanan-atas ~4dp (speech tail effect), tipografi `chatBody` (18sp Regular 400) |
| **ChatMessageAi** | AI Assistant (§8.12.3) `[BARU]` | Respons pesan asisten AI tanpa wadah balon/kartu, teks langsung di atas kanvas latar (`#1F1F1F`), lebar penuh dengan padding horizontal 24dp, `TextAlign.Justify`, format daftar bernomor 1–10 interaktif |
| **AiChatInput** | AI Assistant (§8.12.4) `[BARU]` | Kolom input pill tinggi 56–60dp, fill `surfaceInput` (`#363636`), placeholder "Type Something" (`textMuted`), tombol kirim panah muncul dinamis saat input terisi, adaptif keyboard via `imePadding()` |
| **ChatHistoryDrawer** | AI Assistant (§8.12.5) `[BARU]` | Drawer modal navigasi dari sisi kiri selebar 300dp, latar `bg` (`#1F1F1F`), baris pemicu "New chat" (`ic_action_new_chat`), label seksi "Recents", daftar riwayat percakapan (`ChatHistoryItem`) |
| **ChatHistoryItem** | AI Assistant Drawer (§8.12.5) `[BARU]` | Baris riwayat percakapan teks 1 baris dengan pemotongan ellipsis, tinggi sentuh minimal 48dp, warna putih (`text`), tap untuk membuka sesi percakapan lampau |

---

## 7. Navigasi

### Bottom Nav (5 tab, kiri → kanan) `[OBSERVED]`

| # | Tab | Ikon | Halaman |
|---|---|---|---|
| 1 | Home | rumah | Home |
| 2 | Schedule | kalender bertitik | Schedule |
| 3 | **AI Assistant** | bintang berkilau 4 sudut (sparkle) | AI Chat |
| 4 | Explore | kaca pembesar | Explore |
| 5 | Profile | foto avatar pengguna, bulat 28dp | Profile |

- **Aktif = ikon terisi (filled)**, nonaktif = outline. Tanpa label teks, tanpa pill indikator. Warna ikon selalu `text`.
- Wajib `contentDescription`, `Role.Tab`, dan state `selected` pada semantics.
- Navigasi antar tab: `popUpTo(startDestination) { saveState = true }`, `launchSingleTop`, `restoreState = true`.

### Alur lain
- Ikon **"+"** (kiri atas Home & Profile) → **CreateFlow** (New Post / New Exhibit).
- Profile → ikon menu kanan atas → Settings.
- AI → ikon menu kiri atas → Drawer riwayat chat.
- **Explore** (tab 4): Discovery grid → tap tile → **PostsFeed** (§8.5.5, scrollable `LazyColumn`); tap SearchBar → Search History; tap filter → `CategoryFilterSheet`; Search Results punya 4 tab (`For you`, `Accounts`, `Posts`, `Exhibits`).
- Settings → sub-halaman (SubPage, tanpa bottom nav).
- Route disarankan: grafik terpisah `auth`, `main` (5 tab), `settings`, `create` `[ASSUMPTION]`. Route baru Explore: `explore` (tab) dan `posts_feed?postId={postId}` (§8.5.5).

### Ikon & Logo: aset Figma milik sendiri `[DIPUTUSKAN]`
Ikon dan logo diekspor dari Figma pemilik desain, bukan dari library. Keuntungannya: lisensi aman (karya sendiri), 100% sama dengan wireframe (termasuk pasangan outline + fill), dan tanpa dependency tambahan.

**Lokasi:** `app/src/main/res-icons/drawable/` (folder khusus). Android tidak mengizinkan subfolder di dalam `res/drawable/`, jadi folder terpisah dibuat lewat `srcDirs`:

```kotlin
// app/build.gradle.kts
android {
    sourceSets {
        getByName("main") {
            res.srcDirs("src/main/res", "src/main/res-icons")
        }
    }
}
```
Folder tambahan wajib tetap berisi subfolder tipe resource (`drawable/`), dan nama file harus unik di seluruh resource. Jika kelak ikon ingin dibagi antar-modul, folder ini bisa dipindah ke modul `:icons` tanpa mengubah kode pemakai.

**Penamaan:** `ic_<grup>_<nama>[_outline|_filled]`, huruf kecil + underscore. Logo: `logo_<nama>`.

| Grup | File (varian) | Dipakai di |
|---|---|---|
| `nav` | `ic_nav_home`, `ic_nav_schedule`, `ic_nav_ai`, `ic_nav_explore` (masing-masing `_outline` + `_filled`); `ic_nav_profile_*` hanya fallback bila belum ada foto | BottomNav (aktif = `_filled`). Ikon AI besar di state kosong chat memakai `ic_nav_ai_filled` |
| `tab` | `ic_tab_posts`, `ic_tab_exhibits` (`_outline` + `_filled`) | Tab ikon di Profile (foto & kubus) |
| `action` | `ic_action_add`, `_menu`, `_back`, `_close`, `_chevron_left`, `_chevron_right`, `_chevron_down`, `_search`, `_filter`, `_history`, `_calendar`, `_play`, `_new_chat`, `_visibility`, `_visibility_off`, `_edit`, `_check` | TopBar, SearchBar, PostCard, dropdown, field password, navigasi bulan kalender, mode edit profile (`_edit`), opsi bahasa (`_check`), tombol filter & riwayat pencarian Explore (`_filter`, `_history`) |
| `settings` | `ic_settings_profile`, `_email`, `_password`, `_account_status`, `_history`, `_theme`, `_language`, `_privacy`, `_help`, `_logout` | Halaman Settings |
| `editor` | `ic_editor_text` (Aa), `ic_editor_panel`, `ic_editor_furniture` | Tool editor exhibit (§8.14); `ic_editor_panel` juga untuk penghitung "4/9 panels". Tombol warna = lingkaran swatch (bukan ikon) |
| `status` | `ic_status_check_circle_filled`, `ic_status_circle_outline` | Checklist syarat Publish exhibit (§8.14) |
| logo | `logo_vex` (satu warna, **di-tint**), `logo_google` (multi-warna, **tanpa tint**) | TopBar Home/AI; tombol Google |

**Checklist ekspor dari Figma (per ikon):**
1. Satu frame **24 × 24** per ikon (ikon nav tetap digambar di 24; ukuran tampil 28dp lewat skala). Nama layer = nama file.
2. Ubah stroke jadi bentuk: *Object → Outline stroke*, lalu gabungkan (*Union/Flatten*) bila memungkinkan. Hapus mask/clip/blur/efek.
3. Isi **satu warna solid hitam `#000000`** untuk semua ikon yang akan di-tint. **Jangan** mengekspor warna merah Logout atau putih/ungu logo; warna diberikan lewat tint token. Pengecualian: `logo_google` tetap multi-warna.
4. Export: **SVG**, 1x, *Outline text* aktif, *Include "id" attribute* nonaktif.
5. Impor ke Android: Android Studio → klik kanan `res-icons/drawable` → *New → Vector Asset → Local file (SVG)*; ukuran 24 × 24dp; beri nama sesuai konvensi. (Untuk banyak file sekaligus, alat konversi batch seperti plugin Valkyrie bisa dipertimbangkan; cek fitur terbarunya.)
6. Buka tiap XML di Preview. VectorDrawable tidak mendukung filter/blur dan hanya sebagian gradient. Ikon yang tampil rusak harus disederhanakan di Figma, bukan ditambal di XML.
7. Jangan menaruh `fillColor`/`strokeColor` warna di XML untuk ikon tint (cukup hitam), warna selalu dari Compose.

**Akses di Compose (`ui/icons/VexIcons.kt`):**

```kotlin
@Immutable
data class VexIconPair(@DrawableRes val outline: Int, @DrawableRes val filled: Int)

object VexIcons {
    object Nav {
        val Home     = VexIconPair(R.drawable.ic_nav_home_outline,     R.drawable.ic_nav_home_filled)
        val Schedule = VexIconPair(R.drawable.ic_nav_schedule_outline, R.drawable.ic_nav_schedule_filled)
        val Ai       = VexIconPair(R.drawable.ic_nav_ai_outline,       R.drawable.ic_nav_ai_filled)
        val Explore  = VexIconPair(R.drawable.ic_nav_explore_outline,  R.drawable.ic_nav_explore_filled)
    }
    @DrawableRes val Add = R.drawable.ic_action_add
    @DrawableRes val Back = R.drawable.ic_action_back
    @DrawableRes val ChevronLeft = R.drawable.ic_action_chevron_left
    @DrawableRes val ChevronRight = R.drawable.ic_action_chevron_right
    // ... dst., satu entri per file
    @DrawableRes val LogoVex = R.drawable.logo_vex
    @DrawableRes val LogoGoogle = R.drawable.ic_google
}

@Composable
fun VexIcon(
    @DrawableRes id: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = VexSize.icon,
) = Icon(painterResource(id), contentDescription, modifier.size(size), tint)

// Logo multi-warna: jangan di-tint
// Image(painterResource(VexIcons.LogoGoogle), contentDescription = null)
```

**Aturan:**
- Ikon baru = tambah file di `res-icons/drawable/` **dan** satu entri di `VexIcons` **dan** satu baris di tabel di atas.
- Warna ikon selalu lewat `tint` dari token (`text`, `danger`, `primaryText`, dst.), tidak boleh di XML.
- Tab bottom nav aktif memakai `.filled`, nonaktif `.outline`. Tab Profile menampilkan foto avatar (bukan ikon).
- Ikon yang semantiknya dekoratif (di samping label teks) → `contentDescription = null`.

---

## 8. Spesifikasi per Halaman

### 8.1 Onboarding: Interest (Dark Mode) `[OBSERVED: Wireframe - onboard.png]`
- **Kerangka Halaman:** `AuthLayout` (viewport 412 × 943 dp, background kanvas `#1F1F1F`).
- **Judul Layar:**
  - Teks: "Interest", `displayMedium` (32sp / 700 bold), warna `#FFFFFF`.
  - Posisi: tengah horizontal, posisi $y \approx 95\text{ dp}$.
- **Pilihan Kategori (Chips Flow Layout):**
  - Menggunakan `FlowRow` dengan alignment rata kiri (`Arrangement.Start`), jarak antar chip $\approx 8\text{ dp}$ vertikal dan horizontal.
  - 14 Kategori `[OBSERVED]`:
    - Baris 1: *Cybersecurity*, *Software*, *UI/UX*
    - Baris 2: *Videography*, *Photography*, *3D*
    - Baris 3: *Animation*, *Internet Of Things*
    - Baris 4: *Automation System*, *Game Dev*
    - Baris 5: *Fabrication*, *Manufacturing*, *2D*
    - Baris 6: *Others*
  - **Tampilan Chip (`VexChip.Selectable`):**
    - Bentuk: `Pill` (`RoundedCornerShape(full)` / 100 dp). Tinggi $\approx 38 - 40\text{ dp}$.
    - State Belum Dipilih (Unselected - Dark): Background abu-abu gelap `#343333` (`surfaceInput`), teks putih `#FFFFFF`, 14sp Medium 500, padding horizontal $\approx 16\text{ dp}$, vertikal $\approx 8\text{ dp}$.
    - State Dipilih (Selected): Background aksen **`BrandPurple` (`#BA18F5`)**, teks putih `#FFFFFF` `[ASSUMPTION]`.
- **Area Footer:**
  - Link **"Maybe later"**: Teks warna ungu `primaryText` (`#BA18F5` / `#D257FF`), ukuran 14sp, rata tengah horizontal, tanpa garis bawah pada wireframe dark, posisi di atas divider.
  - Garis Pemisah (Divider): Garis horizontal 1 dp warna abu-abu `#8F8F8F` / `border` selebar area form ($\approx 304 - 360\text{ dp}$).
  - Tombol **"Save"** (Primary CTA):
    - Bentuk: `Pill` (100 dp), lebar penuh ($\approx 304 - 374\text{ dp}$), tinggi $48 - 52\text{ dp}$.
    - Background: **`BrandPurple` (`#BA18F5`)**, teks "Save" putih (`#FFFFFF`), `labelLarge` (16sp Medium 500), margin bawah ke tepi layar $\approx 55\text{ dp}$.
    - Integrasi Backend: Menyimpan preferensi kategori minat pengguna via `PUT /pengguna/kategori-minat`.

### 8.2 Login (Dark Mode) `[OBSERVED: Figma node 3946:17]`
- **Frame & Layout:** 412 × 943 dp. Latar belakang kanvas `#1F1F1F`. Form berpusat horizontal dengan lebar konten **304 dp** (padding horizontal 54 dp pada viewport 412 dp).
- **Judul Layar:**
  - Teks: "Login", `displayMedium` (32sp / 700 bold), warna `#FFFFFF`.
  - Posisi: tengah-atas, posisi y ≈ 100 dp (top offset ke input pertama ±64 dp).
- **Form Input (Komponen Figma `Dark Themed Form` / `4110:33`):**
  - Bentuk: `Pill` (`RoundedCornerShape(full)` / 100 dp).
  - Dimensi: Lebar 304 dp, tinggi 48 dp.
  - Warna: Latar **Solid White (`#FFFFFF`)**, tanpa border. Teks input warna `#000000` dengan placeholder abu-abu muda (`#8A8A92` / `#BDBDBD`).
  - **Input 1 (Email/Username):** placeholder "Email/Username", posisi y = 189 dp – 237 dp.
  - Jarak antar field input: **32 dp** (`VexSpace.s8`).
  - **Input 2 (Password):** placeholder "Password", posisi y = 269 dp – 317 dp.
- **Link Forgot Password?:**
  - Teks: **"Forgot Password?"**, warna `#FFFFFF`, ukuran 14sp, font Elms Sans Medium (weight 500).
  - Posisi: Rata kanan sejajar ujung kanan input field (right = 356 dp), posisi y ≈ 346 dp – 359 dp (jarak ~28 dp di bawah input password).
- **Tombol Login (Primary CTA - Figma `4110:175`):**
  - Bentuk: `Pill` (100 dp), lebar 304 dp, tinggi 48 dp.
  - Background: **BrandPurple (`#BA18F5`)**, teks "Login" putih (`#FFFFFF`), `labelLarge` (16sp Medium 500), rata tengah.
  - Posisi: y = 416 dp – 464 dp (jarak ~57 dp di bawah "Forgot Password?").
- **Divider:**
  - Jarak 22 dp di bawah tombol Login (posisi y = 486 dp).
  - Dimensi: Lebar 304 dp (sejajar tombol/input), tebal 1 dp, warna `#A5A5A5` / `Color.White.copy(0.35f)`.
- **Tombol Google (Figma `4348:239`):**
  - Posisi: y = 512 dp – 560 dp (26 dp di bawah divider).
  - Bentuk: `Pill` (100 dp), lebar 304 dp, tinggi 48 dp.
  - Background: **Solid White (`#FFFFFF`)**, dengan outline/border hitam 1dp (`#000000`).
  - Konten: Logo Google multi-warna di sisi kiri + teks **"Register with Google"** di tengah warna hitam (`#000000`, 16sp Medium) `[OBSERVED di Figma 3946:17]`.
    *(Catatan implementasi: teks wireframe tertulis "Register with Google". Pada implementasi Compose disarankan teks diambil dari `strings.xml` sehingga dapat dikonfigurasi menjadi "Sign in with Google" / "Continue with Google" tanpa mengubah visual pill putih).*
- **Footer (Navigasi ke Register):**
  - Helper text: **"Don't have an account?"**, teks warna `#FFFFFF` / slightly muted (`textSecondary`), ukuran 14sp, rata tengah, posisi y ≈ 815 dp.
  - Jarak ke tombol Register: ~25 dp.
  - Tombol **"Register"** (Inverse Button - Figma `4350:297`):
    - Bentuk: `Pill` (100 dp), lebar 304 dp, tinggi 48 dp.
    - Background: **Solid White (`#FFFFFF`)**, teks "Register" hitam (`#000000`), 16sp Medium 500, rata tengah.
    - Posisi: y = 840 dp – 888 dp.
    - Margin bawah ke tepi layar (bottom padding): **55 dp** (943 - 888 = 55 dp).

### 8.3 Register (Dark Mode) `[OBSERVED: Wireframe - Register.png]`
- **Frame & Layout:** 412 × 943 dp. Latar belakang kanvas `#1F1F1F`. Form berpusat horizontal dengan lebar konten **304 dp** (padding horizontal 54 dp).
- **Judul Layar:**
  - Teks: "Register", `displayMedium` (32sp / 700 bold), warna `#FFFFFF`.
  - Posisi: tengah-atas, posisi $y \approx 95 - 100\text{ dp}$.
- **Form Input (4 Field Pill Putih):**
  - Bentuk: `Pill` (`RoundedCornerShape(full)` / 100 dp). Lebar 304 dp, tinggi 48 dp.
  - Warna: Latar **Solid White (`#FFFFFF`)**, tanpa border. Teks input hitam (`#000000`), placeholder abu-abu muda (`#8A8A92`).
  - Jarak antar field: $16 - 20\text{ dp}$ (`VexSpace.s4` / `s5`).
  - **Field 1 (Email):** Placeholder "Email".
  - **Field 2 (Username):** Placeholder "Username".
  - **Field 3 (Password):** Placeholder "Password", `VisualTransformation = PasswordVisualTransformation()`.
  - **Field 4 (Confirm Password):** Placeholder "Confirm Password", `VisualTransformation = PasswordVisualTransformation()`.
- **Tombol Register (Primary CTA):**
  - Bentuk: `Pill` (100 dp), lebar 304 dp, tinggi 48 dp.
  - Background: **`BrandPurple` (`#BA18F5`)**, teks "Register" putih (`#FFFFFF`), `labelLarge` (16sp Medium 500), rata tengah.
  - Jarak di bawah Confirm Password: $\approx 24\text{ dp}$.
- **Divider:**
  - Jarak 20 dp di bawah tombol Register, lebar 304 dp, tebal 1 dp, warna `#A5A5A5`.
- **Tombol Google:**
  - Bentuk: `Pill` (100 dp), lebar 304 dp, tinggi 48 dp, background **Solid White (`#FFFFFF`)**, dengan outline/border hitam 1dp (`#000000`).
  - Konten: Logo Google multi-warna di sisi kiri + teks **"Login with Google"** warna hitam (`#000000`, 16sp Medium) `[OBSERVED pada wireframe Register]`.
- **Footer (Navigasi ke Login):**
  - Helper text: **"Already have an account?"**, teks putih / `textSecondary`, ukuran 14sp, rata tengah horizontal.
  - Tombol **"Login"** (Inverse Button):
    - Bentuk: `Pill` (100 dp), lebar 304 dp, tinggi 48 dp, background **Solid White (`#FFFFFF`)**, teks "Login" hitam (`#000000`), 16sp Medium 500, rata tengah.
    - Margin bawah ke tepi layar: **55 dp**.
- **Integrasi Backend:** Submit form memanggil endpoint `POST /auth/register` (Laravel Sanctum API), yang mengembalikan token autentikasi dan mengarahkan pengguna ke halaman Onboarding Interest (§8.1).

### 8.4 Schedule (Dark Mode) `[OBSERVED: 6 Wireframe Schedule]`

Spesifikasi lengkap halaman Schedule berbasis observasi langsung 6 file wireframe dark mode (`D:\front-mobile\dark-mode\schedule\wireframe schedule*.png`). Layar ini berjalan murni pada sisi **frontend** dengan data mockup lokal mandiri (tanpa dependensi jaringan backend) mengikuti pola MVI (Model-View-Intent) dan Unidirectional Data Flow.

#### 8.4.1 Anatomi & Tata Letak Layar Utama
- **Kanvas & Insets:**
  - Latar belakang: `#1F1F1F` (`VexTheme.colors.bg`).
  - Terbungkus dalam `AppScaffold` dengan tab bottom navigation `BottomNavTab.Schedule` aktif.
  - Padding atas: `statusBarsPadding()`, padding horizontal konten: 20dp (`VexSpace.s5`), padding bawah terikat `innerPadding` dari Scaffold.

- **Header: SearchBar (`SearchBar`):**
  - Bentuk: `Pill` (`RoundedCornerShape(VexRadius.full)`).
  - Dimensi: Tinggi 44–46dp (`VexSize.control`), margin horizontal 20dp, margin atas 16dp, margin bawah 16dp.
  - Warna: Latar `surfaceInput` (`#2C2C2C`), tanpa border outline saat default.
  - Ikon: Kaca pembesar di kiri (`ic_action_search` / `ic_nav_explore_outline`, 20dp, warna `textMuted`).
  - Teks: Placeholder `"Search"` (`textMuted`), teks ketikan putih (`text`), single-line, `ImeAction.Search`.
  - Fungsi: Melakukan filter instan (*realtime search*) terhadap daftar item pada tab yang sedang aktif berdasarkan nama event, tanggal, atau nama pemohon.

- **Tab Switcher (`VexTextTabs`):**
  - Berisi 3 tab seimbang (masing-masing `weight(1f)`): **`Approved` | `Waiting` | `Request`**.
  - Tinggi baris tab: 48dp.
  - **Tab Aktif:**
    - Label teks: `titleSmall` / 14sp SemiBold 600, warna `text` (putih `#FFFFFF`).
    - Garis indikator bawah: Tebal 1dp, warna `text` (putih solid) selebar tab penuh.
  - **Tab Non-Aktif:**
    - Label teks: 14sp Medium 500, warna `textMuted` (`#8E8E93`).
    - Garis bawah: Warna pembatas halus (`border` / `#313131`).
  - Garis pemisah horizontal: Garis 1dp `#313131` di bawah seluruh baris tab.

---

#### 8.4.2 Tab 1: Approved (`wireframe schedule.png`)
Menampilkan daftar kegiatan pertemuan virtual yang telah disetujui.
- **Komponen Kartu: `EventListItem`**
  - Tata letak: Baris horizontal (`Row`), padding vertikal 12dp (`VexSpace.s3`), padding horizontal 20dp (`VexSpace.s5`), pemisah antar item jarak 12dp.
  - **Sisi Kiri (Thumbnail Poster):**
    - Rasio: 16:9, lebar ≈ 140–150dp, tinggi ≈ 80–85dp.
    - Sudut melengkung: `RoundedCornerShape(VexRadius.md)` (8–12dp), `ContentScale.Crop`.
    - Sumber: Drawable banner pameran (`poster_3.webp` untuk Open Source Competition, `poster_1.webp` untuk PBL EXPO 2026).
  - **Sisi Kanan (Informasi & Aksi):**
    - Kolom vertikal (`Column`), `Arrangement.SpaceBetween`, tinggi mengikuti thumbnail.
    - Tanggal: Format `24th Sep 2026`, gaya `titleSmall` (16sp SemiBold 600), warna putih (`text`).
    - Jam: Format `02.00 PM`, gaya `bodyMedium` (14sp Regular 400), warna putih (`text`).
    - Tombol Teks: `"See details"`, rata kanan-bawah, gaya `bodySmall` (13sp Regular 400), warna redup `textMuted` (`#8E8E93`), clickable ripple.
- **Interaksi Tap:**
  - Mengetuk baris kartu atau teks `"See details"` membuka modal bottom sheet `EventDetailSheet` (varian Approved).

---

#### 8.4.3 Tab 2: Waiting (`wireframe schedule-2.png`)
Menampilkan permohonan pertemuan yang diajukan pengguna ke pihak lain dan masih menunggu persetujuan (*outgoing request*).
- **Komponen Kartu: `EventListItem`**
  - Menggunakan format visual identik dengan Tab Approved (thumbnail 16:9 + tanggal + jam + teks `"See details"`).
  - Menampilkan daftar pameran yang sedang diajukan (mis. Open Source Competition, Virtual Exhibition VEX).
- **Interaksi Tap:**
  - Mengetuk item membuka modal bottom sheet `EventDetailSheet` (varian Waiting).

---

#### 8.4.4 Tab 3: Request (Inbox) (`wireframe schedule-4.png`)
Menampilkan permohonan masuk dari pihak lain yang ingin berdiskusi dengan pengguna (*incoming request inbox*).
- **Komponen Kartu: `RequestListItem`**
  - Tata letak: Baris horizontal (`Row`), padding vertikal 12dp, padding horizontal 20dp.
  - **Sisi Kiri (Avatar Pemohon):**
    - Avatar profil lingkaran diameter 48dp (`Avatar.md`), fallback inisial jika foto null.
  - **Sisi Kanan (Informasi Pesan):**
    - Baris Atas (`Row`, lebar penuh, `Arrangement.SpaceBetween`):
      - Kiri: Username pemohon (contoh: `Gra._.aph`, `Shaf.One`), gaya `titleSmall` (15sp SemiBold 600, warna `text`).
      - Kanan: Tanggal & jam ringkas (`27/9/26  02.00 PM`), gaya `bodySmall` (12sp Regular 400, warna `textMuted`).
    - Baris Bawah:
      - Cuplikan isi pesan (catatan permohonan kerjasama/diskusi), gaya `bodyMedium` (13–14sp Regular 400, warna `textMuted`), `maxLines = 2`, `overflow = TextOverflow.Ellipsis`.
- **Interaksi Tap:**
  - Mengetuk baris item membuka modal bottom sheet `RequestDetailSheet`.

---

#### 8.4.5 Bottom Sheet: `EventDetailSheet` (`wireframe schedule-1.png` & `schedule-3.png`)
Bottom sheet modal yang dibungkus oleh `VexBottomSheet`:
- **Anatomi Sheet:**
  - Drag handle: Pill 40×4dp di atas tengah, warna `border` (`#313131`).
  - Container: Latar `surface` (`#252525`), sudut atas melengkung 28dp (`VexRadius.xl`), padding horizontal 24dp, padding bawah 32dp + `navigationBarsPadding()`.
  - **Banner Pameran:**
    - Gambar pameran horizontal selebar penuh konten (rasio 16:9, lebar ~364dp), sudut melengkung 12dp (`VexRadius.md`), `ContentScale.Crop`.
    - Margin atas 12dp, margin bawah 16dp.
  - **Pesan Status Pertemuan (`AnnotatedString`):**
    - Teks body regular 14–15sp dengan bagian tanggal & jam dicetak tebal (`FontWeight.Bold`):
    - **Varian Approved (`wireframe schedule-1.png`):**  
      > "Your request which is scheduled in **Thursday, 24th September 2026** at **02.00 PM** has been approved. The room will be available on said time, please attend the meeting as scheduled."
    - **Varian Waiting (`wireframe schedule-3.png`):**  
      > "Your request which is scheduled in **Thursday, 24th September 2026** at **02.00 PM** has been sent and is waiting for approval. Please wait patiently."
    - Margin bawah: 32dp.
  - **Tombol Aksi Utama ("Join Room"):**
    - Bentuk: `Pill` (`RoundedCornerShape(VexRadius.full)`), lebar penuh (`fillMaxWidth`), tinggi 48dp (`VexSize.button`).
    - **Kondisi Approved:** Background ungu neon `Primary` (`#B800FF`), teks putih bold 16sp, aktif/clickable (menampilkan snackbar/feedback masuk ruang virtual).
    - **Kondisi Waiting:** Background ungu gelap padam (`#3D154D` / `containerDisabled`), teks ungu redup (`#7A588A` / `onDisabled`), **disabled (`enabled = false`)**.

---

#### 8.4.6 Bottom Sheet: `RequestDetailSheet` (`wireframe schedule-5.png`)
Bottom sheet peninjauan permohonan masuk pada tab Request:
- **Anatomi Sheet:**
  - Drag handle & kontainer identik dengan `EventDetailSheet`.
  - **Banner Pameran:** Banner 16:9 pameran tempat permohonan diajukan, sudut melengkung 12dp, margin bawah 16dp.
  - **Label Jadwal Target (`AnnotatedString`):**
    - Format: "Requested for **Thursday, 24th September 2026 at 02.00 PM**." (tanggal & jam bold, teks putih 15sp, margin bawah 16dp).
  - **Baris Profil Pemohon:**
    - Baris horizontal (`Row`): Avatar pemohon diameter 28dp (`Avatar.sm`) + spasi 12dp + Username (contoh: `Gra._.aph`, 15sp SemiBold 600, teks putih).
    - Margin bawah 12dp.
  - **Isi Pesan Lengkap:**
    - Teks paragraf permohonan (contoh: *"Saya dari perusahaan citra sinar bahagia ingin berdiskusi dengan anda mengenai salah satu proyek yang sedang anda kembangkan, jika anda bersedia saya siap menawarkan sebuah kerjasama berupa kontrak."*).
    - Gaya: `bodyMedium` (14sp Regular 400, warna putih `#FFFFFF`), line height 20sp, margin bawah 32dp.
  - **Dua Tombol Aksi Sejajar (50:50 Side-by-side Pill Buttons):**
    - Baris horizontal (`Row`, `horizontalArrangement = Arrangement.spacedBy(16.dp)`):
      1. **Tombol "Approve":**
         - Bobot: `Modifier.weight(1f)`, tinggi 48dp, bentuk pill.
         - Background: Ungu neon `Primary` (`#B800FF`), teks "Approve" putih SemiBold 16sp.
         - Aksi: Memindahkan item dari daftar request ke daftar approved secara lokal di memori frontend dan menutup sheet.
      2. **Tombol "Reject":**
         - Bobot: `Modifier.weight(1f)`, tinggi 48dp, bentuk pill.
         - Background: Merah terang `Danger` (`#FF1F1F`), teks "Reject" putih SemiBold 16sp.
         - Aksi: Menghapus item dari daftar request secara lokal di memori frontend dan menutup sheet.

---

#### 8.4.7 Arsitektur Frontend & State Management (MVI Mandiri)
Halaman Schedule dirancang mandiri tanpa dependensi backend:
- **Model Data:**
  - `ScheduleEventItem(id, title, dateText, timeText, fullDateFormatted, bannerRes, isApproved)`
  - `ScheduleRequestItem(id, requesterName, requesterAvatarRes, dateText, timeText, fullDateFormatted, note, bannerRes)`
- **UI State (`ScheduleUiState`):**
  - `selectedTab`: `ScheduleTab` (`Approved`, `Waiting`, `Request`).
  - `searchQuery`: string kueri filter pencarian.
  - `approvedItems`: daftar `ScheduleEventItem` tersimpan.
  - `waitingItems`: daftar `ScheduleEventItem` menunggu konfirmasi.
  - `requestItems`: daftar `ScheduleRequestItem` inbox.
  - `activeEventDetail`: item acara yang sedang dibuka bottom sheet-nya (null jika tertutup).
  - `activeRequestDetail`: item request yang sedang dibuka sheet-nya (null jika tertutup).
  - `filteredApprovedItems`, `filteredWaitingItems`, `filteredRequestItems`: hasil kalkulasi memfilter item berdasarkan `searchQuery`.
- **Dataset Mockup (`ScheduleDummyData.kt`):**
  - Menyiapkan dummy data realistis memakai resource lokal yang tersedia (`R.drawable.poster_1`, `poster_2`, `poster_3`) sesuai visual wireframe.

### 8.5 Explore (Dark & Light Mode) `[OBSERVED: Wireframe - Explore*.png]`

**Sumber observasi:** `D:\front-mobile\dark-mode\explore\` (8 wireframe: `Wireframe - Explore.png`, `Explore history`, `Explore all`, `Explore posts`, `Explore exibhition`, `Explore account`, `Explore filter`, `Post`).
**Frame acuan:** 412 × 943 dp. Tema default Dark (`colors.bg` = `#1F1F1F`). Seluruh elemen memakai token semantik `VexTheme.colors` agar otomatis menyesuaikan Light Mode.

**Kerangka layar:** `AppScaffold` (BottomNav tab Explore aktif). Layar induk `ExploreScreen` mengelola beberapa state tampilan secara lokal (MVI), bukan screen terpisah:

| State | Pemicu | Isi |
|---|---|---|
| **Discovery** | Default saat tab Explore dibuka | `ExploreTopBar` (SearchBar + filter) + `MediaGrid` 3 kolom |
| **Search History** | Tap SearchBar | Back + SearchBar aktif + seksi "Recents" |
| **Search Results** | Query terisi / submit | Back + SearchBar + 4 tab hasil |
| **Filter Sheet** | Tap tombol filter | `CategoryFilterSheet` (modal bottom sheet) |

#### 8.5.1 Explore Discovery (Default State) `[OBSERVED]`
- **Top Bar (`ExploreTopBar`):** `SearchBar` (pill, `surfaceInput`, weight 1f) berdampingan dengan tombol filter (`ic_action_filter`, 24dp, `text`) di sisi kanan. Padding horizontal 24dp.
- **Body (`MediaGrid`):**
  - Grid **3 kolom** standar, celah antar item 2dp, sudut tajam (radius 0dp).
  - Tile potret mendominasi dengan rasio `VexAspect.poster` (~4:5).
  - Sesekali muncul **tile rentang 2 kolom** (`GridItemSpan(2)`, rasio horizontal ~16:9), contoh poster *"Open Source Competition"* `[OBSERVED pola berulang]`.
- **Bottom Nav:** tab ke-4 (Explore, `ic_nav_explore_filled` aktif).
- **Interaksi:**
  - Tap `SearchBar` → beralih ke **Search History** (§8.5.2).
  - Tap tombol filter → buka **CategoryFilterSheet** (§8.5.4).
  - Tap tile karya → buka **PostsFeedScreen** (§8.5.5), scroll otomatis ke post terkait.
- **State wajib:** Loading (skeleton grid), Empty (tidak ada hasil), Error, Content. Skeleton memakai `Skeleton.Rect` menyusun pola grid 3 kolom.

#### 8.5.2 Search State & Recent History `[OBSERVED: Wireframe - Explore history.png]`
- **Top Bar:** Tombol panah kembali (`ic_action_back`, 24dp, `text`) di kiri + `SearchBar` aktif (fokus, keyboard terbuka) mengisi sisa lebar.
- **Header Seksi:** Teks **"Recents"** (`titleMedium` / 16sp SemiBold, `text`), padding horizontal 24dp.
- **Body:** Daftar riwayat pencarian (`SearchHistoryItem`) mendukung dua tipe entri:
  1. **Query:** Lingkaran 36dp latar `surfaceInput` berisi ikon jam (`ic_action_history`, `textMuted`) + teks kueri (`bodyMedium`, `text`) + tombol hapus "✕" (`ic_action_close`, `textMuted`) di kanan.
  2. **Account:** `Avatar(sm)` bulat + kolom `@username` (SemiBold, `text`) & nama lengkap (`bodySmall`, `textMuted`) + tombol hapus "✕" di kanan.
- **Interaksi:** Tap entri query mengisi SearchBar & menjalankan pencarian; tap entri akun membuka profil; tap "✕" menghapus entri riwayat tersebut.
- **State wajib:** Empty (belum ada riwayat → tampilkan teks kosong, bukan "Recents" kosong).

#### 8.5.3 Search Results (4 Tab) `[OBSERVED: Wireframe - Explore all/posts/exibhition/account.png]`
Ditampilkan saat pengguna mengetik/menjalankan kueri (contoh wireframe: `"vex"`).
- **Top Bar:** Tombol kembali + `SearchBar` berisi teks kueri.
- **Tabs (`VexTextTabs`):** `For you` | `Accounts` | `Posts` | `Exhibits`. Tab aktif: teks `text` + garis bawah 1dp `text`; nonaktif `textMuted`. Scrollable bila perlu.
- **Tab `For you`** (default): Seksi **"Accounts"** (maks 2–3 akun teratas via `SearchAccountRow`, diakhiri label seksi `VexSectionLabel`) diikuti seksi **"Posts"** (`MediaGrid(columns = 2)`).
- **Tab `Accounts`:** Daftar vertikal penuh akun yang cocok (`SearchAccountRow`). Tap → profil akun.
- **Tab `Posts`:** `MediaGrid(columns = 2)`, celah 2dp, rasio poster. Tap → `PostsFeedScreen`.
- **Tab `Exhibits`:** Daftar vertikal banner pameran landscape 16:9 (`ExhibitBannerCard`, padding horizontal 16dp, gap 12dp). Tap → 3D Virtual Exhibition Room.
- **State wajib:** Loading, Empty ("No results for '{query}'"), Error, Content per tab.

#### 8.5.4 Category Filter Modal Sheet `[OBSERVED: Wireframe - Explore filter.png]`
- **Kontainer:** `VexBottomSheet` (radius atas 28dp, handle abu, container `surface`, scrim `scrim`).
- **Header:** Judul **"Category"** (kiri, 18sp SemiBold, `text`) + aksi teks **"Reset"** (kanan, `labelLarge`/14sp SemiBold, warna `primary` / `BrandPurple`).
- **Body:** `FlowRow` berisi 14 chip kategori (`VexChip` selectable), gap 8dp, padding horizontal 24dp:
  `Cybersecurity`, `Software`, `UI/UX`, `Videography`, `Photography`, `3D`, `Animation`, `Internet Of Things`, `Automation System`, `Game Dev`, `Fabrication`, `Manufacturing`, `2D`, `Others`.
- **State Chip:** Unselected = latar `surfaceInput`, teks `text`; Selected = latar `primary`, teks `onPrimary` (putih). Multi-pilih.
- **Interaksi:** Tap "Reset" mengosongkan seluruh pilihan; menutup sheet menerapkan filter ke grid Discovery.

#### 8.5.5 PostsFeedScreen (Detail Scrollable Post) `[OBSERVED: Wireframe - Post.png]`
- **Konteks:** Dibuka saat tile karya di grid (Discovery, tab `Posts`, atau seksi `Posts` tab `For you`) diketuk.
- **Route:** `posts_feed?postId={postId}` (argumen opsional; kosong = mulai dari atas feed).
- **Top Bar (`VexTopBar.Back`):** Tombol kembali (`ic_action_back`) di kiri + teks judul **"Posts"** (18sp SemiBold, `text`). **Fixed/pinned**, tidak ikut scroll.
- **Body (Scrollable Feed):**
  - **`LazyColumn`** vertikal penuh — daftar dapat di-scroll bebas ke atas/bawah.
  - Posisi awal diatur via `rememberLazyListState` + `LaunchedEffect` → `scrollToItem(targetIndex)` bila `postId` diberikan, agar post yang diketuk langsung terlihat.
  - Setiap item = kartu utuh **`PostCard`**:
    - **Header:** `@username` (SemiBold, `text`) di kiri; ikon Kalender (`Request Meeting`, §8.6.1) dan ikon Play/3D di kanan (24dp, gap 16dp).
    - **Media:** Gambar karya **edge-to-edge** selebar layar, sudut 0dp (tajam), rasio mengikuti karya (mis. 4:5 / 16:9).
    - **Caption & Waktu:** `@username` tebal + deskripsi karya dengan tautan URL yang dapat diklik + aksi `More`/`Less` (lihat `PostCaption`), lalu timestamp relatif (mis. *"17 hours ago"*, `bodySmall`, `textMuted`).
- **Interaksi:** Ikon kalender → `RequestMeetingScreen`; ikon play → 3D Virtual Exhibition Room; tap `@username` → profil; tap tautan → peramban eksternal (`UriHandler`).
- **State wajib:** Loading (skeleton `PostCard`), Empty, Error, Content.

#### 8.5.6 UI State & Dummy Data `[ASSUMPTION]`
- **`ExploreUiState` (`ExploreUiState.kt`):** `isSearchActive`, `searchQuery`, `selectedTab` (`ExploreSearchTab`: `ForYou`/`Accounts`/`Posts`/`Exhibits`), `isFilterSheetOpen`, `selectedCategories: Set<String>`, `availableCategories: List<String>`, `recentSearches: List<RecentSearchItem>` (sealed: `Query`/`Account`), `discoveryMedia: List<MediaGridItem>`, `resultAccounts`, `resultPosts`, `resultExhibits`.
- **`ExploreDummyData.kt`:** dummy data realistis memakai aset lokal (`poster_1`/`poster_2`/`poster_3`): riwayat (`"vex"`, `@gra._.aph`, `@shaf.one`), discovery grid (termasuk tile span 2 *"Open Source Competition"*), serta hasil 4 tab. Murni frontend, tanpa dependensi backend (§3.2).

### 8.6 Home (Dark Mode) `[OBSERVED: Wireframe - Home.png]`
- **TopBar (`VexTopBar.Home`):**
  - Tinggi 56 dp + status bar inset, latar belakang menyatu dengan kanvas (`#1F1F1F`). Tanpa garis pembatas bawah `[OBSERVED]`.
  - Sisi Kiri: Ikon "+" (`ic_action_add`, 24 dp, putih `#FFFFFF`, margin kiri 24 dp) → membuka alur pembuatan konten (*Create Flow* §8.13 / §8.14).
  - Sisi Kanan: Logo resmi VEX (`logo_vex`, putih `#FFFFFF`, margin kanan 24 dp).
- **Seksi "Coming Soon":**
  - Judul Seksi: **"Coming Soon"**, `titleLarge` (20sp SemiBold 600, putih `#FFFFFF`), padding horizontal 24 dp, margin bawah 12 dp.
  - Carousel Poster Pameran (`LazyRow` `EventBanner`):
    - Kartu banner horizontal memanjang (lebar $\approx 230 - 240\text{ dp}$, tinggi $\approx 130 - 140\text{ dp}$, rasio $\approx 16:9$).
    - Sudut membulat: radius 8–12 dp (`VexRadius.md`).
    - Jarak antar kartu: 12 dp (`VexSpace.s3`), padding awal/akhir 24 dp (`VexSpace.s6`).
    - Menampilkan karya/poster promosi kompetisi & expo yang akan datang (mis. "Open Source Competition", "PBL EXPO 2026").
  - Garis Pembatas Seksi: Garis horizontal selebar layar, tebal 1 dp, warna `#313131` / `border`, memisahkan carousel "Coming Soon" dengan feed post.
- **Feed Karya (`PostCard`):**
  - **Header Kartu Post:**
    - Kiri: Nama pembuat karya `@Graaph` (username, 18sp SemiBold 600, warna putih `#FFFFFF`, margin kiri 24 dp).
    - Kanan: Baris dua ikon aksi (ukuran 24 dp, warna putih, jarak antar ikon 16 dp, margin kanan 24 dp):
      1. **Ikon Kalender** (`ic_action_calendar`): **Membuka alur "Request meeting" (§8.6.1)** untuk mengajukan jadwal pertemuan virtual pada pameran ini.
      2. **Ikon Play / Kubus 3D** (`ic_action_play`): **Membuka ruang pameran 3D virtual (*Virtual Exhibition Room*)** karya terkait.
  - **Media Post (Gambar Karya):**
    - Tampilan penuh selebar layar (lebar 412 dp / edge-to-edge), tanpa radius sudut (0 dp, sudut tajam).
    - Tinggi gambar proporsional mengikuti rasio asli karya pengguna (mis. format poster 4:5 atau 16:9).
- **Bottom Navigation Bar (`BottomNav`):**
  - Latar belakang: `#252525` (`navBar`), tinggi 64 dp + navigation bar insets.
  - 5 Tab:
    1. Home (`ic_nav_home_filled` — aktif, ikon rumah solid putih).
    2. Schedule (`ic_nav_schedule_outline`).
    3. AI Assistant (`ic_nav_ai_outline` — bintang berkilau 4 sudut).
    4. Explore (`ic_nav_explore_outline` — kaca pembesar).
    5. Profile (Avatar pengguna lingkaran bulat 28 dp).

---

### 8.6.1 Request Meeting (Dark & Light Mode) `[OBSERVED Dark: Wireframe - Request Meeting.png, Light: Diturunkan dari Token Semantik]`
- **Pemicu Alur:** Dibuka ketika pengguna menekan ikon kalender (`ic_action_calendar`) pada kartu karya `PostCard` di Home Feed (§8.6).
- **Kerangka Halaman:** `SubPage` dengan **`VexBottomActionBar`** (Top bar dengan tombol kembali + konten scrollable + tombol CTA "Send Request" **fixed di bawah seperti bottom nav**, tidak ikut scroll). Latar belakang kanvas mengikuti `VexTheme.colors.bg` (Dark `#1F1F1F`, Light `#FFFFFF`).
- **Dukungan Dark & Light Mode (Mapping Token Semantik):**
  Seluruh elemen wajib menggunakan token `VexTheme.colors` agar otomatis menyesuaikan kedua mode:

  | Elemen UI | Dark Mode | Light Mode | Token Semantik |
  |---|---|---|---|
  | Latar Belakang Kanvas | `#1F1F1F` | `#FFFFFF` | `colors.bg` |
  | TopBar & Teks Judul | Teks `#FFFFFF` | Teks `#000000` | `colors.text` |
  | Divider Pembatas Seksi | `#313131` / `White 10%` | `#DADADA` | `colors.border` |
  | Header Hari Kalender | `White 40%` (`#8A8A92`) | `#8A8A92` | `colors.textMuted` |
  | Angka Tanggal Normal | `#FFFFFF` | `#000000` | `colors.text` |
  | Angka Tanggal Non-Bulan | `#555555` / `White 25%` | `#B5B5BD` | `colors.textDisabled` |
  | Tanggal Terpilih (Lingkaran) | `BrandPurple` (`#BA18F5`) | `BrandPurple` (`#BA18F5`) | `colors.primary` (teks `onPrimary`) |
  | Time Picker (Blok Aktif) | `BrandPurple` (`#BA18F5`) | `BrandPurple` (`#BA18F5`) | `colors.primary` (teks `onPrimary`) |
  | Time Picker (Blok Tidak Aktif) | Ungu gelap / `White 10%` | `#E5E5E5` | `colors.surfaceInput` (teks `textMuted`) |
  | Input Box "Message..." | Background `White 10%`, border `White 20%` | Background `#E5E5E5`, border `#BDBDBD` | `colors.surfaceInput` + `colors.borderInput` |
  | Sticky Action Bar Container | `#252525` (`navBar`) | `#FFFFFF` (`navBar`) | `colors.navBar` |
  | Divider Atas Action Bar | `#313131` | `#DADADA` | `colors.border` |
  | Tombol CTA "Send Request" | `BrandPurple`, teks `#FFFFFF` | `BrandPurple`, teks `#FFFFFF` | `colors.primary` (teks `onPrimary`) |

- **TopBar (`VexTopBar.Back`):**
  - Sisi Kiri: Ikon kembali (panah kiri `ic_action_back`, 24 dp, warna `colors.text`, margin kiri 22.5 dp).
  - Judul: **"Request meeting"**, warna `colors.text`, ukuran 20sp Medium 500.
  - Garis Pembatas Bawah: Garis horizontal 1 dp warna `colors.border` pada $y = 66\text{ dp}$.
- **Konten Halaman (Scrollable via LazyColumn / verticalScroll):**
  - **Seksi 1: Info & Banner Pameran:**
    - Judul Pameran: mis. "6th International Expo", 18–20sp SemiBold 600, warna `colors.text`, padding horizontal 24 dp, margin atas 20 dp.
    - Banner Pameran: Kartu poster persegi panjang membulat (radius $12 - 16\text{ dp}$ / `VexRadius.lg`), rasio $\approx 16:9$, margin horizontal 24 dp, margin bawah 20 dp.
    - Garis Pembatas: Garis horizontal 1 dp warna `colors.border`.
  - **Seksi 2: Pemilihan Tanggal ("Chose Date"):**
    - Judul Seksi: "Chose Date", 18sp SemiBold 600, warna `colors.text`, padding horizontal 24 dp.
    - **Header Navigasi Bulan & Tahun (Dinamis):**
      - Judul Bulan & Tahun: mis. "September 2026", 16sp SemiBold, warna `colors.text`.
      - Tombol Arah (Chevron): Dua tombol aksi `VexIcon(id = VexIcons.ChevronLeft)` dan `VexIcon(id = VexIcons.ChevronRight)` (20dp, warna `colors.text`, touch target minimal 36–48dp) untuk berpindah bulan dinamis secara lancar.
    - Sub-label Tanggal Terpilih: e.g. "Sat, 26 September 2026", 14sp Medium 500, warna `colors.textMuted`, diformat dinamis via utilitas `DateFormatter` dari tanggal aktif `LocalDate`.
    - **Kalender Interaktif (Grid 7 Kolom Dinamis via `java.time` + Desugaring):**
      - Logika Tanggal: Menggunakan standard `java.time.LocalDate` dan `YearMonth` yang didukung `coreLibraryDesugaring` pada Android `minSdk = 24`. Tidak menggunakan pustaka UI eksternal guna mematuhi §2 Aturan Emas (Rule 9 & 10) serta menjaga konsistensi token semantik.
      - Tinggi Grid Konsisten: Selalu membuat 42 sel (6 baris × 7 kolom) agar tinggi layout stabil tanpa getaran layout (jank) saat berpindah bulan. Hari pertama minggu dimulai dari Senin (Monday-first) sesuai wireframe.
      - Baris Header Hari: Mon, Tue, Wed, Thu, Fri, Sat, Sun (14sp, warna `colors.textMuted`, rata tengah).
      - Grid Angka Tanggal:
        - Tanggal di luar bulan aktif (prev/next month): warna `colors.textDisabled`, tidak dapat dipilih (`clickable(enabled = false)`).
        - Tanggal bulan aktif: warna `colors.text`, dapat diklik untuk memilih tanggal pertemuan.
        - Tanggal Terpilih: Lingkaran penuh warna **`colors.primary` (`BrandPurple` `#BA18F5`)**, teks angka putih tebal `colors.onPrimary`.
      - Margin horizontal kalender: 24 dp.
    - Garis Pembatas: Garis horizontal 1 dp warna `colors.border`.
  - **Seksi 3: Pemilihan Jam ("Chose Time" - Segmented Picker):**
    - Judul Seksi: "Chose Time", 18sp SemiBold 600, warna `colors.text`, padding horizontal 24 dp, margin atas 16 dp, margin bawah 16 dp.
    - **Segmented Time Picker Interaktif:**
      - Terdiri dari 4 blok kontrol berjajar horizontal di tengah (padding horizontal 24 dp, gap $\approx 12\text{ dp}$):
        1. Kotak Jam (Hour): Bentuk rounded box (radius 10–12 dp / `VexRadius.md`), ukuran $\approx 64 \times 72\text{ dp}$, background aktif **`colors.primary`**, teks angka jam (mis. "02") 28sp Medium 500 warna `colors.onPrimary`.
        2. Tanda Titik Dua (Colon): Teks ":" 24sp warna `colors.text` di tengah vertikal.
        3. Kotak Menit (Minute): Bentuk rounded box (radius 10–12 dp), background aktif **`colors.primary`**, teks angka menit (mis. "30") 28sp warna `colors.onPrimary`.
        4. Toggle Periode (AM / PM):
           - Dua kotak berdampingan: Kotak "AM" dan Kotak "PM".
           - Kotak tidak aktif (mis. AM): background `colors.surfaceInput`, teks 18sp warna `colors.textMuted`.
           - Kotak aktif (mis. PM): background solid **`colors.primary`**, teks 18sp warna `colors.onPrimary`.
      - Perilaku: Mengetuk kotak jam/menit mengaktifkan input/picker pengubah waktu; mengetuk AM/PM menukar status periode aktif.
    - Garis Pembatas: Garis horizontal 1 dp warna `colors.border` setelah time picker.
  - **Seksi 4: Catatan Pesan ("Message..."):**
    - Field Input Pesan Multiline (`VexTextField.Boxed` / setara textarea):
      - Bentuk: Rounded rectangle (radius 8–10 dp / `VexRadius.md`), padding horizontal 24 dp, margin vertikal 16 dp.
      - Background: `colors.surfaceInput`, outline border 1 dp tipis `colors.borderInput`.
      - Placeholder: "Message...", warna `colors.textMuted`, ukuran 14–16sp. Teks isi: `colors.text`.
      - Tinggi minimal: $\approx 120 - 140\text{ dp}$, mendukung input teks multiline. Sifat field: opsional.
- **Footer Aksi Tetap (`VexBottomActionBar` - Fixed CTA):**
  - **Tombol "Send Request" (Primary CTA):**
    - Bentuk: `Pill` (`RoundedCornerShape(full)` / 100 dp).
    - Dimensi: Lebar 374 dp (margin horizontal 19–20 dp), tinggi $52 - 56\text{ dp}$.
    - Background: **`colors.primary` (`BrandPurple` `#BA18F5`)**, teks "Send Request" `colors.onPrimary` (`#FFFFFF`), `labelLarge` (16sp Medium 500), rata tengah.
  - **Posisi & Perilaku:**
    - Diletakkan di dalam `Scaffold(bottomBar = { VexBottomActionBar(...) })`, **bukan di dalam kolom scroll**.
    - Bar berlatar `colors.navBar` (Dark `#252525`, Light `#FFFFFF`) dengan garis atas 1 dp `colors.border`, menempel di bawah layar persis seperti Bottom Navigation Bar.
    - Memiliki `Modifier.navigationBarsPadding().imePadding()` sehingga otomatis terangkat saat keyboard terbuka.
    - State: `enabled` hanya bila tanggal dan jam valid telah dipilih; `loading` menampilkan spinner saat pengiriman request berlangsung.
- **Alur & Integrasi Pengajuan Request (UI Layer):**
  - Pengguna mengajukan permohonan pertemuan berdasarkan pameran karya tanpa harus mengetahui ruangan privat saat form diisi (cukup memilih tanggal, jam, dan mengisi pesan opsional).
  - Saat "Send Request" ditekan dan berhasil, permintaan masuk status *Waiting* di Schedule pengirim dan status *Request* di Schedule pemilik karya (§8.4).
  - **Ruangan pertemuan (room) dan tautan virtual baru diterima oleh pemohon setelah permintaan disetujui (Approved)** oleh penyelenggara pameran. Tautan dan ruangan ini nantinya diakses melalui tombol "Join Room" pada kartu event Schedule berstatus Approved (§8.4).

### 8.7 Profile (Dark & Light Mode) `[OBSERVED: Wireframe profile.png, profile-1.png, profile-2.png]`
- **Cakupan Pengguna:** Mengimplementasikan varian **own profile `gra._.aph`** (profil pemilik akun yang sedang login). Varian other-user (`shaf.one`) tidak diimplementasikan.
- **Kerangka Halaman:** `AppScaffold` dengan Bottom Navigation Bar tetap diam di bawah (Tab 5 `Profile` aktif, avatar berlingkar border teks). Latar belakang kanvas mengikuti `VexTheme.colors.bg` (Dark `#1F1F1F`, Light `#FFFFFF`).
- **TopBar (`VexTopBar.Profile`):**
  - Tinggi 56 dp + status bar inset, latar menyatu dengan kanvas `colors.bg`. Tanpa garis pembatas bawah di Dark Mode `[OBSERVED]`.
  - Sisi Kiri: Ikon "+" (`ic_action_add`, 24 dp, warna `colors.text`, margin kiri 16–24 dp) → membuka alur Create Flow (New Post / New Exhibit §8.13/§8.14).
  - Sisi Tengah: Teks username `@username` / `gra._.aph`, `titleMedium` (18sp SemiBold 600, warna `colors.text`), diposisikan tepat di tengah horizontal.
  - Sisi Kanan: Ikon Menu Hamburger (`ic_action_menu`, 24 dp, warna `colors.text`, margin kanan 16–24 dp) → membuka halaman Settings (§8.8).
- **Struktur Konten Utama (Single Vertical Scroll via LazyVerticalGrid):**
  Seluruh konten atas dirangkai menggunakan `LazyVerticalGrid(columns = 3)` dengan item span penuh (`GridItemSpan(maxLineSpan)`) untuk header, sehingga scroll vertikal menyatu tanpa konflik (nested-scroll free) dengan grid poster di bawahnya.
  1. **Header Profil & Statistik (`ProfileHeader`):**
     - Sisi Kiri: `Avatar` bulat 96 dp (`AvatarSize.Lg` / `VexSize.avatarLg`), menampilkan inisial huruf kapital nama pengguna (fallback "G") berlatar `surfaceInput` bila belum ada foto karya pengguna.
     - Sisi Kanan: Nama lengkap pengguna (mis. "Terra Faqih Satria Madjid", `titleMedium` SemiBold, warna `colors.text`, mendukung hingga 2 baris).
     - Di bawah nama: Baris statistik karya (`StatBlock`):
       - Blok Posts: Angka tebal (`titleMedium` Bold 700, warna `colors.text`) + label `posts` (`bodyMedium`, warna `colors.textMuted`).
       - Blok Exhibits: Angka tebal (`titleMedium` Bold 700, warna `colors.text`) + label `exhibits` (`bodyMedium`, warna `colors.textMuted`).
  2. **Baris Tag Keahlian (Skills):**
     - Menampilkan maksimal 4 chip keahlian pertama yang terlihat (`VexSkillChip`) dengan latar belakang warna kategori masing-masing (§4.3 `CategoryColorMap`), teks kontras dinamis via `onCategoryColor()`.
     - Chip sisa: Jika terdapat lebih dari 4 keahlian, tampil chip penghitung `+N` (`VexCounterChip`, mis. "+2") berlatar `surfaceInput`.
     - **Interaktivitas Bottom Sheet:** Seluruh chip keahlian dan chip `+N` dapat diklik (`clickable`) untuk memunculkan `VexBottomSheet` berisi judul "Skills" (`titleLarge` SemiBold) dan `FlowRow` seluruh daftar keahlian lengkap pengguna.
  3. **Bio Pengguna:**
     - Deskripsi bebas pengguna (`bodyMedium`, warna `colors.text`), padding horizontal 24 dp.
  4. **Informasi Kontak:**
     - Email: Label "Email" (`bodyMedium`, warna `colors.textMuted`) + nilai alamat email pengguna (`bodyLarge`, warna `colors.text`).
     - Phone: Label "Phone" (`bodyMedium`, warna `colors.textMuted`) + nilai nomor telepon (`bodyLarge`, warna `colors.text`).
  5. **Tab Switcher Ikon (`VexIconTabs`):**
     - Terdiri dari 2 tab selebar layar (masing-masing weight 1f):
       - Tab 1 (Kiri): **Posts** (ikon galeri foto `ic_tab_posts_outline`/`filled`) — **default terpilih**.
       - Tab 2 (Kanan): **Exhibits** (ikon kubus 3D `ic_tab_exhibits_outline`/`filled`).
     - Tab Aktif: Ikon varian `_filled` warna `colors.text` + garis indikator bawah 1 dp warna `colors.text`.
     - Tab Nonaktif: Ikon varian `_outline` warna `colors.textMuted` + garis indikator bawah 1 dp warna `colors.border`.
  6. **Grid Karya / Poster (`MediaGrid` 3 Kolom):**
     - Menampilkan item karya pengguna sesuai tab yang aktif (karya post atau karya exhibit).
     - Format: **3 Kolom** dengan celah antar poster **2 dp** (`spacedBy(2.dp)` vertikal dan horizontal), tanpa sudut melengkung (radius 0 dp), rasio poster $\approx 0.7071$ (`VexAspect.poster`).
     - State Kosong (Empty State): Jika tidak ada karya pada tab aktif, menampilkan teks keterangan netral di tengah ("No posts yet" atau "No exhibits yet", `bodyMedium`, warna `colors.textMuted`).
- **Dukungan Dark & Light Mode:**
  Seluruh elemen UI mematuhi token semantik `VexTheme.colors` (latar, teks, border, surface). Warna chip keahlian konsisten bersumber dari `CategoryColorMap`.
- **Lokalisasi Bahasa:**
  Label sistem UI ("Posts", "Exhibits", "Email", "Phone", "Skills", "Menu", dll.) bersumber dari resource `strings.xml` (bahasa Inggris sebagai standar, `values-in` untuk terjemahan Indonesia). Konten yang diisi pengguna (nama, bio, email, telepon, nama skill) bersifat bebas tanpa lokalisasi sistem.

### 8.8 Settings and activity `[OBSERVED: Wireframe dark & light]`
- **Kerangka Halaman:** `SubPage` (TopBar `VexTopBar.Back` dengan judul "Settings and activity" + garis pemisah 1dp `border`). Latar belakang kanvas `#1F1F1F` (Dark) / `#FFFFFF` (Light). Scroll vertikal dengan padding bawah 32dp (`VexSpace.s8`).
- **Pengelompokan Menu (`VexSectionLabel` + `VexListRow`):**
  1. **Account:**
     - `ic_settings_profile` → Profile (§8.8.1)
     - `ic_settings_email` → Change email (§8.9)
     - `ic_settings_password` → Change password (§8.10)
     - `ic_settings_account_status` → Account status (§8.8.4)
     - `ic_settings_history` → History (§8.8.5)
  2. **Interface:**
     - `ic_settings_theme` → Theme (§8.8.2)
     - `ic_settings_language` → Language (§8.8.3)
  3. **Security & Others:**
     - `ic_settings_privacy` → Privacy policy (§8.10.1)
     - `ic_settings_help` → Help (§8.10.1)
  4. **Login:**
     - `ic_settings_logout` → Logout: item baris Danger (`isDanger = true`, teks & ikon warna `colors.danger`, tanpa chevron).
     - Menekan Logout memicu `AlertDialog` konfirmasi: Judul "Log Out", Pesan "Are you sure you want to log out of your account?", tombol "Cancel" (`colors.text`) dan "Log Out" (`colors.danger`). Jika dikonfirmasi, `SessionManager.clearSession()` dipanggil dan navigasi mereset tumpukan kembali ke Screen Login (`popUpTo(0)`).

#### 8.8.1 Profile Settings & Edit Profile `[OBSERVED: Wireframe Setting Profile]`
- **Mode Tampilan (View Mode):**
  - TopBar: `VexTopBar.Back` dengan judul **"Profile"** + trailing icon pensil (`ic_action_edit`, 22dp).
  - Avatar pengguna berukuran 96dp (`AvatarSize.Lg`) di tengah + teks link "New Photo" (`colors.primaryText`).
  - Formulir input dinonaktifkan (`enabled = false`) untuk Username, Name, Phone, Description (`VexTextFieldVariant.Boxed`).
  - Seksi "Interest": Chip kategori minat pengguna dalam format nonaktif.
- **Mode Edit (Edit Mode):**
  - Mengklik ikon pensil beralih ke Edit Mode.
  - TopBar: Judul berubah menjadi **"Edit profile"** + tombol teks aksi kanan **"Save changes"** (`colors.primaryText`, `titleSmall` SemiBold).
  - Seluruh field input (`Username`, `Name`, `Phone`, `Description`) menjadi aktif untuk disunting.
  - Chip kategori minat (`VexSelectableChip` dalam `FlowRow`) interaktif untuk memilih/menghapus kategori minat.
  - Menekan "Save changes" menyimpan data ke state lokal, memicu callback update profil, dan mengembalikan tampilan ke View Mode.

#### 8.8.2 Theme Settings `[OBSERVED: Wireframe Setting Theme]`
- **TopBar:** `VexTopBar.Back` dengan judul **"Theme"**.
- **Opsi Tema (`VexRadioRow`):**
  - **Light:** Mengatur tema aplikasi ke Light mode.
  - **Dark:** Mengatur tema aplikasi ke Dark mode (default).
- **Mekanisme Persistensi:**
  - Preferensi tema disimpan secara persisten di SharedPreferences lewat `ThemePreferences`.
  - Di-expose via `StateFlow<ThemeMode>` reaktif yang diobservasi oleh `MainActivity` dan `AppNavHost` (`collectAsStateWithLifecycle()`).
  - Perubahan tema langsung memperbarui seluruh tampilan aplikasi secara dinamis tanpa perlu restart aplikasi.

#### 8.8.3 Language Settings `[OBSERVED: Wireframe Setting Language]`
- **TopBar:** `VexTopBar.Back` dengan judul **"Language"**.
- **Opsi Bahasa (`VexCheckRow`):**
  - **English** (default sistem aplikasi).
  - **Bahasa Indonesia**.
- Terpilih ditandai dengan ikon centang kanan (`ic_action_check`, 20dp, warna `colors.text`).

#### 8.8.4 Account Status `[OBSERVED: Wireframe Setting Account Status]`
- **TopBar:** `VexTopBar.Back` dengan judul **"Account status"**.
- **Opsi Penonaktifan (`VexRadioRow`):**
  - Label: **"Disable Account"** (SemiBold).
  - Deskripsi: *"This action will deactivate your account until you log back in and reactivate it."* (`bodySmall`, `textMuted`).
  - Radio indicator ungu aktif ketika akun berstatus dinonaktifkan.

#### 8.8.5 History (Riwayat Pameran / Pertemuan) `[OBSERVED: Wireframe History]`
- **TopBar:** `VexTopBar.Back` dengan judul **"History"**.
- **Daftar Riwayat (`HistoryListItem`):**
  - Menampilkan daftar `LazyColumn` kartu riwayat.
  - Anatomi kartu: Banner thumbnail landscape 16:9 di sisi kiri (lebar 150dp, radius 4dp), tanggal pameran tebal (mis. "24th Sep 2026", `titleMedium`), jam ("02.00 PM", `bodyMedium`), dan tautan "See details" di kanan bawah (`bodySmall`, `colors.textMuted`).
- **Modal Detail (`VexBottomSheet`):**
  - Mengklik salah satu kartu memunculkan bottom sheet detail.
  - Menampilkan poster banner ukuran besar rasio 16:9 (`VexRadius.md`).
  - Teks deskripsi status request: *"Your request which is scheduled in **<Tanggal Lengkap>** at **<Jam>** has been finished."* dengan penegasan font tebal pada tanggal dan waktu.

### 8.9 Change email → Verify email `[OBSERVED: Wireframe Setting Email]`
- **Change Email Screen:**
  - TopBar: `VexTopBar.Back` dengan judul **"Change email"**.
  - Form field berlabel `Boxed`: **New email**, **Password**, **Confirm password**.
  - Teks pembantu (*helper*): *"You need to verify your new email for the change to be applied"* (12sp `bodySmall`, `colors.textMuted`).
  - Tombol CTA lengket di bawah (`VexBottomActionBar`): Tombol utama `Pill` **"Send OTP"**, aktif hanya bila seluruh field valid dan konfirmasi password cocok.
- **Verify Email Screen (`VerifyEmailScreen`):**
  - TopBar: `VexTopBar.Back` dengan judul **"Verify email"**.
  - Label: "OTP" (`titleMedium` SemiBold).
  - Input OTP: `OtpInput` **6 digit** (distandarisasi sesuai arsitektur backend Laravel REST API).
  - Bawah: Teks pertanyaan *"didn’t get your mail?"* dan aksi **"Resend"** (`VexButtonVariant.TextLink`) dengan hitung mundur cooldown 60 detik.
  - Saat 6 digit selesai diinput, verifikasi otomatis berhasil dan kembali ke Settings.

### 8.10 Change password `[OBSERVED: Wireframe Setting Password]`
- **TopBar:** `VexTopBar.Back` dengan judul **"Change password"**.
- **Form Field:** Field `VexPasswordField` (Boxed):
  - **Current password**
  - **New password**
  - **Confirm password**
- **Tombol CTA Bawah (`VexBottomActionBar`):**
  - Tombol utama `Pill` **"Send OTP"**, aktif ketika formulir terisi lengkap dan kedua kata sandi baru sesuai.
  - Menekan tombol memicu proses pengiriman token OTP dan kembali ke halaman Settings/Verify.

### 8.10.1 Privacy Policy & Help
- Halaman placeholder minimal (`SettingsPlaceholderScreen`) dengan `VexTopBar.Back` dan pesan status "Coming Soon" netral, memastikan navigasi bebas dead-link.

### 8.11 Forgot Password (Dark Mode) `[OBSERVED: Figma node 4372:689]`
- **Kerangka Halaman:** `SubPage` (Top bar dengan tombol kembali + form + tombol CTA menempel di bawah). Latar belakang kanvas `#1F1F1F`.
- **TopBar (`VexTopBar.Back`):**
  - Ikon kembali (panah kiri `ic_action_back`) di kiri (posisi $x \approx 22.5\text{ dp}$, warna putih `#FFFFFF`).
  - Judul: **"Forgot Password"**, warna `#FFFFFF`, ukuran 18–20sp (Elms Sans SemiBold 600 / Medium 500).
  - Garis pemisah bawah (*bottom divider*): posisi $y = 66\text{ dp}$, tebal 1 dp, warna `#313131` / `border`.
- **Form Input Email:**
  - Padding horizontal halaman: **25.5 dp** ($\approx 24\text{ dp}$ / `VexSpace.s6`).
  - **Label Field:** Teks **"Email"**, warna `#FFFFFF`, ukuran 14–16sp (Elms Sans Medium 500), posisi $y \approx 98\text{ dp} - 110\text{ dp}$ (jarak 32 dp di bawah garis pembatas top bar).
  - **Input Box (`VexTextField.Boxed`):**
    - Bentuk: Rounded Rectangle / `Boxed` (radius sudut $8 - 10\text{ dp}$ / `VexRadius.md`).
    - Dimensi: Lebar 362–364 dp (lebar penuh dikurangi padding 25.5 dp kiri & kanan), tinggi **48 dp** ($y = 122.5\text{ dp} - 170.5\text{ dp}$).
    - Jarak label ke input box: ~15 dp (`VexSpace.s4`).
    - Warna Latar: Abu-abu gelap `#363636` (`surfaceInput`).
    - Border / Outline: Garis tepi tipis 1 dp warna `#454545` (`borderInput`).
- **Tombol Kirim (CTA Bawah):**
  - Tombol **"Send OTP"** (Primary Button):
    - Bentuk: `Pill` (`RoundedCornerShape(full)` / 100 dp).
    - Dimensi: Lebar 374 dp (margin horizontal $\approx 20\text{ dp}$ kiri & kanan `[OBSERVED]`), tinggi $\approx 56 - 60\text{ dp}$.
    - Warna Latar: **`BrandPurple` (`#BA18F5`)**, teks "Send OTP" putih (`#FFFFFF`), `labelLarge` (16sp Medium 500), rata tengah.
    - Posisi: Slot bawah layar dengan `Modifier.navigationBarsPadding().imePadding()`, margin bawah ke tepi kanvas $\approx 25.5\text{ dp}$.
  - Alur Navigasi: Menekan "Send OTP" memvalidasi input email, memanggil endpoint `POST /password/forgot` pada backend Laravel, lalu menavigasi ke halaman verifikasi OTP (§8.9).

### 8.12 AI Assistant (Dark Mode) `[OBSERVED: 4 Wireframe AI Chat]`

Spesifikasi lengkap layar **AI Assistant / AI Chat** divalidasi langsung dari 4 file wireframe dark mode resmi (`D:\front-mobile\dark-mode\ai\`):
1. `Wireframe - AI Chat.png` (Empty State / Tampilan Awal)
2. `Wireframe - AI Chat-1.png` (Conversation State / Percakapan Aktif)
3. `Wireframe - AI Chat-2.png` (History Drawer / Laci Riwayat Percakapan)
4. `Wireframe - AI Chat-3.png` (Input Active / Pengetikan & Transisi Drawer)

Layar ini berjalan di bawah kerangka utama `AppScaffold` dengan tab bottom navigation ke-3 (`BottomNavTab.Ai`, ikon sparkle/bintang berkilau 4 sudut) aktif. Menggunakan arsitektur **MVI (Model-View-Intent)** reaktif dengan dukungan **Local Mockup Data** untuk preview offline serta terhubung langsung ke RESTful backend Laravel `POST /api/ai/tanya`.

---

#### 8.12.1 Anatomi Layar Utama & Header (`AiChatTopBar`)
- **Kanvas & Insets:**
  - Latar belakang: `#1F1F1F` (`VexTheme.colors.bg`).
  - Bagian atas: `Modifier.statusBarsPadding()`.
  - Bagian bawah: terikat `AppScaffold` dengan `BottomNav` tetap terlihat pada posisi default, beradaptasi saat keyboard terbuka via `imePadding()`.
- **Top Bar (`AiChatTopBar`):**
  - Tata letak: Baris horizontal (`Row`), tinggi 56dp, padding horizontal 20–24dp (`VexSpace.s5`), `Arrangement.SpaceBetween`, `Alignment.CenterVertically`.
  - **Sisi Kiri:** Tombol menu navigasi drawer (`ic_action_menu` / dua garis horizontal tak sama panjang, garis atas sedikit lebih panjang), ukuran ikon 24×24dp, warna putih (`text`), area sentuh minimal 48×48dp (`VexSize.minTouchTarget`). Mengetuk ikon ini membuka laci riwayat (`ChatHistoryDrawer`).
  - **Sisi Kanan:** Logo resmi VEX/VEN (`VexIcons.LogoVex`), tinggi 24dp, warna putih solid (`text`).
  - **Border:** Bersih dan menyatu dengan latar kanvas — **tanpa garis pembatas/divider bawah** `[OBSERVED]`.

---

#### 8.12.2 State Kosong (Empty State) `[OBSERVED: Wireframe - AI Chat.png]`
Kondisi saat sesi percakapan baru dibuka atau belum ada riwayat obrolan yang sedang aktif:
- **Watermark Sparkle Tengah-Atas:**
  - Ikon bintang berkilau 4 sudut berukuran besar (~140–160dp).
  - Posisi: Rata tengah horizontal, vertikal di sepertiga atas layar (`y ≈ 25–35%` kanvas).
  - Warna: Putih sangat redup dan samar dengan tingkat opasitas **±8%** (`Color.White.copy(alpha = 0.08f)`), memberikan aksen visual khas asisten AI tanpa mengalihkan perhatian dari input teks.
- **Kolom Input Pill Tengah Layar:**
  - Pada state kosong, kolom input utama (`AiChatInput`) **berada di tengah-tengah layar secara vertikal** (vertically centered), bukan melayang di dasar layar `[OBSERVED]`.
  - Margin horizontal 28–30dp dari tepi layar.
- **Label Identitas Layar:**
  - Terletak tepat di bawah kolom input tengah dengan jarak vertikal ~16dp (`VexSpace.s4`).
  - Teks: `"AI assistant"`.
  - Tipografi: `bodyLarge` (18sp Regular 400), warna putih (`text`), rata tengah (`TextAlign.Center`).
- **Transisi ke State Percakapan:**
  - Saat pengguna mengirimkan pesan pertama kali, tata letak bertransisi secara halus (*animated layout transition*): kolom input berpindah ke slot bawah layar di atas BottomNav, dan kanvas utama berubah menjadi daftar percakapan scrollable.

---

#### 8.12.3 State Percakapan (Conversation State) `[OBSERVED: Wireframe - AI Chat-1.png]`
Menampilkan percakapan bolak-balik antara pengguna dan asisten AI dalam wadah scrollable vertikal (`LazyColumn` dengan `rememberLazyListState()` yang otomatis auto-scroll ke pesan terbawah):

- **Pesan Pengguna (`ChatBubbleUser`):**
  - **Perataan:** Rata kanan (`Alignment.End`), margin kiri minimal 48dp (lebar maksimum balon $\approx 85 - 87\%$ dari lebar layar kanvas).
  - **Bentuk & Sudut (Speech Tail Bubble):**
    - `RoundedCornerShape`: Sudut kiri-atas 16dp, kiri-bawah 16dp, kanan-bawah 16dp, dan **sudut kanan-atas 2–4dp** (`VexRadius.xs` / hampir tajam) untuk menghasilkan efek ekor balon bicara arah keluar `[OBSERVED]`.
  - **Warna Latar:** Abu-abu gelap solid `#363636` (`VexTheme.colors.surfaceInput`).
  - **Padding Dalam:** Padding horizontal 18–20dp, padding vertikal 14–16dp.
  - **Tipografi:** Token `chatBody` (18sp, Regular 400, line-height 1.4), warna putih solid `#FFFFFF` (`text`), perataan rata kiri (`TextAlign.Start`).
  - **Contoh Teks Wireframe:** *"carikan saya 10 karya terkeren yang sesuai dengan kategori yang saya pilih"*.

- **Balasan Asisten AI (`ChatMessageAi`):**
  - **Desain Netral Tanpa Balon:** **Tidak menggunakan kartu, border, maupun background bubble** `[OBSERVED]`. Teks langsung dicetak di atas latar belakang kanvas `#1F1F1F`.
  - **Tata Letak & Spasi:** Memenuhi lebar layar penuh dengan padding horizontal 24dp (`VexSpace.s6`) dan jarak vertikal antar blok pesan 20–24dp.
  - **Tipografi:** Token `chatBody` (18sp, Regular 400, line-height 1.4), warna putih solid `#FFFFFF` (`text`).
  - **Perataan Paragraf:** Rata kiri-kanan (*justified*, `TextAlign.Justify`) `[OBSERVED]`. Jika panjang baris pendek membuat jarak antarkata terlalu renggang, sistem menyediakan opsi graceful fallback ke `TextAlign.Start`.
  - **Struktur Pesan Rekomendasi:**
    1. **Paragraf Pembuka:** Sambutan dan konfirmasi pencarian (contoh: *"Tentu! Aku sudah menemukan 10 karya yang paling menarik untuk kamu jelajahi. Berikut beberapa karya pilihan yang bisa kamu lihat:"*).
    2. **Daftar Karya Bernomor (1–10):** Format list markdown ringan bernomor rapi:
       - `1. [Nama Karya 1]`
       - `2. [Nama Karya 2]`
       - ...
       - `10. [Nama Karya 10]`
       - Item nama karya bersifat interaktif (*clickable text/link*): mengetuk nama karya dapat memicu navigasi langsung ke detail karya (`PostsFeedScreen` §8.5.5) atau ruang pameran 3D terkait.
    3. **Paragraf Penutup:** Teks arahan eksplorasi (contoh: *"Kamu bisa memilih salah satu karya untuk melihat detailnya atau langsung menjelajahi ruang 3D-nya."*).
  - **Indikator Loading AI:** Saat menunggu respon kecerdasan buatan, ditampilkan animasi pulsasi 3 titik berkedip halus atau shimmer baris teks.

---

#### 8.12.4 Kolom Input Bawah (`AiChatInput`) `[OBSERVED: Wireframe - AI Chat-1.png & AI Chat-3.png]`
- **Bentuk & Dimensi:**
  - Bentuk: `Pill` sempurna (`RoundedCornerShape(VexRadius.full)` / 100dp).
  - Tinggi: **56–60dp** (lebih tinggi dan ergonomis dibanding textfield biasa 48dp).
  - Margin: Margin horizontal 24–30dp, margin bawah 12–16dp di atas BottomNav (atau keyboard saat fokus).
- **Warna & Tampilan:**
  - Latar: Abu-abu gelap `#363636` (`VexTheme.colors.surfaceInput`).
  - Tanpa garis outline/border tebal saat idle; saat fokus outline opsional 1dp halus.
  - Placeholder: `"Type Something"` menggunakan tipografi `bodyLarge` (16–18sp), warna abu-abu redup `#808080` (`textMuted`).
  - Teks Ketikan: Warna putih solid `#FFFFFF` (`text`), single/multiline adaptif (maksimum 4 baris sebelum scroll internal aktif).
- **Aksi Kirim Dinamis:**
  - Pada sisi kanan dalam kolom input terdapat tombol kirim aksi (`IconButton` 36×36dp).
  - **Logika Visibilitas:** Ikon kirim muncul secara dinamis (fade-in / scale-in) hanya saat `inputText.isNotBlank()`.
  - Aset Ikon: Panah ke atas atau panah kirim sudut kanan-atas (20dp, warna putih atau `primaryText` `#D257FF`).
- **Penanganan Keyboard:**
  - Seluruh baris input dikaitkan dengan `Modifier.navigationBarsPadding().imePadding()`, memastikan input terangkat mulus persis di atas papan ketik virtual saat aktif mengetik `[OBSERVED: Wireframe - AI Chat-3.png]`.

---

#### 8.12.5 Drawer Riwayat Percakapan (`ChatHistoryDrawer`) `[OBSERVED: Wireframe - AI Chat-2.png]`
Menyediakan akses cepat ke riwayat sesi percakapan terdahulu serta pemicu sesi baru:
- **Geometri & Pembungkus:**
  - Menggunakan komponen `ModalNavigationDrawer` / `VexDrawer`.
  - Lebar laci: **300dp** dari tepi kiri layar (`VexSize.drawer`), tinggi layar penuh (`fillMaxHeight()`).
  - Latar belakang: Warna latar gelap `#1F1F1F` (`VexTheme.colors.bg`).
  - **Lapisan Scrim:** Sisa area layar di sebelah kanan (~112dp) dilapisi scrim gelap semi-transparan (`Color.Black.copy(alpha = 0.60f)`). Logo VEX pada TopBar dan foto avatar BottomNav tetap samar-samar terlihat di bawah lapisan scrim `[OBSERVED]`.
- **Anatomi Konten Drawer:**
  - **Aksi Sesi Baru ("New chat"):**
    - Baris horizontal interaktif di bagian paling atas drawer.
    - Ikon: Kotak dengan pena/pensil (`ic_action_new_chat` / `VexIcons.NewChat`, 24dp, warna putih `text`).
    - Label Teks: `"New chat"`, tipografi `titleMedium` (17–18sp Medium 500), warna putih solid (`text`), margin kiri 16dp.
    - Padding: Padding atas 48–56dp (`statusBarsPadding()`), padding horizontal 24dp, tinggi baris 48dp.
    - Aksi Tap: Mengosongkan daftar pesan, mereset state ke Empty State (§8.12.2), dan menutup drawer.
  - **Label Seksi ("Recents"):**
    - Label judul seksi riwayat: `"Recents"`.
    - Tipografi: `labelMedium` (14sp Regular 400), warna abu-abu redup `#808080` (`textMuted`).
    - Margin: Jarak atas 24dp dari tombol "New chat", margin bawah 12dp, padding horizontal 24dp.
  - **Daftar Riwayat Percakapan (`LazyColumn`):**
    - Menampilkan riwayat sesi perbincangan pengguna secara vertikal.
    - **Komponen Baris: `ChatHistoryItem`:**
      - Tinggi baris minimal: 48dp (`VexSize.minTouchTarget`).
      - Teks judul percakapan: gaya `bodyLarge` (16sp Regular 400), warna putih (`text`), single-line dengan pemotongan teks otomatis jika panjang (`maxLines = 1`, `overflow = TextOverflow.Ellipsis`).
      - Contoh judul dari wireframe:
        - *"Rekomendasi karya terkeren"*
        - *"Pameran yang sesuai den..."*
        - *"List karya milik Gra._.aph"*
      - Interaksi Tap: Memuat kembali seluruh riwayat pesan dari sesi tersebut ke layar utama dan menutup drawer secara otomatis.
- **Interaksi Penutupan Drawer:**
  - Drawer dapat ditutup sewaktu-waktu dengan:
    1. Melakukan swipe gesture ke arah kiri.
    2. Mengetuk area scrim di sebelah kanan.
    3. Menekan tombol Back sistem (gesture/tombol kembali fisik).

---

#### 8.12.6 Arsitektur MVI Frontend & Spesifikasi API Backend
Halaman AI Assistant dibangun menggunakan pola **Model-View-Intent (MVI)** reaktif, mendukung transisi mulus antara mode offline mockup dummy dan mode online live API:

- **Model Data Domain:**
  - `ChatMessage`: `id: String`, `sender: ChatSender (User | Ai)`, `text: String`, `timestamp: Long`, `status: MessageStatus (Sent | Sending | Error)`.
  - `ChatConversation`: `id: String`, `title: String`, `lastUpdated: Long`, `messages: List<ChatMessage>`.
- **State UI Layar (`AiChatUiState`):**
  - `screenState`: `AiScreenState (Empty | Conversation | Loading | Error)`.
  - `messages`: Daftar `ChatMessage` pada sesi yang sedang aktif.
  - `inputText`: String ketikan pada kolom input.
  - `isAiTyping`: Boolean penanda asisten AI sedang merumuskan jawaban (menampilkan indikator respons).
  - `chatHistory`: Daftar `ChatConversation` untuk disajikan pada Drawer Riwayat.
  - `currentChatId`: ID percakapan aktif (null jika sesi baru belum disimpan).
  - `isDrawerOpen`: Boolean penanda status keterbukaan drawer riwayat.
  - `errorMessage`: String pesan kesalahan jaringan / rate limit.
- **Dataset Mockup Mandiri (`AiChatDummyData.kt`):**
  - Menyediakan 3 sesi percakapan awal sesuai wireframe (Rekomendasi karya terkeren, Pameran yang sesuai, List karya Graaph) lengkap dengan daftar 10 karya mock agar UI dapat diuji tuntas tanpa ketergantungan koneksi server.

- **Kontrak Integrasi Backend Laravel RESTful API:**
  - **Endpoint:** `POST /api/ai/tanya`
  - **Middleware & Autentikasi:** `auth:sanctum`, `throttle:10,1` (maksimal 10 panggilan per menit per pengguna terautentikasi).
  - **Request Body (JSON):**
    ```json
    {
      "pertanyaan": "carikan saya 10 karya terkeren yang sesuai dengan kategori yang saya pilih"
    }
    ```
    *Validasi backend:* Wajib diisi, tipe string, maksimal 1000 karakter (`required|string|max:1000`).
  - **Response Berhasil (HTTP 200 OK):**
    ```json
    {
      "sukses": true,
      "pesan": "Berhasil",
      "data": {
        "jawaban": "Tentu! Aku sudah menemukan 10 karya yang paling menarik untuk kamu jelajahi. Berikut beberapa karya pilihan yang bisa kamu lihat: ..."
      }
    }
    ```
  - **Injeksi Konteks Otomatis oleh Backend:** Controller backend (`AIChatController.php`) secara otomatis mengekstrak 10 judul karya terbaru (`Karya::latest()`) dan 10 judul pameran terbaru (`Pameran::latest()`) untuk disisipkan sebagai system context prompt ke LLM eksternal.
  - **Penanganan Kegagalan (Fallback):**
    - Jika kuota tercapai atau server AI pihak ketiga mengalami kendala, backend mengembalikan pesan ramah: *"Maaf, layanan AI sedang tidak tersedia. Coba lagi nanti."*
    - Frontend menangkap error dan menyajikan umpan balik yang jelas dengan tombol aksi *Retry*.

### 8.13 Create Post `[OBSERVED: Figma node 4256:704 & 4256:861]` (CreateFlow, tanpa bottom nav)

Alur pembuatan postingan karya baru (`CreatePostScreen`), terbagi menjadi 2 langkah terintegrasi:

#### 8.13.1 Langkah 1: Media Picker (`Figma node 4256:704`)
- **TopBar (`VexTopBar.Create`):**
  - Sisi Kiri: Ikon silang / Close (`ic_action_close`, 24dp, warna `colors.text`) untuk membatalkan dan menutup alur.
  - Sisi Kiri-Tengah: Teks judul **"New Post"** (`titleMedium` SemiBold 600, warna `colors.text`).
  - Sisi Kanan: Aksi teks **"Next"** (`titleSmall` SemiBold 600, warna `colors.primaryText` / `#BA18F5`), selalu aktif untuk melangkah ke formulir detail.
- **Kanvas & Konten:**
  - **Pratinjau Media Terpilih (Besar):** Bagian atas menampilkan gambar karya yang sedang dipilih dalam rasio potret (~4:5), memenuhi lebar layar, dengan `ContentScale.Crop`.
  - **Label Seksi "Recents":** Teks judul seksi **"Recents"** (`titleMedium` SemiBold, warna `colors.text`), padding horizontal 24dp, atas 16dp, bawah 12dp.
  - **Grid Galeri Media (`MediaGrid`):** Grid 3 kolom, celah 2dp antar tile, rasio poster potret (`VexAspect.poster`). Mengetuk salah satu tile media langsung memperbarui pratinjau besar di bagian atas.
- **Pill Navigasi Mengambang (`SegmentedPill`):**
  - Komponen pill melayang di posisi tengah-bawah kanvas: `width: 245dp`, `height: 52dp`, radius `VexRadius.full` (100dp), berlatar `colors.surface` dengan garis tepi 1dp `colors.border` dan bayangan lembut.
  - Menampilkan dua opsi: **"Post"** (terpilih: teks tebal warna `colors.text`) dan **"Exhibit"** (tidak terpilih: teks warna `colors.textMuted`).
  - Mengetuk "Exhibit" mengarahkan pengguna ke alur pembuatan pameran virtual 3D (§8.14).

#### 8.13.2 Langkah 2: Details Form (`Figma node 4256:861`)
- **TopBar (`VexTopBar.Create`):**
  - Sisi Kiri: Ikon panah kembali (`ic_action_back`, 24dp, warna `colors.text`) untuk kembali ke Langkah 1 (Media Picker).
  - Sisi Kiri-Tengah: Teks judul **"New Post"** (`titleMedium` SemiBold 600, warna `colors.text`).
  - Sisi Kanan: Aksi teks **"Post"** (`titleSmall` SemiBold 600, warna `colors.primaryText`), berstatus *enabled* hanya bila field Title dan Category telah terisi valid.
- **Pita Pratinjau Media:**
  - Kontainer horizontal di bagian atas formulir: tinggi 260dp, lebar penuh, latar belakang `colors.surfaceInput` (`#E5E5E5` di Light Mode, `#363636` di Dark Mode).
  - Gambar karya terpilih ditempatkan presisi di tengah kontainer dengan `ContentScale.Fit` dan padding vertikal 12dp.
- **Formulir Metadata (Scrollable + Insets):**
  - Padding horizontal 24dp (`VexSpace.s6`), jarak vertikal antar field 16dp (`VexSpace.s4`), dilengkapi `navigationBarsPadding().imePadding()` agar formulir terangkat ergonomis saat keyboard aktif.
  1. **Title:**
     - Label: **"Title"** (`titleSmall` SemiBold).
     - Field: `VexTextField` varian `Boxed` (radius 8dp, fill `surfaceInput`, outline `borderInput`), placeholder *"type your title..."*.
  2. **Category:**
     - Label: **"Category"** (`titleSmall` SemiBold).
     - Field: `VexDropdown` varian `Boxed` dengan ikon `ic_action_chevron_down` di kanan. Menampilkan 14 kategori standar VEN (§4.3 & §8.1).
  3. **Product Link:**
     - Baris Header: Label **"Product Link"** di kiri + label keterangan **"optional"** di kanan (`bodyMedium`, warna `colors.textMuted`).
     - Field: `VexTextField` varian `Boxed`, placeholder *"https://google.com"*, `KeyboardType.Uri`.
  4. **Description:**
     - Label: **"Description"** (`titleSmall` SemiBold).
     - Field: `VexTextArea` varian `Boxed` multiline, tinggi minimal 175dp, placeholder *"type your description..."*.
- **Integrasi Navigasi & Aksi Submit:**
  - Menekan "Post" mengeksekusi penyimpanan karya dan memicu navigasi kembali ke Home/Profile.
  - Komponen terhubung penuh pada `AppNavHost` via rute `Screen.CreatePost`. Tombol "+" pada TopBar Home dan Profile secara langsung membuka alur ini.

### 8.14 Create Exhibit `[OBSERVED: Figma node 4256:704]`

Alur pembuatan ruang pameran virtual 3D baru (`CreateExhibitScreen`), terbagi menjadi:

#### 8.14.1 Langkah 1: Pemilihan Template Pameran (`New Exhibit` - Figma node 4256:704)
- **TopBar (`VexTopBar.Create`):**
  - Sisi Kiri: Ikon silang / Close (`ic_action_close`, 24dp, warna `colors.text`) untuk menutup alur.
  - Sisi Kiri-Tengah: Teks judul **"New Exhibit"** (`titleMedium` SemiBold 600, warna `colors.text`).
  - Sisi Kanan: Bersih tanpa tombol teks aksi.
- **Daftar Template Ruangan Isometrik 3D (`LazyColumn`):**
  - Padding horizontal 24dp (`VexSpace.s6`), jarak antar kartu 24dp, padding bawah 96dp agar kartu paling bawah tidak tertutup `SegmentedPill`.
  - Terdiri dari 4 pilihan template resmi:
    1. **Small:** Kapasitas **9 Post** (lebar ruang proporsional $0.95\times$)
    2. **Medium:** Kapasitas **14 Post** (lebar ruang proporsional $1.0\times$)
    3. **Big:** Kapasitas **21 Post** (lebar ruang proporsional $1.08\times$)
    4. **Event:** Kapasitas **60 Post** (lebar ruang proporsional $1.15\times$, terkonfirmasi resmi dari wireframe `node 4256-704`)
  - **Anatomi Kartu Template (`ExhibitTemplateCard`):**
    - Baris Header: Nama template di kiri (`titleMedium` SemiBold, warna `colors.text`) + Jumlah kapasitas di kanan (mis. *"9 Post"*, `bodyMedium`, warna `colors.textMuted`).
    - Kartu Visualisasi Isometrik (`IsometricRoomPreview`):
      - Tinggi 176dp, sudut membulat 12dp (`VexRadius.md`), kanvas latar belakang gelap `#262626` dengan garis grid isometrik 3D dan garis sumbu axis halus (merah/hijau).
      - Menampilkan model ruang kuboid 3D beratap terbuka dengan dinding ungu Brand VEN (`#6C3BE8`), lantai putih terang (`#F5F5FA`), panggung aksen tengah (`#E4E4ED`), dan efek pencahayaan/bevel isometrik presisi.
      - Klik interaktif pada kartu template membawa pengguna langsung masuk ke **Layar Editor 3D Canvas** (§8.14.2).
- **Pill Navigasi Mengambang (`SegmentedPill`):**
  - Mengambang di tengah bawah layar (`width: 245dp`, `height: 52dp`, radius `VexRadius.full` 100dp).
  - Opsi **"Exhibit"** aktif terpilih (teks tebal warna `colors.text`).
  - Opsi **"Post"** tidak terpilih: mengetuk "Post" menavigasikan pengguna kembali ke alur pembuatan postingan karya baru (`CreatePostScreen` §8.13).

#### 8.14.2 Langkah 2: Editor 3D Canvas & 4 Tool Bottom Sheets
- **Canvas 3D:** Memenuhi bagian atas kanvas, sudut bawah melengkung 28dp, latar abu gelap bergrid isometrik.
- **Kontrol Header & Alat Melayang:**
  - Panah kembali `←` di sisi kiri-atas kanvas.
  - Kolom vertikal 4 tombol alat melayang (`FloatingToolButton`, lingkaran 48dp, latar `surface`) di kanan-atas:
    1. **Aa (Details):** Membuka sheet metadata untuk mengisi Thumbnail banner 16:9, Title ("My Exhibition 1"), dan Description.
    2. **Panel (Karya):** Membuka sheet pemilihan karya (Poster | Video) dengan pagination dots dan penempatan slot panel.
    3. **Furnitur (Dekorasi):** Membuka sheet objek 3D (Light, Chair, Plant, Others) dengan penghitung objek.
    4. **Color (Warna Dinding):** Membuka sheet palet lingkaran warna untuk mengganti warna tema dinding ruangan secara real-time.
- **Indikator & Checklist Syarat Publish:**
  - Penghitung panel di kiri-bawah canvas: *"0/9 panels"* (berubah menjadi misal *"4/9 panels"* saat karya terpasang).
  - Di bawah canvas: checklist syarat penerbitan pameran:
    - `title & description` (merah ✕ saat kosong, hijau ✓ saat terisi)
    - `a panel` (merah ✕ saat belum ada panel terpasang, hijau ✓ jika minimal 1 panel terisi)
  - Tombol CTA **"Publish"** (Pill ungu BrandPurple, lebar penuh): Disabled saat syarat belum terpenuhi, aktif saat kedua syarat berwarna hijau.

### 8.15 Belum ada wireframe `[TODO]`
Account status, History, Theme, Language, Privacy policy, Help, detail/tampilan exhibit (mode melihat), halaman Join Room, pilih panel/furnitur/warna di editor.
> **Catatan v0.4.9:** *filter Explore* (§8.5.4) dan *detail karya / PostsFeed* (§8.5.5) kini **sudah dispesifikasikan** dari wireframe dark mode `explore/`, sehingga dikeluarkan dari daftar ini.

---

## 9. Aksesibilitas

- **Sentuh:** target interaktif ≥ 48×48dp (`minimumInteractiveComponentSize()` / `sizeIn`). Ikon 24dp tetap diberi area 48dp.
- **`contentDescription`** wajib untuk ikon tanpa teks (bottom nav, "+", menu, filter, tombol tool editor). Gambar karya: judul sebagai deskripsi; dekoratif → `null`.
- **Semantics:** `Role.Tab` + `selected` untuk tab; `Role.Button`; `heading()` untuk judul; OTP dengan `contentDescription` per kotak ("digit 1 dari 5").
- **Kontras:** lihat peringatan §4.2. Jangan memakai `textMuted` untuk informasi penting.
- **Fokus/keyboard:** urutan `imeAction`, indikator fokus terlihat (outline 2dp `primary`).
- **Font scaling:** uji sampai 200%; tanpa tinggi tetap pada teks.
- **Reduced motion:** hormati skala animator 0.
- **Sheet/Drawer:** bisa ditutup dengan tombol Back & gesture; fokus kembali ke pemicu.
- **Status warna:** jangan hanya warna (centang hijau selalu disertai ikon + teks; error disertai pesan).

---

## 10. Do & Don't

**Do**
- Biarkan gambar karya tampil bersih; UI netral.
- Satu tombol Primary utama per layar/sheet (pengecualian: pasangan Approve + Reject).
- Pill (`VexRadius.full`) untuk tombol, chip, search, input auth, bubble toggle.
- Skeleton untuk loading.

**Don't**
- Jangan gradient, glow, atau bayangan besar pada UI (hanya konten pengguna yang berwarna).
- Jangan menambah warna aksen UI kedua selain kategori & danger/success.
- Jangan campur `Pill` dan `Boxed` TextField dalam satu halaman.
- Jangan tambah label teks di bottom nav.
- Jangan membuat elemen melayang yang tidak ada di §8 .
- Jangan hardcode string; jangan hardcode warna/ukuran.

---

## 11. Alur Kerja AI: Mengerjakan Layar Baru

1. Baca `design.md` ini dan isi `ui/theme/`.
2. Temukan layar di §8. Jika belum ada, **berhenti dan minta wireframe/penjelasan**.
3. Cek `ui/components/` untuk komponen yang bisa dipakai ulang.
4. Bangun dengan token saja; pastikan dark (default) dan light benar, lengkap dengan `@Preview` keduanya.
5. Implementasikan `UiState` (Loading/Content/Empty/Error), string di `strings.xml`.
6. Uji edge-to-edge, keyboard (`imePadding`), font 200%, dan layar kecil (360dp).
7. Laporkan komponen bersama yang diubah beserta dampaknya.

---

## 12. Pertanyaan Terbuka

**Sudah diputuskan:**
- v0.2: sparkle = halaman AI; "+" = Create Flow; warna aksen kategori = placeholder; dark default dengan bg `#1F1F1F`.
- v0.3: `#252525` = latar bottom nav (dark); `primaryText` `#D257FF` disetujui; ikon & logo dari Figma sendiri (folder `res-icons`, akses lewat `VexIcons`); bagian 3D/Exhibit tetap masuk cakupan.
- v0.4: Sinkronisasi arsitektur repo `ven` (Laravel Backend REST API + Android Kotlin & Jetpack Compose).
- v0.4.1 (Figma node `3946:17`): Form Auth Dark Mode terverifikasi: field input & tombol Google/Register berlatar solid putih (`#FFFFFF`) bentuk pill (`100.dp`), judul 32sp bold, jarak antar field 32dp, divider 1dp abu-abu `#A5A5A5`, tombol Google bertuliskan "Register with Google" berlatar putih dengan logo Google multi-warna.
- v0.4.2 (Figma node `4372:689`): Forgot Password Dark Mode: SubPage, input Boxed `#363636` + border `#454545`, tombol CTA "Send OTP" pill BrandPurple `#BA18F5`.
- v0.4.3 (Analisis Wireframe Dark Mode `register/` & `home/`):
  1. Register Screen: 4 field input pill solid putih (Email, Username, Password, Confirm Password), CTA Register ungu, tombol Google "Login with Google", footer login putih.
  2. Interest (Onboarding): 14 kategori chip pill `#343333` (unselected dark), link "Maybe later" ungu di atas divider horizontal 1dp, tombol "Save" ungu.
  3. Home Screen: TopBar + dan logo VEX, Coming Soon carousel poster (~16:9, radius 8-12dp) dengan divider bawah 1dp, PostCard dengan username `@Graaph`, gambar post edge-to-edge tajam.
  4. Fungsi Ikon PostCard: Ikon kalender membuka alur **Request Meeting** (§8.6.1), ikon play membuka **3D Virtual Exhibition Room**.
  5. Request Meeting Screen: SubPage pameran terpilih, Banner 16:9, Kalender interaktif 7 kolom (tanggal aktif lingkaran BrandPurple `#BA18F5`), pemilih jam "Chose Time", dan CTA "Send Request" (`POST /undangan`).

- v0.4.7: Implementasi lengkap rangkaian layar **Settings & Activity Flow (§8.8–§8.11)**.
- v0.4.8 (Analisis Wireframe Dark Mode `schedule/`):
  1. Halaman Schedule (§8.4) divalidasi penuh dari 6 file wireframe dark mode (`D:\front-mobile\dark-mode\schedule\`).
  2. SearchBar pill (`surfaceInput`, tinggi 44–46dp, leading search icon, placeholder "Search").
  3. `VexTextTabs` (Approved | Waiting | Request) dengan indikator garis bawah 1dp putih solid dan label seimbang 1f.
  4. Tab Approved & Waiting: `EventListItem` dengan poster 16:9 (radius 8–12dp), tanggal, jam, dan tautan "See details".
  5. Tab Request (Inbox): `RequestListItem` dengan avatar 48dp, username bold, tanggal/jam ringkas kanan atas, serta pesan preview.
  6. `EventDetailSheet`: Banner pameran 16:9, teks status dengan tanggal & jam tebal via `AnnotatedString`, serta tombol CTA "Join Room" (Primary aktif untuk Approved, disabled untuk Waiting).
  7. `RequestDetailSheet`: Banner pameran 16:9, label jadwal "Requested for...", avatar + username, teks pesan lengkap, serta dua tombol sejajar Approve (Primary) dan Reject (Danger).
  8. Arsitektur Frontend MVI lokal dengan dummy data realistis (`ScheduleDummyData.kt`) tanpa dependensi jaringan backend.
- v0.4.9 (Analisis Wireframe Dark Mode `explore/`):
  1. Halaman Explore (§8.5) divalidasi penuh dari 8 file wireframe dark mode (`D:\front-mobile\dark-mode\explore\`).
  2. State Discovery: `ExploreTopBar` (SearchBar + tombol filter `ic_action_filter`) + `MediaGrid` 3 kolom dengan tile rentang 2 kolom.
  3. State Search History ("Recents"): `SearchHistoryItem` dua varian (query ikon jam & akun) dengan tombol hapus "✕".
  4. State Search Results: `VexTextTabs` 4 tab (`For you`/`Accounts`/`Posts`/`Exhibits`) dengan `SearchAccountRow`, `MediaGrid(columns = 2)`, dan `ExhibitBannerCard`.
  5. `CategoryFilterSheet`: header "Category" + aksi "Reset" ungu, `FlowRow` 14 chip kategori.
  6. `PostsFeedScreen` (§8.5.5): layar detail **scrollable** (`LazyColumn`) dengan `PostCard`, pinned top bar "Posts", dan auto-scroll ke `postId`.
  7. `MediaGrid` diperluas dengan parameter `columns` fleksibel (3 untuk Discovery, 2 untuk Search Results Posts).
  8. Arsitektur Frontend MVI lokal (`ExploreUiState.kt`, `ExploreDummyData.kt`) tanpa dependensi jaringan backend.

**Masih terbuka:**
1. Label teks tombol Google di Login/Register pada UX final: apakah diseragamkan "Continue with Google" atau mengikuti teks wireframe "Register with Google" (Login) dan "Login with Google" (Register).
2. Peran warna `surface` (sheet, drawer, tombol melayang, toggle) di dark: sementara disamakan dengan `#252525`. Ada nilai berbeda di Figma?
3. Bottom nav di light mode: warna dan ada/tidaknya garis atas.
4. Tampilan chip Interest saat terpilih: apakah background ungu solid (`#BA18F5`) dengan teks putih atau variasi outline?
5. Perilaku AI: lampiran gambar (tidak tampak pada wireframe v0.5.0; diasumsikan chat teks murni); batas panjang kueri teks 1000 karakter mengikuti batasan backend.
6. Aturan validasi password, cooldown OTP, batas ukuran/rasio gambar unggahan.
7. Perilaku scroll top bar Home/Explore; konten empty/error tiap halaman.
8. Library final (Navigation, Coil, DataStore, dll.) dan `minSdk`.
9. Engine 3D editor exhibit (library, gesture orbit/zoom), jumlah panel template Event, isi tool panel/furnitur/warna, dan bagaimana exhibit dilihat orang lain.
10. Standardisasi komponen `VexBottomActionBar` pada seluruh layar alur SubPage/CreateFlow lainnya (§8.9 Change Email, §8.10 Change Password, §8.11 Forgot Password, §8.13 Create Post, §8.14 Create Exhibit) agar pola CTA fixed seragam di seluruh aplikasi.

**Sudah terselesaikan (dari daftar terbuka sebelumnya):**
- ~~Perilaku AI (tombol kirim, transisi input tengah ke bawah, bubble ekor user, teks AI tanpa bubble, drawer riwayat 300dp)~~ → **Selesai v0.5.0**: lihat §8.12 (tervalidasi penuh dari 4 wireframe `D:\front-mobile\dark-mode\ai\`).
- ~~Isi spesifik bottom sheet Filter Explore dan navigasi tile Explore~~ → **Selesai v0.4.9**: lihat §8.5.4 (`CategoryFilterSheet`) & §8.5.5 (`PostsFeedScreen`).
- ~~Wireframe dark untuk halaman Explore untuk memvalidasi turunan token~~ → **Selesai v0.4.9**: divalidasi dari `D:\front-mobile\dark-mode\explore\` (§8.5).

---

## 13. Spesifikasi Lengkap: VEN Admin Web Panel (Desktop - Light & Dark Mode)

> **Observasi Mockup:** Divalidasi langsung dari 9 berkas desain antarmuka `D:\front-mobile\admin/`:
> - `Login (Desktop).png`: Halaman autentikasi administrator dengan kartu tengah berlatar kubus 3D melayang.
> - `Frame 13214.png` (Light) & `Frame 13218.png` (Dark): **Account Management Screen**.
> - `Frame 13215.png` (Light) & `Frame 13219.png` (Dark): **Post Moderation Screen**.
> - `Frame 13216.png` (Light) & `Frame 13220.png` (Dark): **Exhibition Management Screen**.
> - `Frame 13217.png` (Light) & `Frame 13221.png` (Dark): **Activity Log Screen**.
>
> **Aset Vektor Terkait:** Kubus isometrik login dari `D:\front-mobile\icon\xml_icon\balok-1.xml` dan `balok-2.xml`, serta ikon sidebar dari `profile.xml`, `post_fill.xml`, `exhibition_fill.xml`, `history.xml`, dan `logo_ven.xml`.

---

### 13.1 Konteks & Prinsip Desain Admin Web

1. **Target Platform:** Desktop Web Application dengan orientasi layar lebar (resolusi acuan `1920 × 1080` dan `1440 × 900`).
2. **Standarisasi Bahasa Antarmuka (100% English UI):**  
   Seluruh teks antarmuka yang dibuat atau disajikan oleh sistem pada seluruh layar admin **wajib menggunakan Bahasa Inggris** (mis. *"Sign In"*, *"Forgot Password?"*, *"Email Address"*, *"Account"*, *"Posts"*, *"Exhibitions"*, *"Activity Log"*, *"Suspend"*, *"Delete"*, *"View Details"*, dsb.), menggantikan teks placeholder bahasa Indonesia pada mockup asli.
3. **Dukungan Tema Ganda (Dual Theme: Dark & Light Mode):**  
   Seluruh layar dashboard dirancang memiliki pasangan tema Light dan Dark yang simetris dan dapat ditukar seketika melalui ikon switcher tema (Matahari/Bulan) pada sudut kanan Top Bar.
4. **Keputusan Desain Khusus (Penghapusan Elemen "Active"):**  
   - Dropdown filter status *"Active"* di samping search bar pada halaman Account **dihapus**.
   - Kolom dan teks label *"status Active"* pada setiap baris data pengguna **dihapus**.
   - Baris akun hanya menampilkan informasi esensial: Avatar, Username, Full Name, Jumlah Post, Jumlah Exhibition, Join Date, dan Tombol Aksi Suspend.
5. **Karakter Visual:**  
   Minimalis, sudut membulat halus (*pill-shaped* untuk search bar dan tombol aksi, *rounded-2xl* untuk card), kontras tinggi, dan tata letak data yang terstruktur rapi.

---

### 13.2 Desain Token & Palet Warna Semantik

| Token Semantik | Light Mode | Dark Mode | Fungsi & Keterangan |
|---|---|---|---|
| `admin.bg` | `#FFFFFF` | `#121212` | Latar belakang kanvas utama dashboard |
| `admin.surface` | `#FFFFFF` | `#1E1E20` | Latar kartu / item baris tabel data |
| `admin.surfaceInput` | `#EBEBEB` | `#27272A` | Latar search bar pill dan form input |
| `admin.border` | `#E2E8F0` | `#2E2E33` | Garis pemisah (*divider*) antar baris data (tinggi 1px) |
| `admin.text` | `#0F172A` | `#FFFFFF` | Teks judul halaman, nama pengguna, title pameran/karya |
| `admin.textMuted` | `#64748B` | `#94A3B8` | Label metadata kolom kecil (`username`, `post`, `created at`) |
| `admin.primary` | `#BA18F5` | `#BA18F5` | Warna aksen brand VEN, active indicator sidebar, link detail |
| `admin.primaryGradient`| `linear-gradient(90deg, #BA18F5, #D257FF)` | Gradasi tombol Sign In login desktop |
| `admin.danger` | `#FF1E1E` | `#FF1E1E` | Tombol aksi berbahaya: `Suspend` dan `Delete` (pill solid) |
| `admin.sidebarBg` | `#FFFFFF` | `#121212` | Latar sidebar kiri (terpisah garis vertikal tipis) |
| `admin.sidebarActive` | `#BA18F5` | `#BA18F5` | Kotak rounded ungu penanda menu aktif di sidebar |

---

### 13.3 Desain & Geometri Kubus 3D Floating (`balok-1.xml` & `balok-2.xml`)

Halaman Login Desktop memiliki ciri visual khas berupa **kubus isometrik putih melayang** di atas latar lingkaran gradasi ungu konsentris.

```
       [ balok-2 (kecil melayang kiri) ]       [ balok-2 (kecil melayang kanan) ]
                 \                                     /
           +-------------------------------------------------+
           |                                                 |
           |             [ CARD LOGIN DESKTOP ]              |
           |             - Email Address                     |
           |             - Password                          |
           |             - Forgot Password?                  |
           |             - [ Sign In Button ]                |
           |                                                 |
           +-------------------------------------------------+
                 /                                     \
    [ balok-1 (besar kiri bawah) ]          [ balok-1 (besar kanan bawah) ]
```

#### A. Analisis Anatomi Vektor
1. **`balok-1.xml` (Balok Isometrik Besar - Foreground):**
   - **Ukuran Asli:** `width: 592dp`, `height: 317dp`, `viewport: 592 × 317`.
   - **Facet Geometri:**
     - Facet Utama / Bidang Atas: `fillColor="#FFFFFFFF"` (Putih solid).
     - Facet Sisi Kiri: `fillColor="#FFFFFFFF"`.
     - Facet Sisi Kanan / Bayangan (Depth): `fillColor="#FFEBEBEB"` (Abu-abu terang penegas volume 3D).
     - Garis Tepi Kontur (Bevel Outline): `strokeColor="#FFEBEBEB"`, `strokeWidth="3px"`.
   - **Clipping Alamiah:** Sisi bawah balok terpotong rapi oleh batas kanvas viewport (`overflow: hidden`).
2. **`balok-2.xml` (Balok Isometrik Kecil - Midground):**
   - **Ukuran Asli:** `width: 201dp`, `height: 194dp`, `viewport: 201 × 194`.
   - **Facet Geometri:**
     - Facet Atas: `fillColor="#FFFFFFFF"`.
     - Facet Samping: `fillColor="#FFFFFFFF"`.
     - Facet Bayangan: `fillColor="#FFEBEBEB"`.
     - Outline: `strokeColor="#FFEBEBEB"`, `strokeWidth="3px"`.

#### B. Konversi Format Web (SVG)
Aset Android Vector ini dikonversi 1:1 menjadi berkas SVG standar web:
- `public/assets/balok-1.svg`: Menggunakan root tag `<svg viewBox="0 0 592 317" fill="none" xmlns="http://www.w3.org/2000/svg">`.
- `public/assets/balok-2.svg`: Menggunakan root tag `<svg viewBox="0 0 201 194" fill="none" xmlns="http://www.w3.org/2000/svg">`.

#### C. Tata Letak & Efek Floating pada Layar Login
- **Latar Belakang:** Lingkaran busur konsentris bertingkat dengan gradasi ungu:
  - Lingkaran Luar: `#C084FC` (opacity 30%)
  - Lingkaran Tengah: `#A855F7` (opacity 60%)
  - Lingkaran Dalam: `#8B5CF6` / `#7E22CE` (solid)
- **Koordinat 4 Kubus:**
  1. *Kiri Bawah:* `balok-1.svg`, posisi `bottom: -6%`, `left: -4%`, lebar `~420px`, z-index 10.
  2. *Kanan Bawah:* `balok-1.svg`, posisi `bottom: -6%`, `right: -4%`, lebar `~420px`, rotasi horizontal simetris (`transform: scaleX(-1)`), z-index 10.
  3. *Kiri Tengah:* `balok-2.svg`, posisi `top: 48%`, `left: 8%`, lebar `~150px`, rotasi `-8deg`, z-index 5.
  4. *Kanan Tengah:* `balok-2.svg`, posisi `top: 50%`, `right: 8%`, lebar `~150px`, rotasi `12deg`, z-index 5.
- **Animasi Subtle Floating (CSS Keyframes):** Balok diberi animasi gerak melayang halus naik-turun (`translateY(0)` ke `translateY(-12px)`) durasi 4–6 detik dengan timing `ease-in-out infinite alternate`.

---

### 13.4 Kerangka Shell Layout Admin (`AdminLayout`)

Seluruh halaman admin (setelah login) menggunakan layout dashboard terpadu:

```
+----+-------------------------------------------------------------------------------+
|LOGO|  [Page Title: e.g. Account]                         [Theme Toggle: Sun/Moon]  |
|    +-------------------------------------------------------------------------------+
|[1] |  [ Search Bar (Pill) ]                                                        |
|    |                                                                               |
|[2] |  ---------------------------------------------------------------------------  |
|    |  [ROW 1: Avatar | Username | Name | Post | Exhibition | Join Date | Suspend]   |
|[3] |  ---------------------------------------------------------------------------  |
|    |  [ROW 2: Avatar | Username | Name | Post | Exhibition | Join Date | Suspend]   |
|[4] |  ---------------------------------------------------------------------------  |
+----+-------------------------------------------------------------------------------+
```

1. **Sidebar Kiri (Fixed `w-20` / 80px):**
   - Border vertikal kanan 1px (`admin.border`).
   - Paling atas: Logo VEN ungu (`logo_ven.xml`).
   - 4 Ikon Navigasi Menu Vertikal (Icon-only, tanpa teks label):
     1. **Account:** Ikon profile lingkaran (`account_status.xml` / `profile.xml`).
     2. **Posts:** Ikon gambar/galeri persegi (`post_fill.xml`).
     3. **Exhibitions:** Ikon kubus 3D isometrik (`exhibition_fill.xml`).
     4. **Activity Log:** Ikon jam/riwayat melingkar (`history.xml`).
   - **Indikator Aktif:** Kotak persegi membulat warna ungu brand solid (`#BA18F5`, radius 14px, ukuran 44×44px) dengan ikon di dalamnya berwarna putih solid (`#FFFFFF`). Menu non-aktif menampilkan ikon warna netral (`#000000` di light mode, `#FFFFFF` di dark mode) dengan efek hover latar lembut.
2. **Top Bar Header:**
   - Tinggi 70px, menyatu dengan kanvas.
   - Sisi Kiri: Judul halaman aktif dalam teks tebal (22sp SemiBold, font sans-serif, warna `admin.text`).
   - Sisi Kanan: Tombol toggle tema (Matahari di Dark Mode untuk pindah ke Light, Bulan di Light Mode untuk pindah ke Dark).
3. **Area Konten Utama:**
   - Padding horizontal lega (32px–48px), scrollable vertikal.
   - Search bar pill di bagian atas konten dengan placeholder spesifik.

---

### 13.5 Katalog Spesifikasi Detail 5 Halaman Admin

#### 13.5.1 Halaman Login (`Login (Desktop).png`)
- **Struktur Halaman:** Full-screen tanpa sidebar, dipusatkan vertikal dan horizontal.
- **Card Kontainer Tengah:**
  - Lebar: `420px–460px`, latar putih solid `#FFFFFF`, sudut melengkung `rounded-3xl` (24px), bayangan lembut (`shadow-2xl`), padding 36px, z-index 20.
- **Elemen Form (100% English):**
  1. **Input Email:**
     - Label terintegrasi placeholder: `"Email"`.
     - Bentuk: Rounded rectangle (radius 10px), border 1px solid `#CBD5E1`, latar `#F1F5F9`, tinggi 48px, teks input 14px.
  2. **Input Password:**
     - Label terintegrasi placeholder: `"Password"`.
     - Fitur: Toggle tombol lihat/sembunyikan password di sisi kanan.
  3. **Tautan Lupa Sandi:**
     - Teks: `"Forgot Password?"`, rata kanan, warna `#334155`, ukuran 13px, efek hover underline.
  4. **Tombol Masuk (Primary CTA):**
     - Teks: **`"Sign In"`** (putih, bold 16px).
     - Bentuk: Rounded pill (`rounded-xl` / 12px), lebar penuh (100%), tinggi 48px.
     - Latar: Gradasi ungu-ke-magenta halus (`linear-gradient(90deg, #BA18F5, #D257FF)`).
     - State: Hover scale subtle (1.01), loading spinner saat request berlangsung, disabled saat email/password kosong.

---

#### 13.5.2 Halaman Account (`Frame 13214.png` [Light] & `Frame 13218.png` [Dark])
- **Judul TopBar:** `"Account"`
- **Search Bar:**
  - Bentuk pill (`rounded-full`), tinggi 44px, lebar 100% (atau max-w-4xl).
  - Latar: `#EBEBEB` (Light) / `#2A2A2D` (Dark).
  - Ikon kaca pembesar di sisi kiri + placeholder: `"Search by name, username, or email..."`.
- **Perubahan Spesifik Sesuai Arahan:**
  - ❌ **Dropdown filter "Active" di kanan atas search bar DIHAPUS.**
  - ❌ **Kolom dan teks label "status Active" pada baris data DIHAPUS.**
- **Struktur Baris Data Pengguna (`AccountRowItem`):**
  - Setiap baris dibatasi divider horizontal 1px (`admin.border`). Padding vertikal 16px.
  - Susunan kolom horizontal dari kiri ke kanan:
    1. **Avatar:** Foto profil lingkaran diameter 56px (`rounded-full object-cover`). Fallback avatar inisial jika null.
    2. **Username:** Label kecil atas `username` (12sp `admin.textMuted`) + nilai `@username` (15sp Medium `admin.text`).
    3. **Name:** Label kecil atas `name` (12sp `admin.textMuted`) + nilai nama lengkap (mis. *Terra Faqih Satria Madjid*, 15sp SemiBold `admin.text`).
    4. **Post:** Label kecil atas `post` (12sp `admin.textMuted`) + jumlah karya (mis. *2*, 15sp Medium `admin.text`).
    5. **Exhibition:** Label kecil atas `exhibition` (12sp `admin.textMuted`) + jumlah pameran (mis. *1*, 15sp Medium `admin.text`).
    6. **Join Date:** Label kecil atas `join date` (12sp `admin.textMuted`) + tanggal bergabung format `DD/MM/YY` (mis. *29/09/26*, 15sp Medium `admin.text`).
    7. **Tombol Aksi Suspend:**
       - Teks: **`"Suspend"`** (putih, SemiBold 14px).
       - Bentuk: Rounded pill (`rounded-full`), padding horizontal 24px, tinggi 38px.
       - Warna Latar: Merah solid `admin.danger` (`#FF1E1E`).
- **Modal Konfirmasi Suspend (`SuspendUserModal`):**
  - Terbuka saat tombol `"Suspend"` ditekan.
  - Berisi: Nama & email akun yang akan disanksi.
  - Form:
    - `Sanction Type`: Dropdown pilihan (`Suspended` / `Blocked`).
    - `Duration`: Pilihan durasi preset (`24 Hours`, `7 Days`, `30 Days`, atau `Permanent`).
    - `Reason`: Textarea alasan penjatuhan sanksi (wajib diisi, maksimal 255 karakter).
  - Aksi: Tombol `"Confirm Suspend"` (merah) dan `"Cancel"` (abu-abu).

---

#### 13.5.3 Halaman Post (`Frame 13215.png` [Light] & `Frame 13219.png` [Dark])
- **Judul TopBar:** `"Post"`
- **Search & Filter Bar:**
  - Baris horizontal: Search bar pill di sisi kiri + **Category Filter Dropdown** di sisi kanan.
  - Dropdown Kategori: Kustom popover pill (bukan native browser `<select>`), lebar tombol trigger dan panel popover menu diselaraskan presisi 100% sama panjang (`w-full sm:w-[260px]`), menampilkan kategori terpilih (mis. *"All Categories"*, *"Internet of Things"*), chevron panah yang berotasi mulus 180°, menu melayang dengan sudut melengkung `rounded-2xl`, bayangan `shadow-xl`, opsi berlatar `hover:bg-gray-100 dark:hover:bg-zinc-800`, indikator aktif ungu `#BA18F5` dengan ikon centang (*checkmark*), serta penutupan otomatis saat klik di luar area (*click-outside*).
- **Grid Moderasi Karya (2 Kolom):**
  - Layout 2 kolom grid responsif (`grid grid-cols-1 xl:grid-cols-2 gap-8`).
  - **Anatomi Kartu Karya (`ModerationPostCard`):**
    - Sisi Kiri: Gambar poster karya (format potret rasio 3:4, lebar ≈ 160px, tinggi ≈ 220px, sudut melengkung `rounded-xl`, `object-cover`).
    - Sisi Kanan:
      - Baris Atas: Title karya tebal (18sp Bold `admin.text`, mis. *"PBL EXPO"* atau *"Platform Batik AI"*).
      - Baris Kedua: Username pembuat karya (14sp `admin.textMuted`, mis. `@shaf.one` atau `@gra._.aph`).
      - Baris Ketiga: Cuplikan deskripsi karya (13sp `admin.text`, line-clamp 3, mis. *"Karya ini merupakan karya untuk PBL EXPO yang mana dapat menampilkan banyak karya para peserta lainnya"*).
      - Baris Bawah: Tombol aksi **`"Delete"`** (pill merah solid `#FF1E1E`, teks putih SemiBold 14px, padding horizontal 28px, tinggi 38px).
- **Dialog Konfirmasi Hapus Post (`DeleteConfirmModal`):**
  - Pop-up konfirmasi pencegahan: *"Are you sure you want to delete this artwork? This will remove the post from public feeds."*
  - Tombol: `"Delete Post"` (merah) & `"Cancel"`.

---

#### 13.5.4 Halaman Exhibition (`Frame 13216.png` [Light] & `Frame 13220.png` [Dark])
- **Judul TopBar:** `"Exhibition"`
- **Search Bar:**
  - Bentuk pill lebar penuh, placeholder: `"Search exhibition by title or owner..."`.
- **Daftar Pameran (List Rows):**
  - Setiap baris dibatasi divider 1px (`admin.border`), padding vertikal 18px.
  - Susunan kolom horizontal:
    1. **Banner Thumbnail:** Gambar banner pameran format landscape (rasio 16:9, lebar ≈ 160px, tinggi ≈ 90px, sudut melengkung `rounded-lg`, `object-cover`).
    2. **Title:** Label kecil `title` + nilai nama pameran (16sp SemiBold `admin.text`, mis. *"PBL EXPO"*, *"OSC"*).
    3. **Owner:** Label kecil `owner` + nilai username pemilik pameran (14sp `admin.text`, mis. `@gra._.aph`, `@shaf.one`).
    4. **Post:** Label kecil `post` + jumlah karya di dalam pameran (14sp `admin.text`, mis. *2*, *1*).
    5. **Created At:** Label kecil `created at` + format tanggal `DD/MM/YY` (14sp `admin.text`, mis. *29/09/26*).
    6. **Tombol Aksi Delete:**
       - Teks: **`"Delete"`** (putih SemiBold 14px).
       - Bentuk: Rounded pill (`rounded-full`), latar merah solid `#FF1E1E`, padding horizontal 28px, tinggi 38px.
- **Dialog Konfirmasi Hapus Pameran:**
  - Pop-up konfirmasi moderasi pameran sebelum mengeksekusi endpoint soft delete.

---

#### 13.5.5 Halaman Activity Log (`Frame 13217.png` [Light] & `Frame 13221.png` [Dark])
- **Judul TopBar:** `"Activity log"`
- **Search Bar:**
  - Bentuk pill, placeholder: `"Search activity logs..."`.
- **Daftar Log Audit (Activity Feed Rows):**
  - Setiap baris dibatasi divider horizontal 1px (`admin.border`), padding vertikal 16px.
  - Susunan kolom horizontal:
    1. **Deskripsi Aksi:** Teks lengkap tindakan admin (14sp Medium `admin.text`, mis. *"Admin 1 deleted artwork titled Value Exchange Network"* atau *"Admin 1 has suspended user Daffa for 24 hours"*).
    2. **Tanggal Eksekusi:** Format tanggal ringkas di sisi kanan (`29/09/26`, 14sp `admin.textMuted`).
    3. **Tombol Tautan Detail:**
       - Teks: **`"Detail"`** (warna ungu brand `admin.primary` `#BA18F5`, 14sp Medium, clickable).
- **Drawer / Modal Detail Aktivitas (`ActivityDetailModal`):**
  - Terbuka saat tautan `"Detail"` diklik.
  - Menampilkan metadata audit teknis:
    - `Log ID` & `Timestamp` lengkap (ISO 8601 / format lokal).
    - `Actor`: Admin Name, Admin ID, IP Address, dan User Agent.
    - `Action Target`: Resource type (`Pengguna`, `Karya`, `Pameran`), Resource UUID/ID, dan Action Type (`DELETE`, `SUSPEND`, `RESTORE`).
    - `Payload Snapshot`: Data nilai sebelum dan sesudah perubahan (*Audit Diff JSON*).

---

### 13.6 Pemetaan Kontrak Backend Laravel API (`/api/admin/*`)

Frontend web admin mengonsumsi endpoint yang telah tersedia pada backend Laravel (`backend-api/backend/routes/api.php`) dengan otentikasi Bearer Token Sanctum dan middleware `HanyaAdmin`:

| Modul Layar Admin | Metode HTTP | Endpoint Backend | Parameter & Payload | Keterangan |
|---|---|---|---|---|
| **Auth** | `POST` | `/api/auth/masuk` | `{ "email": string, "kata_sandi": string }` | Autentikasi admin, memperoleh token Sanctum |
| **Auth** | `POST` | `/api/auth/keluar` | Header `Authorization: Bearer <token>` | Logout sesi administrator |
| **Account** | `GET` | `/api/admin/pengguna` | `?cari={query}&page={page}` | Mengambil daftar pengguna terpaginasi (24 item/halaman) |
| **Account** | `GET` | `/api/admin/pengguna/{id}` | UUID pengguna | Detail akun lengkap beserta riwayat sanksi |
| **Account** | `POST` | `/api/admin/pengguna/{id}/sanksi` | `{ "jenis_sanksi": "ditangguhkan"\|"diblokir", "alasan": string, "mulai": date, "permanen": bool, "berakhir": date? }` | Eksekusi suspend pengguna |
| **Account** | `DELETE`| `/api/admin/sanksi/{sanksiId}` | ID sanksi | Mencabut penangguhan / sanksi akun |
| **Post** | `GET` | `/api/admin/karya` | `?cari={query}&kategori={kategori}&page={page}` | Mengambil daftar karya terpaginasi untuk moderasi |
| **Post** | `DELETE`| `/api/admin/karya/{karyaUuid}` | UUID karya | Menghapus (soft delete) karya pelanggar kebijakan |
| **Post** | `POST` | `/api/admin/karya/{karyaUuid}/pulihkan` | UUID karya | Memulihkan kembali karya yang telah dihapus |
| **Exhibition**| `GET` | `/api/admin/pameran` | `?cari={query}&page={page}` | Mengambil daftar pameran virtual |
| **Exhibition**| `DELETE`| `/api/admin/pameran/{pameranId}`| ID integer pameran | Menghapus pameran virtual |
| **Exhibition**| `POST` | `/api/admin/pameran/{pameranId}/pulihkan`| ID pameran | Memulihkan pameran virtual |
| **Activity Log**| `GET`| `/api/admin/log/aktivitas` | `?cari={query}&page={page}` | Mengambil daftar riwayat aksi admin |
| **Activity Log**| `GET`| `/api/admin/log/aktivitas/{id}` | ID log | Detail audit trail teknis tindakan admin |

---

### 13.7 Arsitektur Berkas & Struktur Folder Admin Web Panel (Blade + Vite + Tailwind CSS v4)

Seluruh antarmuka website admin diorganisasikan langsung di dalam backend Laravel (`backend-api/backend/`):

📂 **`C:\laragon\www\ven\backend-api\backend/`**

```text
backend-api/backend/
├── public/
│   └── assets/admin/
│       ├── balok-1.svg               # Kubus 3D isometrik besar foreground (dari balok-1.xml)
│       ├── balok-2.svg               # Kubus 3D isometrik kecil melayang (dari balok-2.xml)
│       ├── logo-ven.svg              # Logo VEN ungu brand (dari logo_ven.xml)
│       ├── icon-account.svg          # Ikon menu navigasi Account
│       ├── icon-post.svg             # Ikon menu navigasi Post
│       ├── icon-exhibition.svg       # Ikon menu navigasi Exhibition
│       ├── icon-history.svg          # Ikon menu navigasi Activity Log
│       ├── icon-search.svg           # Ikon leading search bar
│       └── icon-theme.svg            # Ikon switcher tema Light / Dark
│
├── resources/
│   ├── css/
│   │   └── app.css                   # Tailwind CSS v4 + @variant dark + keyframe animasi balok
│   ├── js/
│   │   └── app.js                    # Inisialisasi Vite frontend
│   └── views/
│       └── admin/                    # KUMPULAN BLADE VIEWS ADMIN
│           ├── layouts/
│           │   ├── app.blade.php     # Layout induk: Sidebar w-20 fixed, Topbar h-20, & container konten
│           │   ├── sidebar.blade.php # 4 menu ikon (Account, Post, Exhibition, Log) dengan active purple pill
│           │   └── topbar.blade.php  # Judul dinamis + tombol switcher Sun/Moon + logout
│           ├── auth/
│           │   └── login.blade.php   # Login desktop (lingkaran gradasi ungu + floating balok-1 & balok-2)
│           ├── accounts/
│           │   └── index.blade.php   # Manajemen Akun (Active filter & status DIHAPUS, Suspend button + Modal)
│           ├── posts/
│           │   └── index.blade.php   # Moderasi Karya (Grid responsif 2-kolom, filter kategori, Delete + Modal)
│           ├── exhibitions/
│           │   └── index.blade.php   # Pameran Virtual (List rasio 16:9, Delete + Modal)
│           └── logs/
│               └── index.blade.php   # Riwayat Log Aktivitas (Audit trail feed, Detail purple link + Modal)
│
├── app/Http/Controllers/Admin/Web/
│   ├── AdminAuthController.php       # Controller login/logout sesi web admin
│   ├── AdminAccountController.php    # Controller akun (query Pengguna + suspend sanksi + fallback)
│   ├── AdminPostController.php       # Controller moderasi karya (query Karya + delete + fallback)
│   ├── AdminExhibitionController.php # Controller pameran (query Pameran + delete + fallback)
│   └── AdminLogController.php        # Controller riwayat log (query LogAktivitas + audit + fallback)
│
├── routes/
│   ├── web.php                       # Rute web portal /admin/* (login, accounts, posts, exhibitions, logs)
│   └── api.php                       # Rute REST API /api/admin/* untuk konsumsi headless client
├── package.json                      # Tailwind CSS v4, Vite 8, laravel-vite-plugin
└── vite.config.js                    # Konfigurasi bundler Vite
```

---

### 13.8 Spesifikasi Perbaikan UI: Clearance, Density, & Pagination Kustom

Untuk mengoptimalkan ergonomi dan estetika desktop admin panel, ditetapkan aturan baku berikut:

1. **Clearance Layout & Topbar Offset**:
   - Komponen Topbar memiliki tinggi `h-20` (80px) dengan sifat `sticky top-0 z-20` dan efek `backdrop-blur-md`.
   - Kontainer `<main>` **wajib** menyertakan padding atas `pt-8` (32px) agar seluruh elemen search bar tidak berbenturan atau terpotong oleh batas bawah header saat di-scroll.
2. **Kerapatan Baris List (Compact Row Density)**:
   - Baris daftar pada layar Account, Exhibition, dan Post disetel ke **`py-3.5`** (14px padding vertikal) dengan avatar/thumbnail ramping (`w-11 h-11` untuk profil pengguna, `150×75px` untuk pameran 16:9).
   - Layar Activity Log disetel ke **`py-3`** (12px padding vertikal) dengan teks audit trail `text-sm`.
   - Tombol aksi primer (*Suspend* dan *Delete*) menggunakan tinggi standar `h-9 px-6 rounded-full text-xs font-semibold`.
3. **Komponen Pagination Kustom (`admin.layouts.pagination`)**:
   - Menyajikan informasi rentang data dinamis: *"Showing X to Y of Z results"*.
   - Tombol navigasi Previous (`<`) dan Next (`>`) berbentuk pill sirkular (`w-9 h-9 rounded-full`) dengan efek state disabled transparan saat berada di batas halaman awal/akhir.
   - Indikator halaman aktif disorot dengan warna aksen ungu brand VEN (`bg-[#BA18F5] text-white shadow-md shadow-purple-500/25`), sedangkan halaman lain interaktif dengan hover background ringan.
   - Parameter limit halaman disetel default `4` data per-halaman pada Account, Post (2×2 grid), dan Exhibition, serta `5` pada Activity Log.

---

## Changelog
- **0.6.9**: Penyelarasan Total Tampilan & Alur **Create Exhibition Editor 3D** sesuai Wireframe Resmi Figma:
  - **Layar Utama Editor (Main Screen)**:
    - Kanvas 3D isometrik dengan tombol Back di kiri-atas dan 4 tombol melayang di kanan-atas (`Aa`, Panel, Sofa, Warna).
    - Badge counter pill di kiri-bawah kanvas: `[ic_panel] X/9 panels` (misal `0/9 panels` atau `4/9 panels`).
    - Panel bawah docked: Checklist 2 kriteria (`title & description` dan `a panel`) dengan status lingkaran teks merah ✕ atau hijau ✓ serta tombol CTA "Publish" yang otomatis aktif (ungu solid) saat kedua kriteria terpenuhi.
  - **Layar Details (`Aa`)**:
    - Layar penuh dengan TopBar: judul "Details" di kiri dan tombol Centang (✓) di kanan-atas untuk konfirmasi simpan dan kembali ke Layar Utama.
    - Pratinjau banner 16:9 (`poster_2.webp` PBL Expo 2026 default), input Boxed Title ("My Exhibition 1"), dan textarea Description multiline ("type your description...").
  - **Mode Panel Karya (`Panel`)**:
    - Kanvas 3D tetap terlihat di area atas dengan tombol Centang (✓) di kanan-atas untuk kembali ke Layar Utama.
    - Panel bawah docked dengan 2 tab ("Poster" | "Video") bergaris bawah aktif, 3 kartu karya rounded square (aspect ratio 1:1) dengan aksi instan tap untuk toggle penempatan karya pada slot panel ruangan, serta paginasi titik (• • •).
  - **Mode Furnitur (`Sofa`)**:
    - Kanvas 3D dengan counter `[ic_furniture] X/9 objects` dan tombol Centang (✓) di kanan-atas.
    - Panel bawah docked menyediakan 4 tab ("Light", "Chair", "Plant", "Other"), 3 kartu objek dekorasi rounded square, dan paginasi titik (• • •).
  - **Mode Warna Dinding (`Color`)**:
    - Kanvas 3D merender perubahan warna dinding secara real-time dengan tombol Centang (✓) di kanan-atas.
    - Panel bawah docked menampilkan judul "Color" dan palet 12 lingkaran warna terbagi presisi dalam 2 baris x 6 kolom (Row 1: White, Black, Coral, Cyan/Mint, Yellow, Purple; Row 2: Light Grey, Dark Grey, Hot Pink, Soft Mint, Pale Yellow, Lavender).
- **0.6.8**: Integrasi Penuh **Alur Pembuatan Konten Post & Exhibit 3D (§8.13 & §8.14)** serta Penghubungan Titik Interaksi 3D Aplikasi:
  - **Komponen `CreateOptionsBottomSheet` Terpadu**:
    - Membangun modal bottom sheet kustom (`CreateOptionsBottomSheet`) dengan 2 opsi pilihan pembuatan konten:
      1. **New Post**: Ikon grid postingan ungu VEN (`ic_tab_posts_filled`), judul *"New Post"*, deskripsi *"Share artwork, photos, or designs to the feed"*, navigasi langsung ke `Screen.CreatePost`.
      2. **New Exhibit (3D)**: Ikon kubus 3D oranye (`ic_tab_exhibits_filled`), judul *"New Exhibit (3D)"*, deskripsi *"Build an interactive 3D virtual exhibition room"*, navigasi langsung ke `Screen.CreateExhibit`.
    - Mengintegrasikan bottom sheet ini pada tombol "+" (`VexTopBar.Home` dan `VexTopBar.Profile`), sehingga pengguna dari Home maupun Profil dapat langsung memilih membuat postingan maupun ruang pameran 3D.
  - **Penghubungan Seluruh Titik Interaksi 3D (3D Touchpoints)**:
    - **Home Screen**:
      - Ikon tombol 3D (`onView3dClick`) pada setiap kartu karya `PostCard` kini langsung membuka ruang pameran 3D virtual (`Screen.CreateExhibitEditor`).
      - Ketukan banner pameran (`onBannerClick`) pada carousel pameran kini langsung membuka ruang 3D terkait.
    - **PostsFeed Screen (Explore)**:
      - Ikon 3D (`onView3dClick`) pada detail karya kini terhubung ke `Screen.CreateExhibitEditor`.
    - **Profile Screen**:
      - Pada tab `Exhibits`, mengetuk item kartu pameran di grid langsung membuka ruang pameran 3D editor/viewer (`Screen.CreateExhibitEditor`).
      - Pada tab `Posts`, mengetuk karya membuka `Screen.PostsFeed`.
    - **Explore Screen**:
      - Pada tab `Exhibits`, mengetuk banner pameran `ExhibitBannerCard` kini langsung membuka ruang pameran 3D (`Screen.CreateExhibitEditor`).
- **0.6.7**: Penyempurnaan Ergonomi & Interaktivitas **Alur Create Exhibit (§8.14)**:
  - **Sistem `BackHandler` Bertingkat di Layar Editor 3D**:
    - Menangani gesture / tombol back hardware Android secara cerdas: jika ada bottom sheet yang sedang terbuka (Details, Panels, Furniture, atau Color), menekan back akan menutup sheet tersebut terlebih dahulu; jika seluruh sheet tertutup, barulah menavigasi kembali ke layar pemilihan template.
  - **Checklist Persyaratan Click-to-Open Interaktif**:
    - Baris checklist persyaratan penerbitan di bawah kanvas kini dapat diketuk langsung (`clickable` dengan ripple):
      - Mengetuk baris `title & description` langsung membuka `ExhibitDetailsBottomSheet`.
      - Mengetuk baris `a panel` langsung membuka `ExhibitPanelsBottomSheet`.
  - **Integrasi Aset Karya Riil pada Panel**:
    - Menghubungkan library karya seni sampel dengan drawable lokal (`poster_1.webp`, `poster_2.webp`, `poster_3.webp`), sehingga kartu karya pada bottom sheet panel langsung menampilkan thumbnail visual karya asli.
- **0.6.6**: Penyelarasan Penuh **Tema Terang & Gelap (Dual Theming / Light & Dark Mode)** untuk Seluruh Rangkaian Create Flow:
  - **Pratinjau Studio Isometrik 3D Adaptif (`IsometricRoomPreview` & `Exhibit3dCanvas`)**:
    - Mode Gelap: Kanvas studio gelap `#262626` / `#222226` dengan garis grid isometrik `#333333` dan lantai panggung putih bersih.
    - Mode Terang: Kanvas studio terang elegan `#EFEFF4` dengan garis grid halus `#DCDCE5`, lantai dalam putih cerah, dan kontras bevel yang pas.
  - **Elemen Kontrol & Alat Melayang**:
    - Tombol Back lingkaran adaptif: transparan gelap berikon putih di Mode Gelap, dan `surface` putih dengan border halus serta ikon gelap di Mode Terang.
    - Floating pill penghitung panel (*"0/9 panels"*): latar transparan gelap berikon putih di Mode Gelap, dan latar `surface` putih ber-border dengan teks gelap di Mode Terang.
    - Floating tool buttons (Aa, Panel, Furniture, Color): latar `surface` dengan shadow dan elevasi konsisten di kedua tema.
  - **Checklist Penerbitan**:
    - Menggunakan token warna semantik sistem `colors.success` (hijau) dan `colors.danger` (merah), serta teks dinamis `colors.text` dan `colors.textMuted`.
  - **4 Tool Bottom Sheets**:
    - Penyelarasan penuh `ExhibitDetailsBottomSheet`, `ExhibitPanelsBottomSheet`, `ExhibitFurnitureBottomSheet`, dan `ExhibitColorBottomSheet` menggunakan container semantik `colors.surface`, `colors.surfaceInput`, dan `colors.border`.
    - Pilihan ring aktif pada swatches warna dinding beradaptasi putih di Mode Gelap dan hitam di Mode Terang.
  - **Kelengkapan `@Preview`**:
    - Menambahkan `@Preview` Dark dan Light lengkap untuk setiap komponen dan bottom sheet agar dapat diverifikasi langsung di Compose Preview Android Studio.
- **0.6.5**: Implementasi Lengkap **Alur Pembuatan Ruang Pameran Virtual (Create Exhibit: Template Picker & 3D Canvas Editor) (§8.14)**:
  - **Langkah 1 (Template Picker - New Exhibit)**:
    - Membangun antarmuka pemilihan template ruang beratap terbuka berdasarkan wireframe Figma `node 4256:704`.
    - 4 ukuran resmi: **Small (9 Post)**, **Medium (14 Post)**, **Big (21 Post)**, dan **Event (60 Post)**.
    - Komponen pratinjau isometrik 3D ruang kuboid beratap terbuka dengan lantai dalam bertekstur panggung, dinding beraksen ungu Brand, latar grid gelap, dan garis sumbu axis halus (`IsometricRoomPreview`).
    - Sinkronisasi `SegmentedPill` melayang di bagian bawah dengan transisi bolak-balik antara Post dan Exhibit.
  - **Langkah 2 (3D Canvas Editor & 4 Tool Bottom Sheets)**:
    - Kanvas isometrik 3D interaktif (`Exhibit3dCanvas`) memenuhi bagian atas layar, sudut bawah melengkung 28dp (`VexRadius.xl`), rendering dinding sesuai warna tema, visualisasi panel karya terpasang pada dinding, tombol panah kembali di kiri-atas, dan indikator penghitung panel `"X/N panels"` di kiri-bawah.
    - Kolom 4 tombol alat melayang vertikal di kanan-atas kanvas (`FloatingToolButton`, 48dp lingkaran, latar `surface`):
      1. **Aa (Details)**: Membuka `ExhibitDetailsBottomSheet` untuk upload banner cover 16:9, input judul pameran, dan deskripsi pameran.
      2. **Panel (Karya)**: Membuka `ExhibitPanelsBottomSheet` untuk memilih slot panel ruangan (carousel slot) dan menugaskan postingan/karya seni pengguna.
      3. **Furnitur (Dekorasi)**: Membuka `ExhibitFurnitureBottomSheet` untuk menambah atau mengurangi objek dekorasi 3D (Spotlight, Lounge Chair, Indoor Plant, Gallery Bench) dengan stepper dinamis.
      4. **Color (Warna Dinding)**: Membuka `ExhibitColorBottomSheet` dengan palet 6 warna ruangan terkurasi (VEX Purple, Midnight Blue, Emerald Teal, Crimson Red, Studio Charcoal, Warm Ochre) untuk pengubahan warna secara langsung.
  - **Sistem Validasi & Checklist Penerbitan (Publish)**:
    - Komponen `ExhibitPublishChecklist` di bawah kanvas dengan 2 syarat mandiri: `title & description` dan `a panel` (indikator hijau ✓ saat terpenuhi dan merah ✕ saat belum).
    - Tombol CTA "Publish" dengan status disabled saat syarat belum lengkap dan otomatis aktif saat kedua syarat hijau.
  - **Arsitektur Navigasi & Token**:
    - Mendaftarkan rute `Screen.CreateExhibit` dan `Screen.CreateExhibitEditor` pada `AppNavHost`.
    - Mendaftarkan vektor ikon `ic_panel.xml` dan `ic_furniture.xml` ke dalam `VexIcons`.
    - Mengintegrasikan seluruh teks ke dalam `res/values/strings.xml` dan `res/values-in/strings.xml`.
- **0.6.3**: Implementasi Lengkap **Alur Create Post (Langkah 1: Media Picker & Langkah 2: Details Form) (§8.13)**:
  - **Observasi Desain Figma**: Mengintegrasikan rancangan langsung dari node `4256:704` (Langkah 1: Media Picker) dan `4256:861` (Langkah 2: Details Form).
  - **Komponen SegmentedPill**: Membangun `SegmentedPill` melayang di tengah bawah kanvas (`245 × 52dp`, radius `VexRadius.full`) dengan toggle "Post" & "Exhibit".
  - **Komponen Form Kustom**: Mengimplementasikan `VexDropdown` (Boxed + Chevron Down untuk 14 kategori standar VEN), `VexTextArea` (Boxed multiline min 175dp untuk Description), dan varian `VexTopBar.Create` (dengan ikon Close/Back di kiri dan aksi teks Next/Post ungu di kanan).
  - **Langkah 1 (Media Picker)**: Pratinjau besar media terpilih, label seksi "Recents", grid 3 kolom (`LazyVerticalGrid`), dan overlay sorotan aktif.
  - **Langkah 2 (Details Form)**: Pita pratinjau landscape centered berlatar `surfaceInput`, input Title Boxed, dropdown Category, baris Product Link + label "optional", dan input Description.
  - **Navigasi Terintegrasi**: Menghubungkan rute `Screen.CreatePost` di `AppNavHost` dan menyambungkan tombol "+" pada TopBar `HomeScreen` serta `ProfileScreen`.
- **0.6.2**: Perbaikan Ergonomi & Layout **VEN Admin Web Panel (§13.8)**:
  - **Custom Popover Category Dropdown**: Mengganti elemen native browser `<select>` pada layar Post dengan custom pill popover dropdown (`rounded-2xl`, `shadow-xl`, checkmark centang aktif ungu `#BA18F5`, chevron rotasi 180°, dan event click-outside / ESC close) agar konsisten 100% dengan bahasa desain VEN.
  - **Layout Safe Clearance**: Menambahkan `pt-8` pada `<main>` master layout untuk mencegah search bar terpotong/tertutup oleh `h-20` sticky topbar.
  - **Compact List Row Density**: Menyesuaikan padding baris list dari `py-6` menjadi `py-3.5` serta memperkecil avatar ke `w-11 h-11` agar tampilan lebih ramping dan memuat lebih banyak data per layar.
  - **Komponen Pagination Kustom**: Mengimplementasikan `admin.layouts.pagination` dengan styling pill ungu `#BA18F5`, prev/next chevron, dan counter ringkasan data.
  - **Aktivasi Pagination di Seluruh Layar**: Mengonfigurasi limit default per-page (4 item per halaman) pada seluruh Web Controller agar pagination langsung aktif dan dapat diuji navigasinya.
- **0.6.1**: Relokasi & Integrasi Penuh **Admin Web Panel ke dalam `backend-api/backend`**:
  - **Relokasi Arsitektur**: Memindahkan seluruh antarmuka admin dari direktori root terpisah ke dalam ekosistem internal Laravel backend (`backend-api/backend/resources/views/admin/`) memanfaatkan engine templating Blade terintegrasi dengan Vite dan Tailwind CSS v4.
  - **Manajemen Aset Vektor**: Menempatkan aset SVG ikon dan kubus 3D isometrik di `backend-api/backend/public/assets/admin/`.
  - **Arsitektur Web Controller Dedicated**: Menambahkan `AdminAuthController`, `AdminAccountController`, `AdminPostController`, `AdminExhibitionController`, dan `AdminLogController` di `app/Http/Controllers/Admin/Web/` dengan integrasi langsung model Eloquent serta fallback mockup graceful saat database lokal offline.
  - **Pembaruan Web Routing**: Memetakan grup rute `/admin/*` di `routes/web.php` untuk login, akun (tanpa filter/status Active), moderasi karya, pameran virtual, dan riwayat aktivitas.
- **0.6.0**: Spesifikasi Lengkap **VEN Admin Web Panel Desktop (Light & Dark Mode) (§13)**:
  - **Observasi 9 Berkas Desain**: Mengintegrasikan seluruh rancangan dari `D:\front-mobile\admin/` (`Login (Desktop).png`, `Frame 13214` s.d. `13221`).
  - **Standarisasi 100% English UI**: Seluruh teks antarmuka yang dibuat sistem dibakukan ke dalam Bahasa Inggris.
  - **Penghapusan Elemen "Active"**: Menghilangkan dropdown filter "Active" dan label "status Active" pada layar Account sesuai arahan terbaru.
  - **Analisis & Konversi Kubus 3D Login**: Menguraikan spesifikasi geometri `balok-1.xml` dan `balok-2.xml`, konversi ke SVG murni, serta tata letak 4 kuadran melayang di atas latar lingkaran gradasi ungu konsentris.
  - **Katalog 5 Layar Dashboard**: Menyusun spesifikasi teknis untuk Login Desktop, Account Management, Post Moderation, Exhibition Management, dan Activity Log.
  - **Sinkronisasi Kontrak REST API Laravel**: Memetakan seluruh rute `/api/admin/*` (`PenggunaController`, `ModerasiController`, `LogController`) ke dalam antarmuka admin.
  - **Arsitektur Direktori `ven-admin`**: Menetapkan struktur direktori monorepo baru `C:\laragon\www\ven\ven-admin/` berbasis React + Vite + Tailwind CSS + TypeScript.
- **0.5.0**: Analisis Mendalam & Spesifikasi Lengkap **Halaman AI Assistant / AI Chat Dark Mode (§8.12)**:
  - **Observasi 4 Wireframe**: Mengintegrasikan seluruh rancangan dari `D:\front-mobile\dark-mode\ai\` (`Wireframe - AI Chat.png` [Empty State], `Wireframe - AI Chat-1.png` [Conversation State], `Wireframe - AI Chat-2.png` [History Drawer], `Wireframe - AI Chat-3.png` [Input Active / Typing]).
  - **Dua State Tampilan Utama**: Mengatur tata letak dinamis `Empty State` (watermark sparkle besar putih ±8% di tengah-atas, input pill vertikal di tengah, label "AI assistant") dan `Conversation State` (input pill berpindah ke bawah di atas BottomNav, kanvas berubah menjadi `LazyColumn` percakapan auto-scroll).
  - **Spesifikasi Komponen Baru**: Mendaftarkan dan mendefinisikan komponen `AiChatTopBar`, `AiChatEmptyState`, `ChatBubbleUser` (speech tail bubble, radius 16dp kecuali kanan-atas ~4dp, lebar maks 87%), `ChatMessageAi` (tanpa bubble latar, teks langsung di background `#1F1F1F`, lebar penuh padding 24dp, `TextAlign.Justify`, daftar bernomor 1–10 interaktif), `AiChatInput` (pill 56–60dp, tombol kirim dinamis muncul saat ada teks, adaptif `imePadding`), `ChatHistoryDrawer` (modal drawer 300dp, aksi "New chat", label "Recents"), dan `ChatHistoryItem` (1 baris teks ellipsis).
  - **Arsitektur MVI & Local Mockup**: Merancang `AiChatUiState` reaktif dan `AiChatDummyData.kt` untuk preview dan pengujian mandiri offline di sisi frontend.
  - **Spesifikasi Kontrak Backend Laravel**: Menyinkronkan endpoint `POST /api/ai/tanya` (throttled 10 req/menit, validasi maks 1000 karakter, otomatis injeksi 10 karya & 10 pameran terbaru sebagai konteks) serta penanganan fallback error di UI.
- **0.4.9**: Analisis Mendalam & Spesifikasi Lengkap **Halaman Explore Dark Mode (§8.5)**:
  - **Observasi 8 Wireframe**: Mengintegrasikan seluruh rancangan dari `D:\front-mobile\dark-mode\explore\` (`Wireframe - Explore.png`, `Explore history`, `Explore all`, `Explore posts`, `Explore exibhition`, `Explore account`, `Explore filter`, `Post`).
  - **State Layar Induk**: `ExploreScreen` mengelola 4 state lokal — Discovery, Search History, Search Results, dan Filter Sheet (§8.5.1–§8.5.4).
  - **Spesifikasi Komponen Baru**: `ExploreTopBar`, `CategoryFilterSheet`, `SearchHistoryItem`, `SearchAccountRow`, `ExhibitBannerCard`; perluasan `SearchBar` (mode Search Active) dan `VexTabs` (4 tab).
  - **`MediaGrid` Fleksibel**: parameter `columns` (3 untuk Discovery, 2 untuk Search Results tab Posts).
  - **`PostsFeedScreen` (§8.5.5)**: layar detail post **scrollable** (`LazyColumn`) dengan top bar "Posts" pinned, kartu `PostCard`, dan auto-scroll ke `postId` yang diketuk.
  - **Ikon Baru**: `ic_action_filter` (`VexIcons.Filter`), `ic_action_history` (`VexIcons.History`), `ic_action_close` (`VexIcons.Close`).
  - **Route Baru**: `explore` (tab) dan `posts_feed?postId={postId}`.
  - **MVI Mandiri & Dummy Data**: implementasi frontend murni (`ExploreUiState.kt`, `ExploreDummyData.kt`) dengan aset lokal `poster_1/2/3`.
- **0.4.8**: Analisis Mendalam & Spesifikasi Lengkap **Halaman Schedule Dark Mode (§8.4)**:
  - **Observasi 6 Wireframe**: Mengintegrasikan seluruh rancangan dari `D:\front-mobile\dark-mode\schedule\` (`wireframe schedule.png` hingga `wireframe schedule-5.png`).
  - **Spesifikasi Komponen Baru**: Mendaftarkan dan mendefinisikan komponen `SearchBar`, `VexTextTabs`, `EventListItem`, `RequestListItem`, `EventDetailSheet`, dan `RequestDetailSheet`.
  - **Dua Ragam Bottom Sheet**: Membedakan alur detail `EventDetailSheet` (CTA "Join Room" aktif pada Approved vs disabled pada Waiting) dan `RequestDetailSheet` (dua tombol aksi seimbang 50:50: "Approve" ungu dan "Reject" merah).
  - **MVI Mandiri & Dummy Data**: Merancang implementasi murni pada sisi frontend menggunakan data dummy lokal (`poster_1.webp`, `poster_2.webp`, `poster_3.webp`) dan penanganan filter realtime SearchBar.
- **0.4.7**: Implementasi lengkap rangkaian layar **Settings & Activity Flow (§8.8–§8.11)**.
- **0.4.6**: Implementasi arsitektur MVI/MVVM halaman **Profile Pengguna Sendiri (§8.7)**.
- **0.4.5**: Modernisasi **Kalender Dinamis Request Meeting (§8.6.1)**:
  - **Dukungan `java.time` & Core Library Desugaring**: Mengganti kalender statis/hardcode dengan kalkulasi tanggal dinamis berbasis `java.time.LocalDate` dan `YearMonth`. Mengaktifkan `isCoreLibraryDesugaringEnabled = true` dan menambahkan `com.android.tools:desugar_jdk_libs:2.1.5` agar berjalan lancar di `minSdk = 24` tanpa melanggar §2 Aturan Emas (bebas library UI pihak ketiga).
  - **Navigasi Bulan Interaktif**: Menambahkan header navigasi bulan dan tahun ("September 2026") dengan tombol panah kiri/kanan interaktif untuk berpindah bulan.
  - **Aset Ikon Chevron**: Menambahkan aset vector `ic_action_chevron_left.xml` dan `ic_action_chevron_right.xml` (24dp solid hitam tintable) ke `res/drawable/` serta mendaftarkannya di `VexIcons.kt` (`ChevronLeft` dan `ChevronRight`).
  - **Utilitas Pemformat Tanggal**: Menambahkan modul `com.ven.app.util.DateFormatter` untuk pemformatan string tanggal dinamis sesuai standar arsitektur §3.2.
- **0.4.4**: Pembaruan spesifikasi **Request Meeting (§8.6.1)**:
  - **Sticky Bottom Action Bar**: Tombol "Send Request" dipindahkan dari flow scroll ke footer tetap `VexBottomActionBar` (`Scaffold(bottomBar = ...)`), berlatar `navBar` (`#252525`) dengan garis atas 1dp `border` (`#313131`), margin horizontal 20dp, terikat `navigationBarsPadding().imePadding()` sehingga posisinya fixed mirip `BottomNav`.
  - **Pola Global (§5.2 & §6.2)**: Menambahkan definisi pola *Sticky Bottom CTA Bar* dan mendaftarkan composable `VexBottomActionBar`.
  - **Pemilihan Jam (Chose Time)**: Mengganti spesifikasi chip preset menjadi **Segmented Time Picker Interaktif** (`HH : MM` + toggle `AM`/`PM`) dengan kotak berlatar `BrandPurple` (`#BA18F5`) dan radius 10–12dp sesuai wireframe.
  - **Field Pesan**: Menambahkan Seksi 4 berupa field input multiline "Message..." (`VexTextField.Boxed` / textarea) bersifat opsional.
  - **Alur Ruangan**: Menegaskan alur UI bahwa pemohon tidak perlu memilih atau mengetahui ruangan privat saat mengajukan request; room dan tautan baru diterima setelah request disetujui (Approved).
- **0.4.3**: Analisis mendalam wireframe dark mode dari folder `D:\front-mobile\dark-mode\register\` dan `D:\front-mobile\dark-mode\home\`:
  - **Register (§8.3)**: Memperbarui spesifikasi 4 field input pill putih `#FFFFFF` (Email, Username, Password, Confirm Password), CTA Register, tombol Google "Login with Google", dan footer navigasi Login.
  - **Onboarding Interest (§8.1)**: Memperbarui 14 kategori chip dark mode (`#343333`), penempatan link "Maybe later" ungu dengan divider 1dp, dan tombol Save ungu.
  - **Home Screen (§8.6)**: Memperbarui detail TopBar Home, Coming Soon carousel dengan divider 1dp, header PostCard `@Graaph`, dan gambar post edge-to-edge tanpa radius.
  - **Request Meeting (§8.6.1)**: Menambahkan spesifikasi lengkap alur Request Meeting (dipicu dari ikon kalender PostCard): SubPage info pameran, poster banner, kalender interaktif 7 kolom dengan tanggal terpilih ungu `#BA18F5`, slot jam "Chose Time", dan tombol CTA "Send Request" terhubung ke API Laravel `POST /undangan`.
  - **Pertanyaan Terbuka (§12)**: Menjawab dan menyelesaikan fungsi ikon kalender & play pada PostCard.
- **0.4.2**: Analisis Figma node `4372:689` (Forgot Password Screen - Dark Mode). Menambahkan spesifikasi lengkap §8.11 Forgot Password: pola SubPage dengan TopBar (back icon + judul "Forgot Password" + divider 1dp), field input Email tipe Boxed (radius 8–10dp, fill surfaceInput `#363636`, outline borderInput `#454545`, padding horizontal 25.5dp), dan tombol CTA "Send OTP" (pill BrandPurple `#BA18F5`, lebar 374dp, margin horizontal 20dp, margin bawah 25.5dp). Menghapus Forgot Password dari daftar belum ada wireframe (§8.15).
- **0.4.1**: Analisis Figma node `3946:17` (Dark Mode Login Screen). Memperbarui spesifikasi layar Login: input auth berlatar solid putih (`#FFFFFF`) bentuk pill (lebar 304dp, tinggi 48dp, jarak 32dp), tombol Login ungu (`#BA18F5`), divider 1dp (`#A5A5A5`), tombol Google putih ("Register with Google"), serta tombol Register putih di bagian footer.
- **0.4**: Sinkronisasi struktur proyek dengan repositori `ven`. Laravel ditetapkan secara eksplisit sebagai Headless RESTful Backend API (`backend-api/backend/`), sedangkan aplikasi mobile dikembangkan menggunakan Android Kotlin + Jetpack Compose (`ven-mobile/`). Menambahkan spesifikasi Clean Architecture lengkap (DI, Data Remote/Local, Domain Model/UseCase, UI MVI/Compose), mapping endpoint Laravel Sanctum ke Retrofit API Services, serta integrasi engine 3D.
- **0.3**: `navBar` token (`#252525`) untuk bottom nav; `primaryText` disetujui; ikon & logo kini dari aset Figma sendiri (`res-icons/` + `VexIcons`, lengkap inventaris, checklist ekspor, kode akses); Create Exhibit/3D tetap masuk cakupan (ikon editor masuk inventaris).
- **0.2**: dialihkan ke Kotlin + Jetpack Compose (token jadi Kotlin, komponen jadi Composable, insets/keyboard, dp/sp, 48dp touch). Ditambah token dark dari Figma, halaman AI Chat (+drawer), Create Post, Create Exhibit. Warna kategori jadi placeholder lengkap. Kontras dark diperbarui terhadap `#1F1F1F`.
- **0.1**: draft awal dari 17 wireframe light (versi web/CSS).
