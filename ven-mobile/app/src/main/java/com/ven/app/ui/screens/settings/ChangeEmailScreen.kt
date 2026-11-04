package com.ven.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ven.app.R
import com.ven.app.ui.components.layout.VexBottomActionBar
import com.ven.app.ui.components.layout.VexTopBar
import com.ven.app.ui.components.ui.VexButton
import com.ven.app.ui.components.ui.VexButtonVariant
import com.ven.app.ui.components.ui.VexPasswordField
import com.ven.app.ui.components.ui.VexTextField
import com.ven.app.ui.components.ui.VexTextFieldVariant
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Halaman Change Email.
 * Sesuai wireframe `Wireframe - Setting Email.png` dan DESIGN.md §8.9:
 * - Field: New email, Password, Confirm password.
 * - Helper text: "You need to verify your new email for the change to be applied".
 * - Sticky bottom CTA: [VexBottomActionBar] dengan tombol "Send OTP".
 */
@Composable
fun ChangeEmailScreen(
    state: ChangeEmailUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSendOtpClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.bg,
        topBar = {
            VexTopBar.Back(
                title = stringResource(R.string.settings_item_change_email),
                onBackClick = onBackClick,
            )
        },
        bottomBar = {
            VexBottomActionBar {
                VexButton(
                    text = stringResource(R.string.settings_send_otp),
                    onClick = onSendOtpClick,
                    variant = VexButtonVariant.Primary,
                    enabled = state.isFormValid,
                    loading = state.isLoading,
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.bg)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = VexSpace.s6, vertical = VexSpace.s5)
                .padding(bottom = VexSpace.s8),
        ) {
            VexTextField(
                value = state.emailBaru,
                onValueChange = onEmailChange,
                variant = VexTextFieldVariant.Boxed,
                label = stringResource(R.string.settings_new_email_label),
            )

            Spacer(modifier = Modifier.height(VexSpace.s4))

            VexPasswordField(
                value = state.kataSandi,
                onValueChange = onPasswordChange,
                variant = VexTextFieldVariant.Boxed,
                label = stringResource(R.string.settings_password_label),
            )

            Spacer(modifier = Modifier.height(VexSpace.s4))

            VexPasswordField(
                value = state.konfirmasiKataSandi,
                onValueChange = onConfirmPasswordChange,
                variant = VexTextFieldVariant.Boxed,
                label = stringResource(R.string.settings_confirm_password_label),
            )

            Spacer(modifier = Modifier.height(VexSpace.s4))

            Text(
                text = stringResource(R.string.settings_change_email_helper),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
            )

            state.errorMessage?.let {
                Spacer(modifier = Modifier.height(VexSpace.s3))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.danger,
                )
            }
        }
    }
}

/**
 * Menghubungkan [ChangeEmailScreen] dengan [ChangeEmailViewModel] yang dibagikan.
 * Setelah OTP terbit, memicu [onOtpSent] untuk berpindah ke layar verifikasi.
 */
@Composable
fun ChangeEmailRoute(
    viewModel: ChangeEmailViewModel,
    onBackClick: () -> Unit,
    onOtpSent: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isOtpSent) {
        if (state.isOtpSent) {
            viewModel.onOtpConsumed()
            onOtpSent()
        }
    }

    ChangeEmailScreen(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onKataSandiChange,
        onConfirmPasswordChange = viewModel::onKonfirmasiChange,
        onBackClick = onBackClick,
        onSendOtpClick = viewModel::kirimOtp,
    )
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

@Preview(name = "ChangeEmail - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun ChangeEmailDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        ChangeEmailScreen(
            state = ChangeEmailUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onBackClick = {},
            onSendOtpClick = {},
        )
    }
}

@Preview(name = "ChangeEmail - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ChangeEmailLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        ChangeEmailScreen(
            state = ChangeEmailUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onBackClick = {},
            onSendOtpClick = {},
        )
    }
}
