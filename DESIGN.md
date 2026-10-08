# design.md: VEN / VEX (Virtual Exhibition), Android (Kotlin + Jetpack Compose)

> **Versi:** 0.4 (sinkronisasi arsitektur repo VEN: Laravel Backend API + Android Kotlin & Jetpack Compose)
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
- **Tema default: DARK.** Light mode tetap didukung. Wireframe dark sudah diobservasi langsung dari aset desain resmi untuk: **Login (Auth - node 3946:17), Register, Onboard (Interest), Forgot Password (node 4372:689), Home, Request Meeting, AI Chat (drawer), Create Post, Create Exhibit**; halaman lain masih light dan diturunkan dari token yang sama.
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

Proyek **VEN (Virtual Exhibition)** menggunakan arsitektur modular yang memisahkan **Backend API** dan **Mobile Client**. Laravel difungsikan murni sebagai **RESTful Backend API**, sedangkan antarmuka pengguna dibangun sepenuhnya sebagai aplikasi Android native menggunakan **Kotlin + Jetpack Compose**.

### 3.1 Struktur Repositori Keseluruhan (`ven`)

```
ven/
├── backend-api/
│   └── backend/                     # BACKEND: Laravel 11 (Headless REST API)
│       ├── app/
│       │   ├── Http/Controllers/    # Endpoint API (Auth, Karya, Pameran, Objek, AI, Undangan, dll.)
│       │   ├── Models/              # Eloquent Models (User, Karya, Pameran, Objek, Model3D, dll.)
│       │   └── Middleware/          # Sanctum Authentication & Admin Middleware
│       ├── routes/
│       │   └── api.php              # Definisi route RESTful API
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
│   │   │   └── datastore/
│   │   │       ├── TokenManager.kt        # Simpan & baca token Sanctum secara aman
│   │   │       └── UserPreferences.kt     # Preferensi tema (Dark/Light/System), bahasa, dsb.
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
│   │   │   │   └── VexBottomSheet.kt
│   │   │   ├── layout/                    # Kerangka layout (lihat §6.2)
│   │   │   │   ├── AppScaffold.kt
│   │   │   │   ├── VexTopBar.kt
│   │   │   │   ├── BottomNav.kt
│   │   │   │   ├── AuthLayout.kt
│   │   │   │   └── PageContainer.kt
│   │   │   └── feature/                   # Komponen lintas-layar (lihat §6.3)
│   │   │       ├── PostCard.kt
│   │   │       ├── EventBanner.kt
│   │   │       ├── EventListItem.kt
│   │   │       ├── RequestListItem.kt
│   │   │       ├── MediaGrid.kt
│   │   │       ├── ProfileHeader.kt
│   │   │       └── ChatBubble.kt
│   │   ├── screens/                       # Screen-level composables (Screen, ViewModel, UiState)
│   │   │   ├── auth/                      # LoginScreen, RegisterScreen, ForgotPasswordScreen
│   │   │   ├── onboarding/                # InterestScreen (pemilihan kategori minat)
│   │   │   ├── home/                      # HomeScreen (Coming soon carousel + feed)
│   │   │   ├── schedule/                  # ScheduleScreen (Approved, Waiting, Request tabs + Detail Sheet)
│   │   │   ├── ai/                        # AiChatScreen (Chat view + History drawer)
│   │   │   ├── explore/                   # ExploreScreen (Media grid discovery, search & filter)
│   │   │   ├── profile/                   # ProfileScreen (User stats, posts/exhibits tabs)
│   │   │   ├── settings/                  # SettingsScreen & sub-pages (Profile, Email, Password, Theme, Language)
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
│   └── values-in/                         # strings.xml (Bahasa Indonesia)
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
| **VexButton** | `Primary`, `Inverse` (hitam di light, putih di dark), `Outline` (Google), `Danger`, `TextLink`. State: enabled, pressed, loading, disabled | Tinggi 48dp, `RoundedCornerShape(full)`, teks `labelLarge` rata tengah. Disabled Primary = `primaryDisabled` + teks pucat (lihat "Join Room" status Waiting). Lebar penuh di auth, form, sheet |
| **VexIconButton** | default, pressed | Area sentuh min 48dp walau ikon 24dp |
| **VexTextField** | `Pill` (auth: tanpa border, fill solid `#FFFFFF` pada Dark Auth screen `[OBSERVED 3946:17]` dengan teks `#000000` & placeholder abu-abu; atau fill `surfaceInput` pada screen umum), `Boxed` (settings & create: radius 8dp, fill `surfaceInput`, outline `borderInput`). State: default, focus, error, disabled | Label di atas untuk `Boxed`; placeholder di dalam untuk `Pill`. Fokus: outline 2dp `primary`. `KeyboardOptions` sesuai tipe (email, password, number), `imeAction` Next/Done. Autofill via `semantics { contentType = … }` |
| **VexPasswordField** | turunan | Tombol tampil/sembunyikan `[ASSUMPTION]` |
| **VexTextArea** | `Boxed`, multiline | Tinggi min ≈ 190dp (Description) |
| **VexDropdown** | `Boxed` + ikon chevron-bawah | `ExposedDropdownMenu`; opsi = daftar kategori |
| **OtpInput** | 5 kotak, radius 8dp, outline | **5 digit** `[OBSERVED]`; auto-pindah, dukung paste, `KeyboardType.Number`, kotak error berwarna `danger` |
| **SearchBar** | default, fokus, dengan tombol filter | Pill, tinggi 46dp, ikon search kiri, placeholder "Search" |
| **VexTabs** | `Text` (Approved/Waiting/Request, kolom sama lebar), `Icon` (foto / kubus 3D) | Aktif: teks/ikon `text` (ikon terisi) + garis bawah 1dp selebar tab; nonaktif: `textMuted`. Bangun sendiri (jangan indikator M3 default) |
| **VexChip** | `Selectable`, `Tag`, `Counter` ("+2") | Pill. Selectable default: fill `surfaceInput`; **selected: fill `primary`, teks putih** `[ASSUMPTION]`. Tag: fill warna kategori (§4.3) |
| **Avatar** | `sm` 24, `md` 40, `lg` 96, bulat | `ContentScale.Crop`, fallback inisial |
| **VexBottomSheet** | konten bebas | Bungkus `ModalBottomSheet`: radius atas 28dp, handle abu 40×4dp, container `surface` (dark) / putih (light), scrim `scrim` |
| **VexDrawer** | – | Bungkus `ModalNavigationDrawer`, lebar 300dp, container `bg` |
| **ListRow** | ikon + label + chevron; varian `Danger` | Settings. Tinggi ≥ 56dp, ikon 24dp, jarak ikon-label 16dp |
| **SectionLabel** | – | `bodyMedium` warna `textMuted` + `HorizontalDivider` |
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
| **VexTopBar** | varian: `Home` (+ kiri, logo VEX kanan), `Profile` (+ kiri, username tengah, ikon menu kanan), `Ai` (ikon menu/drawer kiri, logo kanan, tanpa garis bawah), `Back` (panah + judul), `Create` (X/panah + judul kiri, aksi teks kanan), `Search` (SearchBar penuh) |
| **AuthLayout** | judul besar tengah + slot form + blok bawah (teks + tombol) menempel di bawah |
| **PageContainer** | padding horizontal + `widthIn(max = contentMaxWidth)` |

