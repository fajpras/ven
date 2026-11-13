---
name: clean-code-architecture
description: >-
  Panduan koding bersih, arsitektur modular, dan penataan struktur folder profesional untuk ekosistem VEN.
  Gunakan skill ini saat merefaktor kode, membuat modul/fitur baru, mengatur penamaan kelas dan file,
  atau menata struktur direktori di backend Laravel dan Android mobile.
---

# Clean Code & Professional Architecture - VEN

Panduan arsitektur, pola desain, dan standar struktur folder untuk pengembang dan AI agent di proyek VEN.

## 1. Golden Rules Koding Bersih (Clean Code)

### A. Backend (PHP / Laravel)
1. **Controller Ramping (*Skinny Controllers*)**:
   - Controller hanya bertanggung jawab mengorkestrasi alur: menerima request, memanggil service/action, lalu mengembalikan JsonResponse.
   - Jangan letakkan query DB kompleks atau kalkulasi bisnis di controller.
2. **Form Request untuk Validasi**:
   - Pindahkan aturan validasi input ke kelas `App\Http\Requests\<Domain>\<Action>Request`.
3. **Strict Typing**:
   - Selalu berikan type-hinting pada method parameter dan return type:
     ```php
     public function verifikasi(string $email, string $otp): HasilVerifikasiOtp
     ```
4. **Eloquent Models Bersih**:
   - Manfaatkan metode `casts(): array` modern.
   - Pindahkan query yang sering dipakai ke `Local Query Scopes` (`scopeAktif`, `scopePenyelenggara`).
5. **Separation of Concerns**:
   - Kriptografi dan integrasi pihak ketiga diisolasi ke dalam `app/Services/`.
   - Utility murni tanpa dependensi framework diisolasi ke `app/Support/`.

### B. Mobile (Kotlin / Android Jetpack Compose)
1. **Unidirectional Data Flow (UDF)**:
   - State selalu mengalir ke bawah (`ViewModel -> Screen -> Component`).
   - Event selalu mengalir ke atas (`Component -> Screen -> ViewModel via callback lambda`).
2. **Stateless vs Stateful Composables**:
   - Pisahkan file `*Screen.kt` (stateless, menerima state & callback) dari `*Route.kt` (stateful, menghubungkan ViewModel dan NavController).
   - Layar stateless mempermudah pembuatan `@Preview` dan testing antarmuka.
3. **Immutability pada UiState**:
   - Semua properti di dalam `UiState` harus bertipe `val` dan menggunakan immutable collections jika memungkinkan:
     ```kotlin
     data class InterestUiState(
         val isLoading: Boolean = false,
         val categories: List<CategoryItem> = emptyList(),
         val selectedIds: Set<Int> = emptySet(),
         val errorMessage: String? = null
     )
     ```
4. **Coroutine Concurrency yang Tepat**:
   - Gunakan `viewModelScope.launch(Dispatchers.IO)` saat memanggil API / Database.
   - Update state UI via `_uiState.update { ... }` pada Main thread.

---

## 2. Standar Struktur Folder & Lokasi File

### Backend Laravel (`backend-api/backend`)
```text
app/
├── Contracts/              # Interface abstraksi
├── Enums/                  # PHP 8.1+ Enums
├── Exceptions/             # Custom exception per domain
├── Http/
│   ├── Controllers/        # Dikelompokkan: Auth/, Pengguna/, Admin/, Pameran/
│   ├── Middleware/         # Filter HTTP request
│   └── Requests/           # FormRequest per domain
├── Models/                 # Eloquent Models
├── Observers/              # Model Lifecycle Observers
├── Services/               # Domain Business Services (Otp/, Enkripsi/, LogSistem)
└── Support/                # Helper murni
```

### Mobile Android (`ven-mobile`)
```text
app/src/main/java/com/ven/app/
├── core/config/            # AppConfig
├── data/
│   ├── api/models/         # DTO serializable
│   ├── api/                # ApiClient, ApiService, SessionManager
│   └── preference/         # DataStore/Preferences
├── ui/
│   ├── components/         # Reusable UI widgets (feature/, layout/, ui/)
│   ├── icons/              # Vektor icon
│   ├── navigation/         # NavGraph, Route definitions
│   ├── screens/[feature]/  # Feature folder: Screen, Route, ViewModel, UiState
│   └── theme/              # Material 3 styling
└── util/                   # Non-UI utilities
```

---

## 3. Checklist Refactoring Kode
Sebelum commit atau menyatakan fitur selesai, lakukan verifikasi:
- [ ] Apakah ada file yang terlalu panjang (>300 baris)? Jika ada, pecah menjadi komponen atau service yang lebih kecil.
- [ ] Apakah ada duplikasi kode logika? Ekstrak ke helper atau shared component.
- [ ] Apakah ada kode debug atau printout (`dd()`, `dump()`, `println`)? Hapus seluruhnya.
- [ ] Apakah import yang tidak terpakai sudah dibersihkan?
- [ ] Apakah file ditaruh pada subfolder yang sesuai dengan fungsinya?

