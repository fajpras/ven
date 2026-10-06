package com.example.venmobile.ui.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.venmobile.R
import com.example.venmobile.ui.theme.VenMobileTheme

private val PurpleButton = Color(0xFFBA18F5)
private val FieldGray = Color(0xFFE6E6E6)
private val HintGray = Color(0xFFBDBDBD)

data class LoginUiState(
    val identifier: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
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
        // Simulate login
        state = state.copy(isLoading = false)
        onSuccess()
    }
}

@Composable
fun LoginScreen(
    state: LoginUiState,
    onIdentifierChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(56.dp))

        Text(
            text = "Login",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(Modifier.height(56.dp))

        PillTextField(
            value = state.identifier,
            onValueChange = onIdentifierChange,
            placeholder = "Email/Username",
            keyboardType = KeyboardType.Email
        )

        Spacer(Modifier.height(16.dp))

        PillTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            placeholder = "Password",
            keyboardType = KeyboardType.Password,
            isPassword = true
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Forgot Password?",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onForgotPasswordClick)
        )

        state.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = it,
                color = Color.Red,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onLoginClick,
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = PurpleButton,
                contentColor = Color.White
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Login", fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(24.dp))

        HorizontalDivider(color = Color.Black, thickness = 1.dp)

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = onGoogleClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, Color.Black),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google),
                    contentDescription = "Google",
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterStart)
                )
                Text(
                    text = "Register with Google",
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = "Don't have an account?",
            fontSize = 14.sp,
            color = Color.Black
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            Text("Register", fontSize = 16.sp)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun PillTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    isPassword: Boolean = false,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = HintGray) },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(50),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = FieldGray,
            unfocusedContainerColor = FieldGray,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = PurpleButton
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    )
}

@Composable
fun LoginRoute(
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onForgotPassword: () -> Unit = {}
) {
    val viewModel: LoginViewModel = viewModel()
    LoginScreen(
        state = viewModel.state,
        onIdentifierChange = viewModel::onIdentifierChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = { viewModel.login(onNavigateToHome) },
        onForgotPasswordClick = onForgotPassword,
        onGoogleClick = {},
        onRegisterClick = onNavigateToRegister
    )
}

@Preview(showBackground = true, widthDp = 411, heightDp = 923)
@Composable
private fun LoginScreenPreview() {
    VenMobileTheme {
        LoginScreen(
            state = LoginUiState(),
            onIdentifierChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onForgotPasswordClick = {},
            onGoogleClick = {},
            onRegisterClick = {}
        )
    }
}
