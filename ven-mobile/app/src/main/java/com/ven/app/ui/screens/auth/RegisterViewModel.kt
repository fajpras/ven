package com.ven.app.ui.screens.auth

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
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
        PasswordRule("Ada simbol (contoh: ! @ # \$ %)", password.any { !it.isLetterOrDigit() }),
    )

    fun isStrong(password: String): Boolean = rules(password).all { it.isMet }
}

private val EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$".toRegex()

data class RegisterUiState(
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val otp: String = "",
    val isOtpDialogOpen: Boolean = false,
    val isLoading: Boolean = false,
    val isSendingOtp: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
) {
    val isEmailValid: Boolean get() = EMAIL_REGEX.matches(email.trim())
    val isUsernameValid: Boolean get() = username.trim().length >= 3
    val isPasswordStrong: Boolean get() = PasswordValidator.isStrong(password)
    val isPasswordMatch: Boolean get() = password == confirmPassword

    val canSubmit: Boolean
        get() = isEmailValid && isUsernameValid && isPasswordStrong &&
                confirmPassword.isNotEmpty() && isPasswordMatch && !isLoading
}

sealed interface RegisterEvent {
    data object NavigateToOnboarding : RegisterEvent
}

class RegisterViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<RegisterEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

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
        _uiState.update { it.copy(isOtpDialogOpen = false, otp = "", errorMessage = null) }

    /** Langkah 1: Validasi form lalu kirim OTP ke email */
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

    /** Kirim ulang OTP dari dalam dialog */
    fun resendOtp() {
        val email = _uiState.value.email
        if (email.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSendingOtp = true, errorMessage = null) }

            val result = ApiRegister.kirimOtp(email)

            result.onSuccess { message ->
                _uiState.update { it.copy(isSendingOtp = false, infoMessage = message) }
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

    /** Langkah 2: Kirim OTP + data registrasi ke backend */
    fun submitRegisterWithOtp(context: Context) {
        val state = _uiState.value
        if (state.otp.trim().length < 6) {
            _uiState.update { it.copy(errorMessage = "Kode verifikasi harus 6 digit") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val displayName = state.username.trim()
            val result = ApiRegister.register(
                nama = displayName,
                username = displayName,
                email = state.email.trim(),
                kataSandi = state.password,
                otp = state.otp.trim()
            )

            result.onSuccess { authData ->
                val sessionManager = SessionManager.getInstance(context)
                sessionManager.saveToken(authData.token)
                sessionManager.saveUser(authData.pengguna)

                _uiState.update { it.copy(isLoading = false, isOtpDialogOpen = false) }
                _events.send(RegisterEvent.NavigateToOnboarding)
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

    /** Daftar menggunakan akun Google */
    fun registerWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val googleResult = GoogleAuthHelper.getGoogleIdToken(context)

            googleResult.onSuccess { idToken ->
                // Coba daftar dulu, jika sudah ada akun langsung login
                val registerResult = ApiRegister.registerGoogle(idToken = idToken)

                registerResult.onSuccess { authData ->
                    val sessionManager = SessionManager.getInstance(context)
                    sessionManager.saveToken(authData.token)
                    sessionManager.saveUser(authData.pengguna)

                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(RegisterEvent.NavigateToOnboarding)
                }.onFailure {
                    // Akun mungkin sudah terdaftar, coba login Google
                    val loginResult = ApiLogin.loginGoogle(idToken = idToken)
                    loginResult.onSuccess { authData ->
                        val sessionManager = SessionManager.getInstance(context)
                        sessionManager.saveToken(authData.token)
                        sessionManager.saveUser(authData.pengguna)

                        _uiState.update { it.copy(isLoading = false) }
                        _events.send(RegisterEvent.NavigateToOnboarding)
                    }.onFailure { loginException ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = loginException.message ?: "Gagal mendaftar dengan Google"
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

/** Penghubung antara RegisterViewModel dan RegisterScreen (stateless). */
@Composable
fun RegisterRoute(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onGoogleClick: () -> Unit = onNavigateToOnboarding,
    viewModel: RegisterViewModel = viewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                RegisterEvent.NavigateToOnboarding -> onNavigateToOnboarding()
            }
        }
    }

    RegisterScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onRegisterClick = viewModel::requestOtpAndOpenDialog,
        onGoogleClick = { viewModel.registerWithGoogle(context) },
        onLoginClick = onNavigateToLogin,
        onOtpChange = viewModel::onOtpChange,
        onSubmitOtpClick = { viewModel.submitRegisterWithOtp(context) },
        onResendOtpClick = viewModel::resendOtp,
        onDismissOtpDialog = viewModel::dismissOtpDialog,
    )
}
