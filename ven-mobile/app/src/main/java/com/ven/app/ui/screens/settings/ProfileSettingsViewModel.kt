package com.ven.app.ui.screens.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ven.app.data.api.ApiProfil
import com.ven.app.data.api.models.KategoriModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ProfileSettingsUiState(
    val categories: List<KategoriModel> = emptyList(),
    val selectedIds: Set<Int> = emptySet(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val isSaved: Boolean = false,
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
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = kategori.exceptionOrNull()?.message
                            ?: "Gagal memuat kategori minat",
                    )
                }
                return@launch
            }

            val minat = ApiProfil.minat()
            if (minat.isFailure) {
                _uiState.update {
                    it.copy(
                        categories = emptyList(),
                        selectedIds = emptySet(),
                        isLoading = false,
                        errorMessage = minat.exceptionOrNull()?.message
                            ?: "Gagal memuat kategori minat",
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
                deskripsi = data.description.trim(),
            )

            if (profil.isFailure) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = profil.exceptionOrNull()?.message
                            ?: "Gagal menyimpan profil",
                    )
                }
                return@launch
            }

            val minat = ApiProfil.simpanMinat(data.interestIds)
            if (minat.isFailure) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = minat.exceptionOrNull()?.message
                            ?: "Gagal menyimpan kategori minat",
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(isSaving = false, isSaved = true) }
        }
    }
}
