package com.ven.app.ui.screens.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

class LoginViewModel : ViewModel() {
    var state by mutableStateOf(LoginUiState())
        private set

    fun onIdentifierChange(value: String) {
        state = state.copy(identifier = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        state = state.copy(password = value, errorMessage = null)
    }

    fun login(onSuccess: () -> Unit) {
        if (state.identifier.isBlank() || state.password.isBlank()) {
            state = state.copy(errorMessage = "Email/Username and Password cannot be empty")
            return
        }
        state = state.copy(isLoading = true, errorMessage = null)
        // Simulate login (frontend-only)
        state = state.copy(isLoading = false)
        onSuccess()
    }
}
