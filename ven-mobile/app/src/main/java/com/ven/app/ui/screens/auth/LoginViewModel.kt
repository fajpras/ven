package com.ven.app.ui.screens.auth

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ven.app.data.api.ApiLogin
import com.ven.app.data.api.ApiRegister
import com.ven.app.data.api.SessionManager
import com.ven.app.util.GoogleAuthHelper
import kotlinx.coroutines.launch

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

    fun login(context: Context, onSuccess: () -> Unit) {
        val identifier = state.identifier.trim()
        val password = state.password

        if (identifier.isBlank() || password.isBlank()) {
            state = state.copy(errorMessage = "Email/Username dan Password tidak boleh kosong")
            return
        }

        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)

            val result = ApiLogin.login(identitas = identifier, kataSandi = password)

            result.onSuccess { authData ->
                val sessionManager = SessionManager.getInstance(context)
                sessionManager.saveToken(authData.token)
                sessionManager.saveUser(authData.pengguna)

                state = state.copy(isLoading = false)
                onSuccess()
            }.onFailure { exception ->
                state = state.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Login gagal, silakan periksa data Anda"
                )
            }
        }
    }

    fun loginWithGoogle(context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            state = state.copy(isLoading = true, errorMessage = null)

            val googleResult = GoogleAuthHelper.getGoogleIdToken(context)

            googleResult.onSuccess { idToken ->
                val loginResult = ApiLogin.loginGoogle(idToken = idToken)

                loginResult.onSuccess { authData ->
                    val sessionManager = SessionManager.getInstance(context)
                    sessionManager.saveToken(authData.token)
                    sessionManager.saveUser(authData.pengguna)

                    state = state.copy(isLoading = false)
                    onSuccess()
                }.onFailure {
                    // Jika belum terdaftar, coba daftar otomatis via Google
                    val registerResult = ApiRegister.registerGoogle(idToken = idToken)
                    registerResult.onSuccess { authData ->
                        val sessionManager = SessionManager.getInstance(context)
                        sessionManager.saveToken(authData.token)
                        sessionManager.saveUser(authData.pengguna)

                        state = state.copy(isLoading = false)
                        onSuccess()
                    }.onFailure { registerException ->
                        state = state.copy(
                            isLoading = false,
                            errorMessage = registerException.message ?: "Gagal masuk menggunakan Google"
                        )
                    }
                }
            }.onFailure { exception ->
                state = state.copy(
                    isLoading = false,
                    errorMessage = exception.message ?: "Autentikasi Google dibatalkan"
                )
            }
        }
    }
}