### 6.3 `ui/components/feature/` (lintas screen)

| Composable | Dipakai di | Anatomi |
|---|---|---|
| **EventBanner** | Home (carousel "Coming Soon"), sheet | Gambar landscape radius 8–12dp; `LazyRow` + snap |
| **EventListItem** | Schedule (Approved/Waiting) | Thumbnail kiri ≈ 50% lebar rasio ≈ 16:9 (radius 4dp); kanan: tanggal (`titleLarge`), jam, "See details" (muted, kanan-bawah) |
| **RequestListItem** | Schedule/Request | Avatar, nama, tanggal+jam muted kanan-atas, pesan maks 2 baris ellipsis |
| **EventDetailSheet** | Schedule | Isi `VexBottomSheet` (§8.4) |
| **PostCard** | Home | Header `@username` kiri + ikon kalender & play kanan; gambar penuh lebar tanpa radius |
| **MediaGrid** | Explore, Profile, picker media | Grid 3 kolom, celah 2dp; dukung tile 2-kolom (`GridItemSpan(2)`) |
| **ProfileHeader / StatBlock** | Profile | Avatar 96dp, nama, statistik (angka tebal + label muted), tag, bio, email, phone |
| **ChatBubbleUser / ChatMessageAi** | AI | Lihat §8.12 |

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
- Settings → sub-halaman (SubPage, tanpa bottom nav).
- Route disarankan: grafik terpisah `auth`, `main` (5 tab), `settings`, `create` `[ASSUMPTION]`.

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
| `action` | `ic_action_add`, `_menu`, `_back`, `_close`, `_chevron_right`, `_chevron_down`, `_search`, `_filter`, `_calendar`, `_play`, `_new_chat`, `_visibility`, `_visibility_off` | TopBar, SearchBar, PostCard, dropdown, field password `[ASSUMPTION untuk visibility]` |
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
    // ... dst., satu entri per file
    @DrawableRes val LogoVex = R.drawable.logo_vex
    @DrawableRes val LogoGoogle = R.drawable.logo_google
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
  - Background: **Solid White (`#FFFFFF`)**, tanpa outline.
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
  - Bentuk: `Pill` (100 dp), lebar 304 dp, tinggi 48 dp, background **Solid White (`#FFFFFF`)**.
  - Konten: Logo Google multi-warna di sisi kiri + teks **"Login with Google"** warna hitam (`#000000`, 16sp Medium) `[OBSERVED pada wireframe Register]`.
