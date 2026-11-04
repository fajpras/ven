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
data class AccountStatusUiState(
    val kataSandi: String = "",
    val setujuKetentuan: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
) {
    val isFormValid: Boolean
        get() = kataSandi.isNotBlank() && setujuKetentuan && !isLoading
}

/**
 * Menonaktifkan akun sendiri secara sementara melalui PUT /api/profil/status.
 * Sesuai UC-04: memerlukan kata sandi + persetujuan ketentuan.
 */
class AccountStatusViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AccountStatusUiState())
    val uiState = _uiState.asStateFlow()

    fun onKataSandiChange(value: String) =
        _uiState.update { it.copy(kataSandi = value, errorMessage = null) }

    fun onSetujuChange(value: Boolean) =
        _uiState.update { it.copy(setujuKetentuan = value, errorMessage = null) }

    /** Reset form ketika pengguna memilih "Batal". */
    fun reset() = _uiState.update { it.copy(kataSandi = "", setujuKetentuan = false, errorMessage = null) }

    fun deaktivasi() {
        val state = _uiState.value
        if (!state.isFormValid) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = ApiProfil.updateStatus(
                status = "nonaktifkan",
                kataSandi = state.kataSandi,
            )

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Gagal menonaktifkan akun",
                    )
                }
            }
        }
    }
}
