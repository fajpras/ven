package com.ven.app.ui.components.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Event Banner / Poster Pameran untuk carousel "Coming Soon".
 *
 * Sesuai spesifikasi DESIGN.md §6.3 & §8.6:
 * - Kartu banner horizontal rasio ~16:9.
 * - Width: ~240dp.
 * - Sudut membulat: [VexRadius.md] (8–12dp).
 * - Menampilkan karya/poster promosi kompetisi & expo yang akan datang.
 */
@Composable
fun EventBanner(
    title: String,
    dateText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    backgroundColor: Color = VexTheme.colors.surfaceInput,
) {
    val colors = VexTheme.colors

    Box(
        modifier = modifier
            .width(240.dp)
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(VexRadius.md))
            .background(backgroundColor)
            .clickable(onClick = onClick),
    ) {
        // Overlay gradient hitam transparan di bagian bawah agar teks judul & tanggal kontras terbaca
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.75f),
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY,
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(VexSpace.s3),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Bottom,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = dateText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

@Preview(name = "EventBanner - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun EventBannerDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        EventBanner(
            title = "Open Source Competition",
            dateText = "24th Sep 2026",
            onClick = {},
        )
    }
}

@Preview(name = "EventBanner - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun EventBannerLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        EventBanner(
            title = "PBL EXPO 2026",
            dateText = "1st Oct 2026",
            onClick = {},
        )
    }
}
