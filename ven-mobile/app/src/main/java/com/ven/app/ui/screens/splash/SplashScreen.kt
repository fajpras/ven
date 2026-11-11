package com.ven.app.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ven.app.R
import com.ven.app.data.api.models.UserModel
import com.ven.app.ui.components.ui.VexButton
import com.ven.app.ui.components.ui.VexButtonVariant
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Layar Selamat Datang / Splash Screen aplikasi VEN (Virtual Exhibition).
 * Menampilkan identitas visual, penjelasan singkat aplikasi, serta pilihan login/register/next.
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
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            colors.bg,
            Color(0xFF2A153A),
            colors.bg,
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -40 }),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Spacer(modifier = Modifier.height(VexSpace.s8))

                    // Brand Logo Icon
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFBA18F5), Color(0xFF6B11F4))
                                )
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_ven),
                            contentDescription = "VEN Logo",
                            modifier = Modifier.size(54.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(VexSpace.s4))

                    Text(
                        text = "Virtual Exhibition",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                        ),
                        color = colors.text,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        text = "Ruang Pameran Seni & Kreatif 3D Interaktif",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFFBA18F5),
                            fontWeight = FontWeight.SemiBold,
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp),
                    )

                    Spacer(modifier = Modifier.height(VexSpace.s6))

                    // Penjelasan Aplikasi
                    Card(
                        shape = RoundedCornerShape(VexRadius.md),
                        colors = CardDefaults.cardColors(
                            containerColor = colors.surface.copy(alpha = 0.85f),
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = colors.border.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(VexRadius.md),
                            ),
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Selamat Datang di VEN!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                                color = colors.text,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Platform pameran virtual imersif untuk memamerkan dan menikmati karya seni digital, desain 3D, fotografi, dan portofolio kreatif secara langsung di dalam ruang 3D interaktif.",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted,
                                lineHeight = 19.sp,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(VexSpace.s4))

                    // Highlight Fitur Singkat
                    FeatureItem(
                        iconEmoji = "🏛️",
                        title = "Pameran 3D Interaktif",
                        desc = "Jelajahi galeri seni virtual dengan sudut pandang bebas 3D.",
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FeatureItem(
                        iconEmoji = "🎨",
                        title = "Kustomisasi Hall Pameran",
                        desc = "Rancang tata letak, warna dinding & lantai, serta tempatkan karya Anda.",
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FeatureItem(
                        iconEmoji = "🤝",
                        title = "Terhubung dengan Kreator",
                        desc = "Apresiasi karya dan ajukan jadwal diskusi kolaborasi seni secara langsung.",
                    )
                }
            }

            Spacer(modifier = Modifier.height(VexSpace.s8))

            // Action Buttons Section
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 40 }),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (isLoggedIn && user != null) {
                        // Sesi pengguna sedang aktif
                        Card(
                            shape = RoundedCornerShape(VexRadius.md),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF1E1728),
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFBA18F5).copy(alpha = 0.5f), RoundedCornerShape(VexRadius.md))
                                .padding(bottom = 12.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFBA18F5)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = (user.nama ?: "U").take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Terhubung sebagai:",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = colors.textMuted,
                                    )
                                    Text(
                                        text = user.nama ?: "Pengguna",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                        ),
                                        color = colors.text,
                                    )
                                }
                                Text(
                                    text = (user.role ?: "kreator").replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = Color(0xFFBA18F5),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFBA18F5).copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                )
                            }
                        }

                        // Tombol Next (Lanjut ke Home)
                        VexButton(
                            text = "Lanjut ke Beranda (Next)",
                            onClick = onNavigateToHome,
                            variant = VexButtonVariant.Primary,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            VexButton(
                                text = "Ganti Akun",
                                onClick = onNavigateToLogin,
                                variant = VexButtonVariant.Outline,
                                modifier = Modifier.weight(1f),
                            )
                            VexButton(
                                text = "Daftar Baru",
                                onClick = onNavigateToRegister,
                                variant = VexButtonVariant.Outline,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    } else {
                        // Belum login
                        VexButton(
                            text = "Masuk (Login)",
                            onClick = onNavigateToLogin,
                            variant = VexButtonVariant.Primary,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        VexButton(
                            text = "Daftar Akun Baru (Register)",
                            onClick = onNavigateToRegister,
                            variant = VexButtonVariant.Outline,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        VexButton(
                            text = "Mulai Jelajah (Next)",
                            onClick = onNavigateToExplore,
                            variant = VexButtonVariant.TextLink,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureItem(
    iconEmoji: String,
    title: String,
    desc: String,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(VexRadius.md))
            .background(colors.surface.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = iconEmoji,
            fontSize = 20.sp,
            modifier = Modifier.padding(end = 12.dp),
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.text,
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
            )
        }
    }
}
