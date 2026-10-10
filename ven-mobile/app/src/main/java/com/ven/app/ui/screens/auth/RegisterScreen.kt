package com.ven.app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.components.layout.AuthLayout
import com.ven.app.ui.components.ui.OtpInput
import com.ven.app.ui.components.ui.VexButton
import com.ven.app.ui.components.ui.VexButtonVariant
import com.ven.app.ui.components.ui.VexPasswordField
import com.ven.app.ui.components.ui.VexTextField
import com.ven.app.ui.components.ui.VexTextFieldVariant
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Layar Register Aplikasi VEN (Virtual Exhibition).
 * Sesuai spesifikasi DESIGN.md §8.3 (Wireframe - Register.png).
 */
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
    onDismissOtpDialog: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    if (state.isOtpDialogOpen) {
        AlertDialog(
            onDismissRequest = onDismissOtpDialog,
            title = {
                Text(
                    text = stringResource(R.string.verify_otp_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.text,
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.verify_otp_subtitle, state.email),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(Modifier.height(VexSpace.s4))

                    OtpInput(
                        value = state.otp,
                        onValueChange = onOtpChange,
                        digitCount = 6,
                        isError = state.errorMessage != null,
                        onOtpComplete = { onSubmitOtpClick() },
                    )

                    state.errorMessage?.let {
                        Spacer(Modifier.height(VexSpace.s2))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.danger,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    state.infoMessage?.let {
                        Spacer(Modifier.height(VexSpace.s2))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.success,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    Spacer(Modifier.height(VexSpace.s4))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(R.string.verify_otp_didnt_receive),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary,
                        )
                        Spacer(Modifier.width(4.dp))
                        VexButton(
                            text = stringResource(R.string.verify_otp_resend),
                            onClick = onResendOtpClick,
                            variant = VexButtonVariant.TextLink,
                            enabled = !state.isSendingOtp,
                        )
                    }
                }
            },
            confirmButton = {
                VexButton(
                    text = stringResource(R.string.verify_otp_button),
                    onClick = onSubmitOtpClick,
                    variant = VexButtonVariant.Primary,
                    loading = state.isLoading,
                    enabled = state.otp.trim().length >= 6,
                )
            },
            dismissButton = {
                VexButton(
                    text = "Batal",
                    onClick = onDismissOtpDialog,
                    variant = VexButtonVariant.Outline,
                )
            },
            containerColor = colors.surface,
        )
    }

    AuthLayout(
        title = stringResource(R.string.register_title),
        modifier = modifier,
        formContent = {
            // Email
            VexTextField(
                value = state.email,
                onValueChange = onEmailChange,
                placeholder = stringResource(R.string.auth_placeholder_email),
                variant = VexTextFieldVariant.Pill,
                authDarkMode = true,
                isError = state.email.isNotEmpty() && !state.isEmailValid,
                errorMessage = stringResource(R.string.register_error_email),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )

            Spacer(Modifier.height(VexSpace.s4))

            // Username
            VexTextField(
                value = state.username,
                onValueChange = onUsernameChange,
                placeholder = stringResource(R.string.auth_placeholder_username),
                variant = VexTextFieldVariant.Pill,
                authDarkMode = true,
                isError = state.username.isNotEmpty() && !state.isUsernameValid,
                errorMessage = stringResource(R.string.register_error_username),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
            )

            Spacer(Modifier.height(VexSpace.s4))

            // Password
            VexPasswordField(
                value = state.password,
                onValueChange = onPasswordChange,
                placeholder = stringResource(R.string.auth_placeholder_password),
                variant = VexTextFieldVariant.Pill,
                authDarkMode = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next,
                ),
            )

            if (state.password.isNotEmpty()) {
                Spacer(Modifier.height(VexSpace.s2))
                PasswordRequirements(
                    password = state.password,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = VexSpace.s4),
                )
            }

            Spacer(Modifier.height(VexSpace.s4))

            // Confirm Password
            VexPasswordField(
                value = state.confirmPassword,
                onValueChange = onConfirmPasswordChange,
                placeholder = stringResource(R.string.auth_placeholder_confirm_password),
                variant = VexTextFieldVariant.Pill,
                authDarkMode = true,
                isError = state.confirmPassword.isNotEmpty() && !state.isPasswordMatch,
                errorMessage = stringResource(R.string.register_error_confirm),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
            )

            state.errorMessage?.let {
                Spacer(Modifier.height(VexSpace.s2))
                Text(
                    text = it,
                    color = colors.danger,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(VexSpace.s8))

            // Register Button
            VexButton(
                text = stringResource(R.string.register_button),
                onClick = onRegisterClick,
                variant = VexButtonVariant.Primary,
                enabled = state.canSubmit,
                loading = state.isLoading,
            )

            Spacer(Modifier.height(VexSpace.s6))

            HorizontalDivider(
                color = colors.border,
                thickness = 1.dp,
            )

            Spacer(Modifier.height(VexSpace.s6))

            // Google Sign In
            VexButton(
                text = stringResource(R.string.google_sign_in),
                onClick = onGoogleClick,
                variant = VexButtonVariant.Outline,
                icon = R.drawable.ic_google,
            )
        },
        footerContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.already_have_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.text,
                )

                Spacer(Modifier.height(VexSpace.s3))

                VexButton(
                    text = stringResource(R.string.login_button),
                    onClick = onLoginClick,
                    variant = VexButtonVariant.Inverse,
                )
            }
        },
    )
}

@Composable
private fun PasswordRequirements(password: String, modifier: Modifier = Modifier) {
    val colors = VexTheme.colors
    val rules = PasswordValidator.rules(password)

    Column(modifier = modifier) {
        rules.forEach { rule ->
            val color = if (rule.isMet) colors.success else colors.textMuted
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 2.dp),
            ) {
                Text(
                    text = if (rule.isMet) "✓" else "•",
                    color = color,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.width(20.dp),
                )
                Text(
                    text = rule.label,
                    color = color,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Preview(name = "RegisterScreen - Dark", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun RegisterScreenDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        RegisterScreen(
            state = RegisterUiState(),
            onEmailChange = {},
            onUsernameChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onRegisterClick = {},
            onGoogleClick = {},
            onLoginClick = {},
        )
    }
}

@Preview(name = "RegisterScreen - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun RegisterScreenLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        RegisterScreen(
            state = RegisterUiState(),
            onEmailChange = {},
            onUsernameChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onRegisterClick = {},
            onGoogleClick = {},
            onLoginClick = {},
        )
    }
}