- **Footer (Navigasi ke Login):**
  - Helper text: **"Already have an account?"**, teks putih / `textSecondary`, ukuran 14sp, rata tengah horizontal.
  - Tombol **"Login"** (Inverse Button):
    - Bentuk: `Pill` (100 dp), lebar 304 dp, tinggi 48 dp, background **Solid White (`#FFFFFF`)**, teks "Login" hitam (`#000000`), 16sp Medium 500, rata tengah.
    - Margin bawah ke tepi layar: **55 dp**.
- **Integrasi Backend:** Submit form memanggil endpoint `POST /auth/register` (Laravel Sanctum API), yang mengembalikan token autentikasi dan mengarahkan pengguna ke halaman Onboarding Interest (§8.1).

### 8.4 Schedule
- SearchBar → `VexTabs` `Approved | Waiting | Request` → daftar → bottom nav.
- **Approved & Waiting:** `EventListItem`; tap → `EventDetailSheet`:
  - *Approved:* banner → "Your request which is scheduled in **{hari, tanggal} at {jam}** has been approved. The room will be available on said time, please attend the meeting as scheduled." → **Join Room** (Primary aktif).
  - *Waiting:* "…has been sent and is waiting for approval. Please wait patiently." → **Join Room** disabled.
  - Tanggal/jam di kalimat dicetak **tebal** (gunakan `AnnotatedString`).
- **Request (inbox):** `RequestListItem`; tap → sheet: banner → "Requested for **{tanggal jam}**." → avatar kecil + username → isi pesan lengkap → dua tombol sejajar sama lebar: **Approve** (Primary) dan **Reject** (Danger).
- Format tanggal: `24th Sep 2026` (list), `Thursday, 24th September 2026` (sheet), `27/9/26` (request). Satu util `formatDate` `[ASSUMPTION]`.
- State: skeleton, empty per tab `[TODO copy]`, error, loading di tombol.

### 8.5 Explore
- SearchBar + tombol filter (kanan) → grid media → bottom nav (tab Explore aktif).
- Grid 3 kolom celah 2dp tanpa radius; tile potret mendominasi, sesekali tile lebar 2 kolom `[ASSUMPTION pola berulang]`.
- Tap tile → detail karya `[TODO]`. Filter → sheet `[TODO]`. State: skeleton, empty hasil, paging.

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

### 8.6.1 Request Meeting (Dark Mode) `[OBSERVED: Wireframe - Request Meeting.png]`
- **Pemicu Alur:** Dibuka ketika pengguna menekan ikon kalender (`ic_action_calendar`) pada kartu karya `PostCard` di Home Feed (§8.6).
- **Kerangka Halaman:** `SubPage` (Top bar dengan tombol kembali + konten scrollable + tombol CTA "Send Request" di bawah). Latar belakang kanvas `#1F1F1F`.
- **TopBar (`VexTopBar.Back`):**
  - Sisi Kiri: Ikon kembali (panah kiri `ic_action_back`, 24 dp, putih `#FFFFFF`, margin kiri 22.5 dp).
  - Judul: **"Request meeting"**, warna `#FFFFFF`, ukuran 20sp Medium 500.
  - Garis Pembatas Bawah: Garis horizontal 1 dp warna `#313131` / `border` pada $y = 66\text{ dp}$.
