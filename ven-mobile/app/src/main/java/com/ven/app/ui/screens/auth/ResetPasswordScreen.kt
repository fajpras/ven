package com.ven.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.ven.app.ui.components.ui.VexPasswordField
import com.ven.app.ui.components.ui.VexTextFieldVariant
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Layar Buat Kata Sandi Baru (Halaman 3).
 *
 * Sesuai alur reset kata sandi:
 * - Pola SubPage: [VexTopBar.Back] dengan judul "Reset Password".
 * - Input kata sandi baru dan konfirmasi kata sandi tipe Boxed.
 * - Checklist pemenuhan syarat keamanan password.
 * - Tombol CTA "Reset Password" yang setelah sukses akan membawa pengguna ke halaman tujuan.
 */
@Composable
fun ResetPasswordScreen(
    state: ForgotPasswordUiState,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onResetClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        // TopBar SubPage
        VexTopBar.Back(
            title = stringResource(R.string.reset_password_title),
            onBackClick = onBackClick,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = VexSpace.s6),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = VexSpace.s6, bottom = 100.dp),
            ) {
                Text(
                    text = stringResource(R.string.reset_password_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(bottom = VexSpace.s6),
                )

                // Label Kata Sandi Baru
                Text(
                    text = stringResource(R.string.reset_password_new_label),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = colors.text,
                    modifier = Modifier.padding(bottom = VexSpace.s2),
                )

                VexPasswordField(
                    value = state.newPassword,
                    onValueChange = onNewPasswordChange,
                    placeholder = stringResource(R.string.reset_password_new_label),
                    variant = VexTextFieldVariant.Boxed,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next,
                    ),
                )

                // Checklist persyaratan kata sandi
                if (state.newPassword.isNotEmpty()) {
                    Spacer(Modifier.height(VexSpace.s2))
                    ResetPasswordRequirements(
                        password = state.newPassword,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = VexSpace.s2),
                    )
                }

                Spacer(modifier = Modifier.height(VexSpace.s5))

                // Label Konfirmasi Kata Sandi
                Text(
                    text = stringResource(R.string.reset_password_confirm_label),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = colors.text,
                    modifier = Modifier.padding(bottom = VexSpace.s2),
                )

                VexPasswordField(
                    value = state.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    placeholder = stringResource(R.string.reset_password_confirm_label),
                    variant = VexTextFieldVariant.Boxed,
                    isError = state.confirmPassword.isNotEmpty() && !state.isPasswordMatch,
                    errorMessage = stringResource(R.string.register_error_confirm),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                    ),
                )

                state.errorMessage?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.danger,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = VexSpace.s2),
                    )
                }
            }

            // Tombol CTA Reset Password
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(bottom = VexSpace.s6),
            ) {
                VexButton(
                    text = stringResource(R.string.reset_password_button),
                    onClick = onResetClick,
                    variant = VexButtonVariant.Primary,
                    loading = state.isLoading,
                    enabled = state.canReset,
                )
            }
        }
    }
}

@Composable
private fun ResetPasswordRequirements(password: String, modifier: Modifier = Modifier) {
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

@Preview(name = "ResetPassword - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun ResetPasswordDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        ResetPasswordScreen(
            state = ForgotPasswordUiState(newPassword = "Pass123!", confirmPassword = "Pass123!"),
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onResetClick = {},
            onBackClick = {},
        )
    }
}

@Preview(name = "ResetPassword - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ResetPasswordLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        ResetPasswordScreen(
            state = ForgotPasswordUiState(),
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onResetClick = {},
            onBackClick = {},
        )
    }
}
