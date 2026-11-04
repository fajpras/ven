package com.ven.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ven.app.R
import com.ven.app.ui.components.layout.VexTopBar
import com.ven.app.ui.components.ui.OtpInput
import com.ven.app.ui.components.ui.VexButton
import com.ven.app.ui.components.ui.VexButtonVariant
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme
import kotlinx.coroutines.delay

/**
 * Halaman Verifikasi Email melalui OTP.
 * Sesuai wireframe `Wireframe - Setting Email-1.png` dan DESIGN.md §8.9:
 * - Label "OTP".
 * - 6 kotak [OtpInput] (konsisten dengan arsitektur backend).
 * - Fitur resend dengan hitung mundur cooldown.
 */
@Composable
fun VerifyEmailScreen(
    email: String,
    state: ChangeEmailUiState,
    onOtpChange: (String) -> Unit,
    onVerifyClick: () -> Unit,
    onResendClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    var cooldown by remember { mutableIntStateOf(60) }

    LaunchedEffect(cooldown) {
        if (cooldown > 0) {
            delay(1000)
            cooldown -= 1
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.bg,
        topBar = {
            VexTopBar.Back(
                title = stringResource(R.string.settings_verify_email_title),
                onBackClick = onBackClick,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.bg)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = VexSpace.s6, vertical = VexSpace.s5),
        ) {
            Text(
                text = stringResource(R.string.verify_otp_label),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.text,
                modifier = Modifier.padding(bottom = VexSpace.s3),
            )

            // 6 Digit OtpInput
            OtpInput(
                value = state.otp,
                onValueChange = onOtpChange,
                digitCount = 6,
                isError = state.errorMessage != null,
                onOtpComplete = { onVerifyClick() },
            )

            state.errorMessage?.let {
                Spacer(modifier = Modifier.height(VexSpace.s2))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.danger,
                )
            }

            state.infoMessage?.let {
                Spacer(modifier = Modifier.height(VexSpace.s2))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.success,
                )
            }

            Spacer(modifier = Modifier.height(VexSpace.s6))

            // Info & Link Resend
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.settings_didnt_get_mail),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.text,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(VexSpace.s1))

                if (cooldown > 0) {
                    Text(
                        text = stringResource(R.string.verify_otp_resend_cooldown, cooldown),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = colors.textMuted,
                    )
                } else {
                    VexButton(
                        text = stringResource(R.string.settings_resend),
                        onClick = {
                            cooldown = 60
                            onResendClick()
                        },
                        variant = VexButtonVariant.TextLink,
                        enabled = !state.isLoading,
                    )
                }

                Spacer(modifier = Modifier.height(VexSpace.s4))

                VexButton(
                    text = stringResource(R.string.verify_otp_button),
                    onClick = onVerifyClick,
                    variant = VexButtonVariant.Primary,
                    loading = state.isLoading,
                    enabled = state.otp.trim().length >= 6,
                )
            }
        }
    }
}

/**
 * Menghubungkan [VerifyEmailScreen] dengan [ChangeEmailViewModel] yang dibagikan
 * dengan [ChangeEmailRoute]. Setelah verifikasi sukses, memicu [onVerified].
 */
@Composable
fun VerifyEmailRoute(
    viewModel: ChangeEmailViewModel,
    onBackClick: () -> Unit,
    onVerified: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onVerified()
        }
    }

    VerifyEmailScreen(
        email = state.emailBaru,
        state = state,
        onOtpChange = viewModel::onOtpChange,
        onVerifyClick = viewModel::gantiEmail,
        onResendClick = viewModel::kirimUlangOtp,
        onBackClick = onBackClick,
    )
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

@Preview(name = "VerifyEmail - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun VerifyEmailDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        VerifyEmailScreen(
            email = "user@example.com",
            state = ChangeEmailUiState(emailBaru = "user@example.com"),
            onOtpChange = {},
            onVerifyClick = {},
            onResendClick = {},
            onBackClick = {},
        )
    }
}

@Preview(name = "VerifyEmail - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun VerifyEmailLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        VerifyEmailScreen(
            email = "user@example.com",
            state = ChangeEmailUiState(emailBaru = "user@example.com"),
            onOtpChange = {},
            onVerifyClick = {},
            onResendClick = {},
            onBackClick = {},
        )
    }
}