- **Seksi 1: Info & Banner Pameran:**
  - Judul Pameran: mis. "6th International Expo", 18–20sp SemiBold 600, warna putih `#FFFFFF`, padding horizontal 24 dp, margin atas 20 dp.
  - Banner Pameran: Kartu poster persegi panjang membulat (radius $12 - 16\text{ dp}$ / `VexRadius.lg`), rasio $\approx 16:9$, margin horizontal 24 dp, margin bawah 20 dp.
  - Garis Pembatas: Garis horizontal 1 dp warna `#313131` / `border`.
- **Seksi 2: Pemilihan Tanggal ("Chose Date"):**
  - Judul Seksi: "Chose Date", 18sp SemiBold 600, warna putih `#FFFFFF`, padding horizontal 24 dp.
  - Sub-label Tanggal Terpilih: e.g. "Sat, 26 September 2026", 14sp Medium 500, warna putih `#FFFFFF`, margin bawah 12 dp.
  - **Kalender Interaktif (Grid 7 Kolom Compose):**
    - Baris Header Hari: Mon, Tue, Wed, Thu, Fri, Sat, Sun (14sp, warna abu-abu `#8A8A92` / `textMuted`, rata tengah).
    - Grid Angka Tanggal:
      - Tanggal di luar bulan aktif (prev/next month): warna abu-abu gelap `#555555` (`textDisabled`).
      - Tanggal bulan aktif: warna putih `#FFFFFF` (`text`).
      - Tanggal Terpilih: Lingkaran penuh warna **`BrandPurple` (`#BA18F5`)**, teks angka putih tebal (contoh pada wireframe: tanggal 30 aktif).
    - Margin horizontal kalender: 24 dp.
  - Garis Pembatas: Garis horizontal 1 dp warna `#313131` / `border`.
- **Seksi 3: Pemilihan Jam ("Chose Time"):**
  - Judul Seksi: "Chose Time", 18sp SemiBold 600, warna putih `#FFFFFF`, padding horizontal 24 dp, margin atas 16 dp.
  - Slot Waktu: Chip pilihan jam yang tersedia (mis. 09:00, 10:30, 14:00) yang disediakan oleh pemilik pameran.
- **Seksi 4: Tombol CTA ("Send Request"):**
  - Tombol **"Send Request"** (Primary CTA):
    - Bentuk: `Pill` (`RoundedCornerShape(full)` / 100 dp).
    - Dimensi: Lebar 374 dp (margin horizontal 19–20 dp), tinggi $52 - 56\text{ dp}$.
    - Background: **`BrandPurple` (`#BA18F5`)**, teks "Send Request" putih (`#FFFFFF`), `labelLarge` (16sp Medium 500), rata tengah.
    - Posisi: Menempel di bawah layar dengan `Modifier.navigationBarsPadding().imePadding()`, margin bawah 24 dp.
- **Integrasi Backend:** Mengirimkan data permintaan pertemuan ke endpoint `POST /undangan` (Laravel API auth:sanctum) dengan payload ID pameran, tanggal, jam, dan pesan opsional. Permintaan ini otomatis masuk ke status *Waiting* di Schedule pengirim dan status *Request* di Schedule pemilik karya (§8.4).

### 8.7 Profile
- `VexTopBar.Profile` → avatar 96dp + (nama; di bawahnya `StatBlock` posts & exhibits) → baris tag (maks 4 + chip "+N") → bio → "Email" (muted) + nilai → "Phone" (muted) + nilai → `VexTabs.Icon` (foto = *posts*, kubus 3D = *exhibits*) → `MediaGrid` → bottom nav.
- Nama panjang boleh 2 baris. Empty state per tab `[TODO copy]`.

### 8.8 Settings and activity
- SubPage (back + judul). Kelompok dengan `SectionLabel`: **Account** (Profile, Change email, Change password, Account status, History), **Interface** (Theme, Language), **Security & Others** (Privacy policy, Help), **Login** (Logout: ikon + teks `danger`, tanpa chevron, konfirmasi dialog `[ASSUMPTION]`).
- *Theme* = pilihan mode (§4.7). *Language* = `AppCompatDelegate.setApplicationLocales` `[ASSUMPTION]`.

