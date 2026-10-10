package com.ven.app.ui.register

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ven.app.R
import com.ven.app.ui.components.PillTextField

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
    onOtpChange: (String) -> Unit = {},
    onSubmitOtpClick: () -> Unit = {},
    onResendOtpClick: () -> Unit = {},
    onDismissOtpDialog: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
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

            PillTextField(
                value = state.username,
                onValueChange = onUsernameChange,
                placeholder = "Username"
            )
            if (state.username.isNotEmpty() && !state.isUsernameValid) {
                ErrorText("Username minimal 3 karakter")
            }

            Spacer(Modifier.height(16.dp))

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
                if (!state.isOtpDialogOpen) {
                    Spacer(Modifier.height(8.dp))
                    ErrorText(it)
                }
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

            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)

            Spacer(Modifier.height(24.dp))

            OutlinedButton(
                onClick = onGoogleClick,
                enabled = !state.isLoading,
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
                        text = "Sign up with Google",
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

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

        if (state.isOtpDialogOpen) {
            AlertDialog(
                onDismissRequest = onDismissOtpDialog,
                title = {
                    Text(
                        text = "Verifikasi Email",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Masukkan 6 digit kode OTP yang telah dikirim ke ${state.email}",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )

                        Spacer(Modifier.height(16.dp))

                        OutlinedTextField(
                            value = state.otp,
                            onValueChange = { if (it.length <= 6) onOtpChange(it) },
                            placeholder = { Text("Kode OTP (6 digit)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        state.errorMessage?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp
                            )
                        }

                        state.infoMessage?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = it,
                                color = Color(0xFF2E7D32),
                                fontSize = 13.sp
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        TextButton(
                            onClick = onResendOtpClick,
                            enabled = !state.isSendingOtp,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = if (state.isSendingOtp) "Mengirim..." else "Kirim Ulang Kode",
                                fontSize = 13.sp,
                                color = PurpleButton
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onSubmitOtpClick,
                        enabled = !state.isLoading && state.otp.length >= 6,
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleButton)
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Verifikasi & Selesai")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissOtpDialog) {
                        Text("Batal", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
fun RegisterRoute(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: RegisterViewModel = viewModel(),
) {
    val context = LocalContext.current
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
        onRegisterClick = viewModel::requestOtpAndOpenDialog,
        onGoogleClick = {
            viewModel.registerWithGoogle(context)
        },
        onLoginClick = onNavigateToLogin,
        onOtpChange = viewModel::onOtpChange,
        onSubmitOtpClick = {
            viewModel.submitRegisterWithOtp(context)
        },
        onResendOtpClick = viewModel::resendOtp,
        onDismissOtpDialog = viewModel::dismissOtpDialog
    )
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
            onEmailChange = {},
            onUsernameChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onRegisterClick = {},
            onGoogleClick = {},
            onLoginClick = {}
        )
    }
}
