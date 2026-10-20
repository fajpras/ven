package com.ven.app.ui.screens.create.exhibit.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ven.app.R
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.screens.create.exhibit.editor.exhibitWallPalette
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Panel Bawah Pemilihan Warna Dinding 3D (Wireframe Create Exhibition - Frame 1).
 *
 * Menampilkan:
 * 1. Label "Color"
 * 2. 12 Lingkaran Warna terbagi dalam 2 baris x 6 kolom yang presisi sesuai wireframe.
 */
@Composable
fun ExhibitColorBottomPanel(
    selectedColor: Color,
    onSelectColor: (Color) -> Unit,
    modifier: Modifier = Modifier,
    palette: List<Color> = exhibitWallPalette,
) {
    val colors = VexTheme.colors
    val panelShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val row1 = palette.take(6)
    val row2 = palette.drop(6).take(6)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 16.dp, shape = panelShape)
            .clip(panelShape)
            .background(colors.surface)
            .border(width = 1.dp, color = colors.border.copy(alpha = 0.5f), shape = panelShape)
            .navigationBarsPadding()
            .padding(horizontal = VexSpace.s6, vertical = VexSpace.s5),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Label "Color"
            Text(
                text = stringResource(R.string.create_exhibit_color_label),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp,
                ),
                color = colors.text,
            )

            // Baris 1: 6 Lingkaran Warna
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row1.forEach { color ->
                    ColorCircleSwatch(
                        color = color,
                        isSelected = color == selectedColor,
                        onClick = { onSelectColor(color) },
                    )
                }
            }

            // Baris 2: 6 Lingkaran Warna
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row2.forEach { color ->
                    ColorCircleSwatch(
                        color = color,
                        isSelected = color == selectedColor,
                        onClick = { onSelectColor(color) },
                    )
                }
            }
        }
    }
}

/**
 * Lingkaran swatch warna individu dengan indikator seleksi aktif.
 */
@Composable
private fun ColorCircleSwatch(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isLightColor = (color.red * 0.299f + color.green * 0.587f + color.blue * 0.114f) > 0.82f

    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .then(
                if (isSelected) {
                    Modifier
                        .border(width = 2.5.dp, color = VexTheme.colors.primary, shape = CircleShape)
                        .padding(3.dp)
                } else {
                    Modifier
                }
            )
            .clip(CircleShape)
            .background(color)
            .then(
                if (isLightColor) {
                    Modifier.border(width = 1.dp, color = Color(0xFFD0D0D4), shape = CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = Color.White.copy(alpha = 0.3f)),
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            VexIcon(
                id = VexIcons.Check,
                contentDescription = null,
                size = 14.dp,
                tint = if (isLightColor) Color.Black else Color.White,
            )
        }
    }
}

// -------------------------------------------------------------
// Previews
// -------------------------------------------------------------

@Preview(name = "Color Bottom Panel - Light")
@Composable
private fun ExhibitColorBottomPanelLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        ExhibitColorBottomPanel(
            selectedColor = exhibitWallPalette[5], // Purple
            onSelectColor = {},
        )
    }
}

@Preview(name = "Color Bottom Panel - Dark")
@Composable
private fun ExhibitColorBottomPanelDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        ExhibitColorBottomPanel(
            selectedColor = exhibitWallPalette[5], // Purple
            onSelectColor = {},
        )
    }
}
