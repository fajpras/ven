package com.ven.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.components.layout.VexTopBar
import com.ven.app.ui.components.ui.VexButton
import com.ven.app.ui.components.ui.VexButtonVariant
import com.ven.app.ui.components.ui.VexTextField
import com.ven.app.ui.components.ui.VexTextFieldVariant
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Layar Masukkan Email untuk alur Lupa Kata Sandi (Halaman 1).
 *
 * Sesuai spesifikasi DESIGN.md §8.11 [Figma node 4372:689]:
 * - Pola SubPage: [VexTopBar.Back] dengan ikon kembali, judul "Forgot Password", dan garis bawah 1dp.
 * - Form: Input Boxed untuk Email berlabel "Email".
 * - CTA Bawah: Tombol "Send OTP" pill BrandPurple dengan status loading.
 */
@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSendOtpClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        // TopBar SubPage dengan tombol kembali
        VexTopBar.Back(
            title = stringResource(R.string.forgot_password_title),
            onBackClick = onBackClick,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = VexSpace.s6),
        ) {
            // Konten Form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = VexSpace.s8, bottom = 100.dp),
            ) {
                Text(
                    text = stringResource(R.string.forgot_password_email_label),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = colors.text,
                    modifier = Modifier.padding(bottom = VexSpace.s2),
                )

                VexTextField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    placeholder = stringResource(R.string.forgot_password_email_hint),
                    variant = VexTextFieldVariant.Boxed,
                    isError = state.errorMessage != null,
                    errorMessage = state.errorMessage ?: "",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done,
                    ),
                )
            }

            // Tombol CTA Send OTP menempel di bagian bawah
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(bottom = VexSpace.s6),
            ) {
                VexButton(
                    text = stringResource(R.string.forgot_password_send_otp),
                    onClick = onSendOtpClick,
                    variant = VexButtonVariant.Primary,
                    loading = state.isLoading,
                    enabled = state.email.isNotBlank(),
                )
            }
        }
    }
}

@Preview(name = "ForgotPassword - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun ForgotPasswordDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        ForgotPasswordScreen(
            state = ForgotPasswordUiState(email = "user@example.com"),
            onEmailChange = {},
            onSendOtpClick = {},
            onBackClick = {},
        )
    }
}

@Preview(name = "ForgotPassword - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ForgotPasswordLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        ForgotPasswordScreen(
            state = ForgotPasswordUiState(),
            onEmailChange = {},
            onSendOtpClick = {},
            onBackClick = {},
        )
    }
}
