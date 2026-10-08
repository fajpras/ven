package com.ven.app.ui.register

import android.util.Patterns
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
    val email: String = "",
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
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

    fun onEmailChange(value: String) =
        _uiState.update { it.copy(email = value, errorMessage = null) }

    fun onUsernameChange(value: String) =
        _uiState.update { it.copy(username = value, errorMessage = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, errorMessage = null) }

    fun onConfirmPasswordChange(value: String) =
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }

    fun onRegisterClick() {
        val state = _uiState.value
        if (!state.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            delay(1000)
            val success = true

            if (success) {
                _uiState.update { it.copy(isLoading = false) }
                _events.send(RegisterEvent.NavigateToHome)
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Registrasi gagal, coba lagi") }
            }
        }
    }
}

/** Penghubung antara RegisterViewModel dan RegisterScreen (stateless). */
@Composable
fun RegisterRoute(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onGoogleClick: () -> Unit = {},
    viewModel: RegisterViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                RegisterEvent.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    RegisterScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onUsernameChange = viewModel::onUsernameChange,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onRegisterClick = viewModel::onRegisterClick,
        onGoogleClick = onGoogleClick,
        onLoginClick = onNavigateToLogin,
    )
}