### 8.9 Change email → Verify email
- *Change email:* back + judul → field `Boxed` berlabel (New email, Password, Confirm password) → helper 12sp muted "You need to verify your new email for the change to be applied" → **Send OTP** (bawah).
- *Verify email:* label "OTP" → `OtpInput` 5 kotak → "didn't get your mail?" + **Resend** (TextLink). Cooldown Resend ±60 dtk dengan hitung mundur `[ASSUMPTION]`; verifikasi otomatis saat 5 digit terisi `[ASSUMPTION]`. State: salah/kedaluwarsa, loading.

### 8.10 Change password
- Seperti Change email. Field: Current password, New password, Confirm password → **Send OTP** → halaman Verify OTP yang sama `[ASSUMPTION]`.

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

### 8.12 AI Assistant `[OBSERVED]`
- **Tab sparkle.** TopBar `Ai`: ikon menu (dua garis tak sama panjang) kiri → membuka `VexDrawer`; logo VEX kanan. Tidak ada garis bawah.
- **State kosong:** sparkle besar (bintang 4 sudut, sangat samar: putih ±8% di dark) di tengah-atas; **kolom input pill di tengah layar**, di bawahnya teks "AI assistant" (`bodyLarge`, tengah). Saat pesan pertama dikirim, input pindah ke bawah (di atas bottom nav).
- **State percakapan:**
  - Pesan pengguna: bubble rata kanan, fill `surfaceInput`, radius 16dp **kecuali sudut kanan-atas ≈ 0–4dp** (efek ekor), lebar maks ≈ 87% layar, teks `chatBody`.
  - Balasan AI: **tanpa bubble**, teks langsung di latar, lebar penuh dengan padding 24dp, `chatBody`. Wireframe memakai rata kiri-kanan (*justify*); `TextAlign.Justify` `[OBSERVED]`, boleh diganti Start bila terbaca buruk `[TODO]`. Mendukung daftar bernomor (markdown ringan).
  - Daftar 10 karya di balasan: sebaiknya item bisa diketuk → detail karya / ruang 3D (teks di wireframe: "Kamu bisa memilih salah satu karya…") `[TODO bentuk: link teks atau kartu]`.
  - Bahasa balasan mengikuti bahasa pengguna.
- **Input:** pill tinggi ≈ 60dp (lebih tinggi dari input biasa), fill `surfaceInput`, placeholder "Type Something" (`textMuted`), margin horizontal ≈ 30dp, di atas bottom nav. Tombol kirim muncul saat ada teks `[ASSUMPTION]`; naik mengikuti keyboard (`imePadding`).
- **Drawer riwayat:** lebar 300dp dari kiri, latar `bg`, scrim di sisa layar (logo VEX tetap terlihat di bawah scrim `[OBSERVED]`). Isi: baris **"New chat"** (ikon pena-pada-kotak + teks) → label "Recents" (muted) → daftar judul chat (1 baris, ellipsis). Judul chat bisa berbahasa apa saja. Tap = buka chat; tutup dengan swipe/tap scrim/Back. Aksi ganti nama/hapus (long-press) `[TODO]`.
- State: streaming (teks muncul bertahap), loading (3 titik/skeleton), error + retry, empty riwayat, batas panjang input `[TODO]`.

### 8.13 Create Post `[OBSERVED]` (CreateFlow, tanpa bottom nav)
Dua langkah. Di langkah 1 ada **SegmentedPill "Post | Exhibit"** melayang di bawah tengah (lebar ≈ 245dp, tinggi ≈ 54dp, di atas inset bawah); mengganti ke "Exhibit" membuka §8.14.
1. **Pilih media:** TopBar `Create` ("X" + "New Post" kiri, **"Next"** `primaryText` kanan) → pratinjau besar (lebar penuh, potret ±4:5) dari item terpilih → label "Recents" (`titleMedium`) → `MediaGrid` 3 kolom, 2dp dari galeri. Gunakan Photo Picker/izin media; state izin ditolak `[TODO]`.
2. **Detail:** TopBar `Create` (panah + "New Post", **"Post"** `primaryText`) → pita pratinjau (tinggi ±262dp, gambar *contain* di tengah, latar abu gelap) → field `Boxed` berlabel di atas (`titleSmall`/medium, jarak antar field ±16dp):
   - **Title** (teks)
   - **Category** (`VexDropdown`, daftar kategori §8.1)
   - **Product Link** + label kanan "optional" (`textSecondary`), validasi URL
   - **Description** (`VexTextArea`, placeholder "type your description...")
