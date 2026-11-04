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
data class ChangeEmailUiState(
    val emailBaru: String = "",
    val kataSandi: String = "",
    val konfirmasiKataSandi: String = "",
    val otp: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val isOtpSent: Boolean = false,
    val isSuccess: Boolean = false,
) {
    val isFormValid: Boolean
        get() = emailBaru.isNotBlank() &&
                kataSandi.isNotBlank() &&
                kataSandi == konfirmasiKataSandi &&
                !isLoading
}

/**
 * Mengganti email pengguna melalui alur dua langkah:
 * 1. POST /api/profil/email/kirim-otp (kirim OTP ke email baru).
 * 2. PUT  /api/profil/email (verifikasi OTP + simpan email baru).
 */
class ChangeEmailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChangeEmailUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(emailBaru = value, errorMessage = null) }

    fun onKataSandiChange(value: String) =
        _uiState.update { it.copy(kataSandi = value, errorMessage = null) }

    fun onKonfirmasiChange(value: String) =
        _uiState.update { it.copy(konfirmasiKataSandi = value, errorMessage = null) }

    fun onOtpChange(value: String) =
        _uiState.update { it.copy(otp = value, errorMessage = null) }

    /** Reset flag agar navigasi ke layar OTP tidak terpicu berulang. */
    fun onOtpConsumed() =
        _uiState.update { it.copy(isOtpSent = false) }

    /** Langkah 1: kirim OTP ke email baru. */
    fun kirimOtp() {
        val state = _uiState.value
        if (state.kataSandi != state.konfirmasiKataSandi) {
            _uiState.update { it.copy(errorMessage = "Konfirmasi kata sandi tidak sesuai") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }

            val result = ApiProfil.kirimOtpGantiEmail(
                emailBaru = state.emailBaru,
                kataSandi = state.kataSandi,
            )

            result.onSuccess { message ->
                _uiState.update {
                    it.copy(isLoading = false, isOtpSent = true, infoMessage = message)
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Gagal mengirim kode verifikasi",
                    )
                }
            }
        }
    }

    fun kirimUlangOtp() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = ApiProfil.kirimOtpGantiEmail(
                emailBaru = _uiState.value.emailBaru,
                kataSandi = _uiState.value.kataSandi,
            )
            result.onSuccess { message ->
                _uiState.update { it.copy(isLoading = false, infoMessage = message) }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = exception.message ?: "Gagal mengirim ulang kode")
                }
            }
        }
    }

    /** Langkah 2: verifikasi OTP + simpan email baru. */
    fun gantiEmail() {
        val state = _uiState.value
        if (state.otp.trim().length < 6) {
            _uiState.update { it.copy(errorMessage = "Kode verifikasi harus 6 digit") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = ApiProfil.gantiEmail(
                emailBaru = state.emailBaru,
                otp = state.otp,
            )

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Gagal mengganti email",
                    )
                }
            }
        }
    }
}
