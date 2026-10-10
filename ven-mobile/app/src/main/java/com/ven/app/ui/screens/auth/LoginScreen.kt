package com.ven.app.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ven.app.R
import com.ven.app.ui.components.layout.AuthLayout
import com.ven.app.ui.components.ui.VexButton
import com.ven.app.ui.components.ui.VexButtonVariant
import com.ven.app.ui.components.ui.VexPasswordField
import com.ven.app.ui.components.ui.VexTextField
import com.ven.app.ui.components.ui.VexTextFieldVariant
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Layar Login Aplikasi VEN (Virtual Exhibition).
 * Sesuai spesifikasi DESIGN.md §8.2 (Figma node 3946:17).
 */
@Composable
fun LoginScreen(
    state: LoginUiState,
    onIdentifierChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onGoogleClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    AuthLayout(
        title = stringResource(R.string.login_title),
        modifier = modifier,
        formContent = {
            VexTextField(
                value = state.identifier,
                onValueChange = onIdentifierChange,
                placeholder = stringResource(R.string.auth_placeholder_email_username),
                variant = VexTextFieldVariant.Pill,
                authDarkMode = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
            )

            Spacer(Modifier.height(VexSpace.s4))

            VexPasswordField(
                value = state.password,
                onValueChange = onPasswordChange,
                placeholder = stringResource(R.string.auth_placeholder_password),
                variant = VexTextFieldVariant.Pill,
                authDarkMode = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
            )

            Spacer(Modifier.height(VexSpace.s4))

            Text(
                text = stringResource(R.string.forgot_password),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.text,
                textAlign = TextAlign.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onForgotPasswordClick),
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

            VexButton(
                text = stringResource(R.string.login_button),
                onClick = onLoginClick,
                variant = VexButtonVariant.Primary,
                loading = state.isLoading,
            )

            Spacer(Modifier.height(VexSpace.s6))

            HorizontalDivider(
                color = colors.border,
                thickness = 1.dp,
            )

            Spacer(Modifier.height(VexSpace.s6))

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
                    text = stringResource(R.string.dont_have_account),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.text,
                )

                Spacer(Modifier.height(VexSpace.s3))

                VexButton(
                    text = stringResource(R.string.register_button),
                    onClick = onRegisterClick,
                    variant = VexButtonVariant.Inverse,
                )
            }
        },
    )
}

@Composable
fun LoginRoute(
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onForgotPassword: () -> Unit,
    onGoogleClick: () -> Unit = onNavigateToHome,
    viewModel: LoginViewModel = viewModel(),
) {
    val context = LocalContext.current

    LoginScreen(
        state = viewModel.state,
        onIdentifierChange = viewModel::onIdentifierChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = { viewModel.login(context, onNavigateToHome) },
        onForgotPasswordClick = onForgotPassword,
        onGoogleClick = { viewModel.loginWithGoogle(context, onGoogleClick) },
        onRegisterClick = onNavigateToRegister,
    )
}

@Preview(name = "LoginScreen - Dark", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun LoginScreenDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        LoginScreen(
            state = LoginUiState(),
            onIdentifierChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onForgotPasswordClick = {},
            onGoogleClick = {},
            onRegisterClick = {},
        )
    }
}

@Preview(name = "LoginScreen - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun LoginScreenLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        LoginScreen(
            state = LoginUiState(),
            onIdentifierChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onForgotPasswordClick = {},
            onGoogleClick = {},
            onRegisterClick = {},
        )
    }
}
