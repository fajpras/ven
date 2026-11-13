package com.ven.app.ui.screens.unity

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme
import com.ven.app.util.UnityLauncher
import kotlinx.coroutines.delay

@Composable
fun UnityLoadingScreen(
    targetId: String,
    title: String = "",
    description: String = "",
    bannerUrl: String = "",
    tipe: String = "kecil",
    isEditor: Boolean = true,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val colors = VexTheme.colors

    var hasLaunched by rememberSaveable { mutableStateOf(false) }

    // Launcher aktivitas Unity: ketika Unity selesai (activity.finish), callback dipanggil dan kembali otomatis
    val unityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) {
        onBackClick()
    }

    LaunchedEffect(targetId) {
        if (!hasLaunched) {
            hasLaunched = true
            // Berikan jeda sejenak (400ms) agar loading UI ter-render mulus sebelum transisi berat ke engine Unity
            delay(400)
            val intent = if (isEditor) {
                UnityLauncher.createEditorIntent(
                    context = context,
                    templateId = targetId,
                    title = title,
                    description = description,
                    bannerUrl = bannerUrl,
                    tipe = tipe,
                )
            } else {
                UnityLauncher.createViewerIntent(context, targetId)
            }
            unityLauncher.launch(intent)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "UnityLoadingPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "PulseScale",
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "GlowAlpha",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .padding(start = VexSpace.s4, top = VexSpace.s4)
                .align(Alignment.TopStart),
        ) {
            VexIcon(
                id = VexIcons.Close,
                contentDescription = "Tutup",
                tint = colors.text,
                size = 22.dp,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = VexSpace.s8),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(130.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    colors.primary.copy(alpha = glowAlpha),
                                    Color.Transparent,
                                ),
                            ),
                        ),
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(colors.surface)
                        .border(1.5.dp, colors.primary.copy(alpha = 0.45f), CircleShape),
                ) {
                    VexIcon(
                        id = if (isEditor) VexIcons.Tab.Exhibits.filled else VexIcons.Play,
                        contentDescription = null,
                        tint = colors.primary,
                        size = 36.dp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(VexSpace.s6))

            Text(
                text = if (isEditor) "Membuka Ruang 3D Unity" else "Memuat Pameran 3D",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                ),
                color = colors.text,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(VexSpace.s2))

            Text(
                text = if (isEditor) {
                    "Menyiapkan template ruangan, pencahayaan, dan kanvas karya seni..."
                } else {
                    "Menghubungkan ke ruang pameran virtual dan mengunduh data karya..."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(VexSpace.s6))

            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = colors.primary,
                trackColor = colors.border.copy(alpha = 0.35f),
                strokeCap = StrokeCap.Round,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = VexSpace.s6, vertical = VexSpace.s6),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(VexRadius.md))
                    .background(colors.surface.copy(alpha = 0.85f))
                    .border(1.dp, colors.border.copy(alpha = 0.4f), RoundedCornerShape(VexRadius.md))
                    .padding(horizontal = VexSpace.s4, vertical = VexSpace.s3),
            ) {
                Text(
                    text = if (isEditor) {
                        "Tips: Di dalam Unity Editor, Anda dapat menggeser sudut pandang, menempatkan karya seni di dinding pameran, dan langsung mempublikasikannya."
                    } else {
                        "Tips: Gunakan joystick virtual untuk berkeliling di dalam ruang pameran dan mendekati karya seni untuk melihat detailnya."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
