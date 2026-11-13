package com.ven.app.ui.screens.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ven.app.data.api.ApiProfil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class ChangePasswordUiState(
    val kataSandiSaatIni: String = "",
    val kataSandi: String = "",
    val konfirmasiKataSandi: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
) {
    val isFormValid: Boolean
        get() = kataSandiSaatIni.isNotBlank() &&
                kataSandi.isNotBlank() &&
                kataSandi == konfirmasiKataSandi &&
                !isLoading
}

/**
 * Mengganti kata sandi pengguna melalui endpoint PUT /api/profil/kata-sandi.
 */
class ChangePasswordViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState = _uiState.asStateFlow()

    fun onKataSandiSaatIniChange(value: String) =
        _uiState.update { it.copy(kataSandiSaatIni = value, errorMessage = null) }

    fun onKataSandiChange(value: String) =
        _uiState.update { it.copy(kataSandi = value, errorMessage = null) }

    fun onKonfirmasiChange(value: String) =
        _uiState.update { it.copy(konfirmasiKataSandi = value, errorMessage = null) }

    fun simpan() {
        val state = _uiState.value
        if (state.kataSandi != state.konfirmasiKataSandi) {
            _uiState.update { it.copy(errorMessage = "Konfirmasi kata sandi tidak sesuai") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = ApiProfil.updateKataSandi(
                kataSandiSaatIni = state.kataSandiSaatIni,
                kataSandi = state.kataSandi,
            )

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Gagal mengganti kata sandi",
                    )
                }
            }
        }
    }
}
