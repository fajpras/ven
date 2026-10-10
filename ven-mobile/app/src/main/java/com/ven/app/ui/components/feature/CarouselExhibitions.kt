package com.ven.app.ui.components.feature

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Komponen Carousel Pameran (Carousel Exhibitions) yang reusable.
 *
 * Mendukung berbagai seksi pameran horizontal (seperti "Coming Soon", "Trending Exhibitions", dll).
 * Menggunakan [rememberSnapFlingBehavior] agar scroll berhenti (snap) per-kartu selayaknya carousel.
 *
 * Sesuai spesifikasi DESIGN.md §6.3 & §8.6:
 * - Judul seksi: `titleLarge` SemiBold, padding horizontal 24dp (VexSpace.s6), margin bawah 8dp (VexSpace.s2).
 * - Carousel: `LazyRow` horizontal dengan padding konten 24dp, jarak antar item 12dp (VexSpace.s3).
 * - Snap behavior: Menahan dan menyelaraskan posisi item saat di-fling.
 * - Divider pembatas seksi opsional (default true): tebal 1dp, warna `#313131`.
 */
@Composable
fun CarouselExhibitions(
    title: String,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    showDivider: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = VexSpace.s6),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(VexSpace.s3),
    content: LazyListScope.() -> Unit,
) {
    val colors = VexTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = VexSpace.s4),
    ) {
        // Judul Seksi Carousel
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = colors.text,
            modifier = Modifier.padding(
                horizontal = VexSpace.s6,
                vertical = VexSpace.s2,
            ),
        )

        Spacer(modifier = Modifier.height(VexSpace.s2))

        // Carousel Horizontal dengan Snap Fling Behavior
        LazyRow(
            state = state,
            contentPadding = contentPadding,
            horizontalArrangement = horizontalArrangement,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = state),
            content = content,
        )

        // Garis Pembatas Seksi (Opsional)
        if (showDivider) {
            Spacer(modifier = Modifier.height(VexSpace.s4))
            HorizontalDivider(
                thickness = 1.dp,
                color = Color(0xFF313131),
            )
        }
    }
}

// ---------------------------------------------------------
// Previews untuk Dark Mode & Light Mode
// ---------------------------------------------------------

@Preview(name = "CarouselExhibitions - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun CarouselExhibitionsDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        CarouselExhibitions(
            title = "Coming Soon",
        ) {
            item {
                EventBanner(
                    title = "Open Source Competition",
                    dateText = "24th Sep 2026",
                    onClick = {},
                )
            }
            item {
                EventBanner(
                    title = "PBL EXPO 2026",
                    dateText = "1st Oct 2026",
                    onClick = {},
                )
            }
        }
    }
}

@Preview(name = "CarouselExhibitions - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CarouselExhibitionsLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        CarouselExhibitions(
            title = "Coming Soon",
        ) {
            item {
                EventBanner(
                    title = "Open Source Competition",
                    dateText = "24th Sep 2026",
                    onClick = {},
                )
            }
            item {
                EventBanner(
                    title = "PBL EXPO 2026",
                    dateText = "1st Oct 2026",
                    onClick = {},
                )
            }
        }
    }
}
