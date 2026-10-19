package com.ven.app.ui.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

enum class CreateContentType {
    Post,
    Exhibit,
}

/**
 * SegmentedPill melayang untuk beralih antara alur Post dan Exhibit.
 * Sesuai spesifikasi DESIGN.md §6.1 & §8.13:
 * - Lebar ≈ 240-250dp, tinggi ≈ 50-54dp.
 * - Pill melayang, fill surface/bg + border subtle.
 * - Terpilih: teks tebal warna text, Tidak terpilih: textMuted.
 */
@Composable
fun SegmentedPill(
    selectedType: CreateContentType,
    onTypeSelected: (CreateContentType) -> Unit,
    modifier: Modifier = Modifier,
    postLabel: String = "Post",
    exhibitLabel: String = "Exhibit",
) {
    val colors = VexTheme.colors
    val shape = RoundedCornerShape(VexRadius.full)

    Box(
        modifier = modifier
            .width(245.dp)
            .height(52.dp)
            .shadow(elevation = 6.dp, shape = shape, spotColor = colors.scrim.copy(alpha = 0.15f))
            .clip(shape)
            .background(colors.surface)
            .border(width = 1.dp, color = colors.border, shape = shape)
            .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Tab Post
            val isPostSelected = selectedType == CreateContentType.Post
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(shape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = colors.text.copy(alpha = 0.12f)),
                        role = Role.Tab,
                        onClick = { onTypeSelected(CreateContentType.Post) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = postLabel,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (isPostSelected) FontWeight.Bold else FontWeight.Medium,
                    ),
                    color = if (isPostSelected) colors.text else colors.textMuted,
                    textAlign = TextAlign.Center,
                )
            }

            // Tab Exhibit
            val isExhibitSelected = selectedType == CreateContentType.Exhibit
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(shape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = colors.text.copy(alpha = 0.12f)),
                        role = Role.Tab,
                        onClick = { onTypeSelected(CreateContentType.Exhibit) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = exhibitLabel,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (isExhibitSelected) FontWeight.Bold else FontWeight.Medium,
                    ),
                    color = if (isExhibitSelected) colors.text else colors.textMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview(name = "SegmentedPill - Dark", backgroundColor = 0xFF1F1F1F, showBackground = true)
@Composable
private fun SegmentedPillDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        Box(modifier = Modifier.padding(16.dp)) {
            SegmentedPill(
                selectedType = CreateContentType.Post,
                onTypeSelected = {},
            )
        }
    }
}

@Preview(name = "SegmentedPill - Light", backgroundColor = 0xFFFFFFFF, showBackground = true)
@Composable
private fun SegmentedPillLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        Box(modifier = Modifier.padding(16.dp)) {
            SegmentedPill(
                selectedType = CreateContentType.Post,
                onTypeSelected = {},
            )
        }
    }
}
