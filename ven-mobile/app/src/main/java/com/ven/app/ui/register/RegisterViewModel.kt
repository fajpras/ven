package com.ven.app.ui.register

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ven.app.data.api.ApiLogin
import com.ven.app.data.api.ApiRegister
import com.ven.app.data.api.SessionManager
import com.ven.app.util.GoogleAuthHelper
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PasswordRule(val label: String, val isMet: Boolean)

object PasswordValidator {

    private const val MIN_LENGTH = 8

    fun rules(password: String): List<PasswordRule> = listOf(
        PasswordRule("Minimal $MIN_LENGTH karakter", password.length >= MIN_LENGTH),
        PasswordRule("Ada huruf besar (A-Z)", password.any { it.isUpperCase() }),
        PasswordRule("Ada huruf kecil (a-z)", password.any { it.isLowerCase() }),
        PasswordRule("Ada angka (0-9)", password.any { it.isDigit() }),
        PasswordRule("Ada simbol (contoh: ! @ # $ %)", password.any { !it.isLetterOrDigit() }),
    )

    fun isStrong(password: String): Boolean = rules(password).all { it.isMet }
}

data class RegisterUiState(
    val name: String = "",
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val otp: String = "",
    val isOtpDialogOpen: Boolean = false,
    val isLoading: Boolean = false,
    val isSendingOtp: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
) {
    val isEmailValid: Boolean get() = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val isUsernameValid: Boolean get() = username.trim().length >= 3
    val isPasswordStrong: Boolean get() = PasswordValidator.isStrong(password)
    val isPasswordMatch: Boolean get() = password == confirmPassword

    val canSubmit: Boolean
        get() = isEmailValid && isUsernameValid && isPasswordStrong &&
                confirmPassword.isNotEmpty() && isPasswordMatch && !isLoading
}

sealed interface RegisterEvent {
    data object NavigateToHome : RegisterEvent
}

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<RegisterEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onNameChange(value: String) =
        _uiState.update { it.copy(name = value, errorMessage = null) }

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, errorMessage = null) }

    fun onUsernameChange(value: String) =
        _uiState.update { it.copy(username = value, errorMessage = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, errorMessage = null) }

    fun onConfirmPasswordChange(value: String) =
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }

    fun onOtpChange(value: String) =
        _uiState.update { it.copy(otp = value, errorMessage = null) }

    fun dismissOtpDialog() =
        _uiState.update { it.copy(isOtpDialogOpen = false, errorMessage = null) }

    fun requestOtpAndOpenDialog() {
        val state = _uiState.value
        if (!state.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }

            val result = ApiRegister.kirimOtp(state.email)

            result.onSuccess { message ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isOtpDialogOpen = true,
                        infoMessage = message
                    )
                }
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
        val email = _uiState.value.email
        if (email.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSendingOtp = true, errorMessage = null) }

            val result = ApiRegister.kirimOtp(email)

            result.onSuccess { message ->
                _uiState.update {
                    it.copy(isSendingOtp = false, infoMessage = message)
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isSendingOtp = false,
                        errorMessage = exception.message ?: "Gagal mengirim ulang kode"
                    )
                }
            }
        }
    }

    fun submitRegisterWithOtp(context: Context) {
        val state = _uiState.value
        val otpCode = state.otp.trim()

        if (otpCode.length < 6) {
            _uiState.update { it.copy(errorMessage = "Kode verifikasi harus 6 digit") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val displayName = state.name.trim().ifBlank { state.username.trim() }
            val result = ApiRegister.register(
                nama = displayName,
                username = state.username.trim(),
                email = state.email.trim(),
                kataSandi = state.password,
                otp = otpCode
            )

            result.onSuccess { authData ->
                val sessionManager = SessionManager.getInstance(context)
                sessionManager.saveToken(authData.token)
                sessionManager.saveUser(authData.pengguna)

                _uiState.update { it.copy(isLoading = false, isOtpDialogOpen = false) }
                _events.send(RegisterEvent.NavigateToHome)
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Registrasi gagal, coba lagi"
                    )
                }
            }
        }
    }

    fun registerWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val googleResult = GoogleAuthHelper.getGoogleIdToken(context)

            googleResult.onSuccess { idToken ->
                val registerResult = ApiRegister.registerGoogle(idToken = idToken)

                registerResult.onSuccess { authData ->
                    val sessionManager = SessionManager.getInstance(context)
                    sessionManager.saveToken(authData.token)
                    sessionManager.saveUser(authData.pengguna)

                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(RegisterEvent.NavigateToHome)
                }.onFailure { regException ->
                    val loginResult = ApiLogin.loginGoogle(idToken = idToken)
                    loginResult.onSuccess { authData ->
                        val sessionManager = SessionManager.getInstance(context)
                        sessionManager.saveToken(authData.token)
                        sessionManager.saveUser(authData.pengguna)

                        _uiState.update { it.copy(isLoading = false) }
                        _events.send(RegisterEvent.NavigateToHome)
                    }.onFailure {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = regException.message ?: "Gagal mendaftar dengan Google"
                            )
                        }
                    }
                }
            }.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Autentikasi Google dibatalkan"
                    )
                }
            }
        }
    }
}