- "Post" disabled sampai Title & Category terisi `[ASSUMPTION]`; loading saat unggah; error unggah + retry. Setelah sukses kembali ke Profile/Home `[TODO]`.

### 8.14 Create Exhibit `[OBSERVED]`
1. **Pilih template (New Exhibit):** TopBar `Create` ("X" + "New Exhibit"), daftar vertikal kartu: judul kiri (`titleMedium`) + kapasitas kanan (`textSecondary`, mis. "9 Post"), pratinjau isometrik 3D di bawahnya (radius 8dp, latar abu gelap bergrid). Template: **Small (9)**, **Medium (14)**, **Big (21)**, **Event (±50/60 `[TODO angka pasti, tertutup toggle]`)**. SegmentedPill "Post | Exhibit" melayang di bawah (Exhibit terpilih). Tap kartu → editor.
2. **Editor:**
   - **Canvas 3D** memenuhi bagian atas, sisi bawah melengkung 28dp, latar abu gelap bergrid. Engine/gesture (orbit, zoom) `[TODO]`.
   - **Panah kembali** kiri-atas (putih). **Kolom 4 `FloatingToolButton`** (lingkaran 48dp, fill `surface`, jarak 16dp) di kanan-atas: **Aa** (judul & deskripsi exhibit), **panel** (tambah/atur panel karya), **furnitur** (ikon kursi sofa; dekorasi), **warna** (lingkaran berisi warna terpilih + ring putih; ganti warna dinding).
   - **Penghitung** kiri-bawah canvas: ikon panel + "4/9 panels" (14sp putih). Panel = slot pajangan untuk post; total = kapasitas template.
   - **Checklist syarat publish** di bawah canvas: baris berisi ikon check-lingkaran terisi + teks, **hijau (`success`)** bila terpenuhi: "title & description", "a panel". Belum terpenuhi → ikon kosong + `textMuted` `[ASSUMPTION]`.
   - **Publish** (Primary, penuh, margin 20dp). Disabled sampai semua syarat hijau `[ASSUMPTION]`.
   - Warna ungu kubus di canvas adalah *warna exhibit pilihan pengguna*, **bukan** token UI.
- State: loading model 3D, error render, konfirmasi bila keluar dengan perubahan belum tersimpan `[ASSUMPTION]`.

### 8.15 Belum ada wireframe `[TODO]`
Account status, History, Theme, Language, Privacy policy, Help, detail karya, detail/tampilan exhibit (mode melihat), filter Explore, halaman Join Room, pilih panel/furnitur/warna di editor.

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

**Masih terbuka:**
1. Label teks tombol Google di Login/Register pada UX final: apakah diseragamkan "Continue with Google" atau mengikuti teks wireframe "Register with Google" (Login) dan "Login with Google" (Register).
2. Isi spesifik bottom sheet Filter Explore dan navigasi tile Explore.
3. Peran warna `surface` (sheet, drawer, tombol melayang, toggle) di dark: sementara disamakan dengan `#252525`. Ada nilai berbeda di Figma?
4. Bottom nav di light mode: warna dan ada/tidaknya garis atas.
5. Tampilan chip Interest saat terpilih: apakah background ungu solid (`#BA18F5`) dengan teks putih atau variasi outline?
6. Perilaku AI: tombol kirim, lampiran, item karya bisa diketuk, rename/hapus chat, batas panjang.
7. Wireframe dark untuk halaman lain (Schedule, Profile, Settings, Explore) untuk memvalidasi turunan token.
8. Aturan validasi password, cooldown OTP, batas ukuran/rasio gambar unggahan.
9. Perilaku scroll top bar Home/Explore; konten empty/error tiap halaman.
10. Library final (Navigation, Coil, DataStore, dll.) dan `minSdk`.
11. Engine 3D editor exhibit (library, gesture orbit/zoom), jumlah panel template Event, isi tool panel/furnitur/warna, dan bagaimana exhibit dilihat orang lain.

---

## Changelog
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
