---
name: android-compose-expert
description: >-
  Panduan dan standar pengembangan Android Kotlin dengan Jetpack Compose untuk projek VEN.
  Gunakan skill ini saat membuat atau mengubah screen, composable UI, ViewModel,
  UiState, navigasi, atau integrasi API di modul ven-mobile.
---

# Android Compose Expert - VEN Mobile

Panduan pengembangan aplikasi Android Jetpack Compose pada projek `ven-mobile`.

## 1. Arsitektur Komponen
Setiap fitur di `ven-mobile` disusun dengan pola pemisahan tanggung jawab yang jelas:
1. **UiState**: Data class *immutable* yang merepresentasikan kondisi layar (misal: `InterestUiState.kt`, `SettingsUiState.kt`).
2. **ViewModel**: Mewarisi `ViewModel()`, mengelola event dan mengekspos state melalui `StateFlow<UiState>`.
3. **Screen (Stateless Composable)**: Komponen UI murni yang menerima parameter state dan callback lambda (tidak boleh instansiasi ViewModel di dalam reusable components).
4. **Route (Stateful Composable)**: Menghubungkan ViewModel dengan Screen dan menangani aksi navigasi (misal: `ProfileSettingsRoute.kt`).

## 2. Praktik Jetpack Compose & Material 3
- Gunakan `androidx.compose.material3.*` untuk komponen antarmuka.
- Terapkan **State Hoisting**: Buat composable yang reusable dengan menerima state dan meneruskan event ke atas via lambda callback.
- Gunakan `remember` dan `derivedStateOf` secara bijak untuk mencegah *recomposition* yang tidak perlu.
- Gunakan resource string (`stringResource(R.string....)`) untuk mendukung lokalisasi (tersedia `values/strings.xml` dan `values-in/strings.xml`).

## 3. Asynchronous & Coroutines
- Selalu panggil API Retrofit melalui `viewModelScope.launch(Dispatchers.IO)`.
- Perbarui state UI di Main thread menggunakan `_uiState.update { ... }`.
- Tangani error koneksi dan HTTP status secara elegan dengan menampilkan indikator error/pesan di UiState.

