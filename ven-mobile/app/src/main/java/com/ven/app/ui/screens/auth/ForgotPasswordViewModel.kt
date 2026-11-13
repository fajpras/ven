package com.ven.app.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ven.app.data.api.ApiLogin
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

data class ForgotPasswordUiState(
    val email: String = "",
    val otp: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val resendCooldown: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val isEmailValid: Boolean get() = EMAIL_REGEX.matches(email.trim())
    val isOtpComplete: Boolean get() = otp.length == 6
    val isPasswordStrong: Boolean get() = PasswordValidator.isStrong(newPassword)
    val isPasswordMatch: Boolean get() = newPassword == confirmPassword
    val canReset: Boolean
        get() = isPasswordStrong && confirmPassword.isNotEmpty() && isPasswordMatch && !isLoading
}

sealed interface ForgotPasswordEvent {
    data object NavigateToVerifyOtp : ForgotPasswordEvent
    data object NavigateToResetPassword : ForgotPasswordEvent
    data object NavigateToLogin : ForgotPasswordEvent
    data object NavigateToHome : ForgotPasswordEvent
}

class ForgotPasswordViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<ForgotPasswordEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var timerJob: Job? = null

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onOtpChange(value: String) {
        _uiState.update { it.copy(otp = value, errorMessage = null) }
    }

    fun onNewPasswordChange(value: String) {
        _uiState.update { it.copy(newPassword = value, errorMessage = null) }
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }
    }

    fun sendOtp() {
        val state = _uiState.value
        if (!state.isEmailValid) {
            _uiState.update { it.copy(errorMessage = "Format email tidak valid") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = ApiLogin.lupaKataSandi(state.email)

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                startCooldownTimer()
                _events.send(ForgotPasswordEvent.NavigateToVerifyOtp)
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Gagal mengirim kode verifikasi"
                    )
                }
            }
        }
    }

    fun resendOtp() {
        if (_uiState.value.resendCooldown > 0) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = ApiLogin.lupaKataSandi(_uiState.value.email)

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                startCooldownTimer()
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Gagal mengirim ulang kode"
                    )
                }
            }
        }
    }

    fun verifyOtp() {
        val state = _uiState.value
        if (!state.isOtpComplete) {
            _uiState.update { it.copy(errorMessage = "Kode OTP harus 6 digit") }
            return
        }

        // Backend memverifikasi OTP menyatu dengan reset kata sandi,
        // jadi di sini cukup validasi lokal lalu lanjut ke layar kata sandi baru.
        _uiState.update { it.copy(errorMessage = null) }
        viewModelScope.launch {
            _events.send(ForgotPasswordEvent.NavigateToResetPassword)
        }
    }

    fun resetPassword(toHome: Boolean = false) {
        val state = _uiState.value
        if (!state.canReset) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = ApiLogin.resetKataSandi(
                email = state.email,
                otp = state.otp,
                kataSandiBaru = state.newPassword,
            )

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false) }
                _events.send(
                    if (toHome) ForgotPasswordEvent.NavigateToHome
                    else ForgotPasswordEvent.NavigateToLogin
                )
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Gagal mengubah kata sandi"
                    )
                }
            }
        }
    }

    private fun startCooldownTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            _uiState.update { it.copy(resendCooldown = 60) }
            while (_uiState.value.resendCooldown > 0) {
                delay(1000)
                _uiState.update { it.copy(resendCooldown = (it.resendCooldown - 1).coerceAtLeast(0)) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
