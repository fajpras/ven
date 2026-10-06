package com.example.venmobile.ui.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.venmobile.R
import com.example.venmobile.ui.components.PillTextField

private val PurpleButton = Color(0xFFBA18F5)

@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onEmailChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onLoginClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(56.dp))

        Text(
            text = "Register",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(Modifier.height(48.dp))

        // Email
        PillTextField(
            value = state.email,
            onValueChange = onEmailChange,
            placeholder = "Email",
            keyboardType = KeyboardType.Email
        )
        if (state.email.isNotEmpty() && !state.isEmailValid) {
            ErrorText("Format email tidak valid")
        }

        Spacer(Modifier.height(16.dp))

        // Username
        PillTextField(
            value = state.username,
            onValueChange = onUsernameChange,
            placeholder = "Username"
        )
        if (state.username.isNotEmpty() && !state.isUsernameValid) {
            ErrorText("Username minimal 3 karakter")
        }

        Spacer(Modifier.height(16.dp))

        // Password + checklist (muncul saat mulai mengetik)
        PillTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            placeholder = "Password",
            keyboardType = KeyboardType.Password,
            isPassword = true
        )
        if (state.password.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            PasswordRequirements(
                password = state.password,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Confirm password
        PillTextField(
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            placeholder = "Confirm Password",
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            isPassword = true
        )
        if (state.confirmPassword.isNotEmpty() && !state.isPasswordMatch) {
            ErrorText("Password tidak sama")
        }

        state.errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            ErrorText(it)
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onRegisterClick,
            enabled = state.canSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = PurpleButton,
                contentColor = Color.White,
                disabledContainerColor = PurpleButton.copy(alpha = 0.4f),
                disabledContentColor = Color.White
            )
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Register", fontSize = 16.sp)
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
                    text = "Login with Google",
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(Modifier.height(64.dp))

        Text(
            text = "Already have an account?",
            fontSize = 14.sp,
            color = Color.Black
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black,
                contentColor = Color.White
            )
        ) {
            Text("Login", fontSize = 16.sp)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun PasswordRequirements(password: String, modifier: Modifier = Modifier) {
    val rules = PasswordValidator.rules(password)
    Column(modifier = modifier) {
        rules.forEach { rule ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Text(
                    text = if (rule.isMet) "✓" else "•",
                    color = if (rule.isMet) Color(0xFF4CAF50) else Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(20.dp)
                )
                Text(
                    text = rule.label,
                    color = if (rule.isMet) Color(0xFF4CAF50) else Color.Gray,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun ErrorText(message: String) {
    Text(
        text = message,
        color = Color.Red,
        fontSize = 13.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 4.dp)
    )
}

@Preview(showBackground = true, widthDp = 411, heightDp = 923)
@Composable
private fun RegisterScreenPreview() {
    MaterialTheme {
        RegisterScreen(
            state = RegisterUiState(),
            onEmailChange = {}, onUsernameChange = {},
            onPasswordChange = {}, onConfirmPasswordChange = {},
            onRegisterClick = {}, onGoogleClick = {}, onLoginClick = {}
        )
    }
}
