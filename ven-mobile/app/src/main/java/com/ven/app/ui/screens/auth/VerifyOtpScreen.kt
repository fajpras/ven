package com.ven.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.components.layout.VexTopBar
import com.ven.app.ui.components.ui.OtpInput
import com.ven.app.ui.components.ui.VexButton
import com.ven.app.ui.components.ui.VexButtonVariant
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Layar Verifikasi Kode OTP (Halaman 2).
 *
 * Sesuai spesifikasi DESIGN.md §8.9 & §8.11:
 * - Pola SubPage: [VexTopBar.Back] dengan judul "Verify OTP".
 * - Input: [OtpInput] 6 digit (selaras dengan API backend).
 * - Fitur Kirim Ulang (Resend) dengan cooldown hitung mundur 60 detik.
 * - Otomatis memicu verifikasi saat 6 digit terisi penuh.
 */
@Composable
fun VerifyOtpScreen(
    state: ForgotPasswordUiState,
    onOtpChange: (String) -> Unit,
    onVerifyClick: () -> Unit,
    onResendClick: () -> Unit,
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
            title = stringResource(R.string.verify_otp_title),
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
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Info tujuan pengiriman OTP
                Text(
                    text = stringResource(R.string.verify_otp_subtitle, state.email),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = VexSpace.s6),
                )

                // Label OTP
                Text(
                    text = stringResource(R.string.verify_otp_label),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = colors.text,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = VexSpace.s3),
                )

                // Komponen 6 Kotak OTP
                OtpInput(
                    value = state.otp,
                    onValueChange = onOtpChange,
                    digitCount = 6,
                    isError = state.errorMessage != null,
                    onOtpComplete = { onVerifyClick() },
                )

                // Pesan Error jika ada
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

                Spacer(modifier = Modifier.height(VexSpace.s6))

                // Baris Resend OTP & Cooldown
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

                    if (state.resendCooldown > 0) {
                        Text(
                            text = " " + stringResource(R.string.verify_otp_resend_cooldown, state.resendCooldown),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = colors.textMuted,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    } else {
                        VexButton(
                            text = stringResource(R.string.verify_otp_resend),
                            onClick = onResendClick,
                            variant = VexButtonVariant.TextLink,
                            enabled = !state.isLoading,
                        )
                    }
                }
            }

            // Tombol CTA Verify OTP di bagian bawah
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(bottom = VexSpace.s6),
            ) {
                VexButton(
                    text = stringResource(R.string.verify_otp_button),
                    onClick = onVerifyClick,
                    variant = VexButtonVariant.Primary,
                    loading = state.isLoading,
                    enabled = state.isOtpComplete,
                )
            }
        }
    }
}

@Preview(name = "VerifyOtp - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun VerifyOtpDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        VerifyOtpScreen(
            state = ForgotPasswordUiState(email = "user@example.com", otp = "123", resendCooldown = 45),
            onOtpChange = {},
            onVerifyClick = {},
            onResendClick = {},
            onBackClick = {},
        )
    }
}

@Preview(name = "VerifyOtp - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun VerifyOtpLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        VerifyOtpScreen(
            state = ForgotPasswordUiState(email = "user@example.com", otp = ""),
            onOtpChange = {},
            onVerifyClick = {},
            onResendClick = {},
            onBackClick = {},
        )
    }
}
