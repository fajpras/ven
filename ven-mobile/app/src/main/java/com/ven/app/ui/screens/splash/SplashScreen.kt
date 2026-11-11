package com.ven.app.ui.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ven.app.R
import com.ven.app.data.api.models.UserModel
import com.ven.app.ui.components.layout.AuthLayout
import com.ven.app.ui.components.ui.VexButton
import com.ven.app.ui.components.ui.VexButtonVariant
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Layar Selamat Datang / Splash Screen aplikasi VEN (Virtual Exhibition).
 * Menggunakan AuthLayout agar seragam, bersih, dan konsisten dengan halaman Login/Register.
 */
@Composable
fun SplashScreen(
    user: UserModel?,
    isLoggedIn: Boolean,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToExplore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    AuthLayout(
        title = "Virtual Exhibition",
        modifier = modifier,
        formContent = {
            // Logo VEN
            Image(
                painter = painterResource(id = R.drawable.logo_ven),
                contentDescription = "VEN Logo",
                modifier = Modifier
                    .size(width = 120.dp, height = 48.dp),
            )

            Spacer(modifier = Modifier.height(VexSpace.s6))

            // Penjelasan singkat dan elegan
            Text(
                text = "Platform pameran virtual interaktif berbasis 3D untuk memamerkan, mengkurasi, dan menjelajahi karya seni digital.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 22.sp,
                ),
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(VexSpace.s12))

            // Tombol Login
            VexButton(
                text = "Masuk",
                onClick = onNavigateToLogin,
                variant = VexButtonVariant.Primary,
            )

            Spacer(modifier = Modifier.height(VexSpace.s4))

            // Tombol Register
            VexButton(
                text = "Daftar",
                onClick = onNavigateToRegister,
                variant = VexButtonVariant.Outline,
            )

            Spacer(modifier = Modifier.height(VexSpace.s4))

            // Tombol Next / Lewati
            if (isLoggedIn && user != null) {
                VexButton(
                    text = "Lanjut sebagai ${user.nama ?: "Pengguna"} (Next)",
                    onClick = onNavigateToHome,
                    variant = VexButtonVariant.TextLink,
                )
            } else {
                VexButton(
                    text = "Lanjut ke Beranda (Next)",
                    onClick = onNavigateToExplore,
                    variant = VexButtonVariant.TextLink,
                )
            }
        },
        footerContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "VEN • Virtual Exhibition Platform",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textMuted,
                )
            }
        },
    )
}
