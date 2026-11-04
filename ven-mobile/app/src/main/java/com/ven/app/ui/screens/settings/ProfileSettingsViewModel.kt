package com.ven.app.ui.screens.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ven.app.data.api.ApiProfil
import com.ven.app.data.api.models.KategoriModel
import com.ven.app.data.api.models.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

@Immutable
data class ProfileSettingsUiState(
    val categories: List<KategoriModel> = emptyList(),
    val selectedIds: Set<Int> = emptySet(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isUploadingPhoto: Boolean = false,
    val fotoProfilUrl: String? = null,
    val pengguna: UserModel? = null,
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
    val isSessionExpired: Boolean = false,
)

/**
 * Memuat kategori minat terbaru dan menyimpan perubahan profil + minat.
 */
class ProfileSettingsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSettingsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        muat()
    }

    fun muat() {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val kategori = ApiProfil.kategori()
            if (kategori.isFailure) {
                val pesan = kategori.exceptionOrNull()?.message ?: "Gagal memuat kategori minat"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = pesan,
                        isSessionExpired = sesiBerakhir(pesan),
                    )
                }
                return@launch
            }

            val minat = ApiProfil.minat()
            if (minat.isFailure) {
                val pesan = minat.exceptionOrNull()?.message ?: "Gagal memuat kategori minat"
                _uiState.update {
                    it.copy(
                        categories = emptyList(),
                        selectedIds = emptySet(),
                        isLoading = false,
                        errorMessage = pesan,
                        isSessionExpired = sesiBerakhir(pesan),
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    categories = kategori.getOrElse { emptyList() },
                    selectedIds = minat.getOrElse { emptyList() }
                        .map { item -> item.idKategori }
                        .toSet(),
                    isLoading = false,
                    errorMessage = null,
                )
            }
        }
    }

    fun simpan(data: ProfileSettingsData) {
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, isSaved = false) }

            val profil = ApiProfil.updateProfil(
                nama = data.name.trim(),
                namaPanggilan = data.username.trim(),
                nomorHp = data.phone.trim(),
                deskripsi = data.description.trim(),
            )

            if (profil.isFailure) {
                val pesan = profil.exceptionOrNull()?.message ?: "Gagal menyimpan profil"
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = pesan,
                        isSessionExpired = sesiBerakhir(pesan),
                    )
                }
                return@launch
            }

            val pengguna = profil.getOrThrow()
            _uiState.update { it.copy(pengguna = pengguna) }

            if (_uiState.value.categories.isNotEmpty()) {
                val minat = ApiProfil.simpanMinat(data.interestIds)
                if (minat.isFailure) {
                    val pesan = minat.exceptionOrNull()?.message
                        ?: "Profil tersimpan, tetapi kategori minat gagal disimpan"
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = pesan,
                            isSessionExpired = sesiBerakhir(pesan),
                        )
                    }
                    return@launch
                }
            }

            _uiState.update {
                it.copy(isSaving = false, isSaved = true, pengguna = pengguna)
            }
        }
    }

    fun tampilkanGagal(pesan: String) {
        _uiState.update { it.copy(errorMessage = pesan) }
    }

    fun uploadFoto(file: File, mimeType: String) {
        if (_uiState.value.isUploadingPhoto) return

        viewModelScope.launch {
            _uiState.update { it.copy(isUploadingPhoto = true, errorMessage = null) }

            val result = ApiProfil.uploadFoto(file, mimeType)

            result.onSuccess { pengguna ->
                _uiState.update {
                    it.copy(
                        isUploadingPhoto = false,
                        fotoProfilUrl = pengguna.fotoProfil,
                        pengguna = pengguna,
                        errorMessage = null,
                    )
                }
            }.onFailure { exception ->
                val pesan = exception.message ?: "Gagal mengunggah foto"
                _uiState.update {
                    it.copy(
                        isUploadingPhoto = false,
                        errorMessage = pesan,
                        isSessionExpired = sesiBerakhir(pesan),
                    )
                }
            }
        }
    }

    private fun sesiBerakhir(pesan: String?): Boolean {
        val teks = pesan.orEmpty().lowercase()
        return "unauthenticated" in teks || "tidak terautentikasi" in teks ||
            "tidak_terautentikasi" in teks
    }
}
