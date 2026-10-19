package com.ven.app.ui.screens.create.exhibit.editor.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.components.ui.VexBottomSheet
import com.ven.app.ui.components.ui.VexButton
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.screens.create.exhibit.editor.WallColorOption
import com.ven.app.ui.screens.create.exhibit.editor.defaultWallColorOptions
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Bottom Sheet 4: Pemilihan Palet Warna Dinding Ruangan Pameran.
 * Mengubah warna dinding 3D secara langsung.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExhibitColorBottomSheet(
    selectedColor: Color,
    onSelectColor: (Color) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    colorOptions: List<WallColorOption> = defaultWallColorOptions,
) {
    val colors = VexTheme.colors
    val isDark = (colors.bg.red * 0.299f + colors.bg.green * 0.587f + colors.bg.blue * 0.114f) < 0.5f

    VexBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = VexSpace.s6)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = VexSpace.s6),
            verticalArrangement = Arrangement.spacedBy(VexSpace.s4),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.create_exhibit_sheet_color_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = colors.text,
                )

                IconButton(onClick = onDismiss) {
                    VexIcon(
                        id = VexIcons.Close,
                        contentDescription = stringResource(R.string.create_post_close_desc),
                        size = VexSize.icon,
                        tint = colors.textMuted,
                    )
                }
            }

            // Daftar Warna
            Column(
                verticalArrangement = Arrangement.spacedBy(VexSpace.s3),
            ) {
                colorOptions.forEach { option ->
                    val isSelected = option.color == selectedColor

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(VexRadius.md))
                            .background(if (isSelected) colors.primary.copy(alpha = 0.12f) else colors.surfaceInput)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) colors.primary else colors.border,
                                shape = RoundedCornerShape(VexRadius.md),
                            )
                            .clickable(
                                role = Role.Button,
                                onClick = { onSelectColor(option.color) },
                            )
                            .padding(horizontal = VexSpace.s4, vertical = VexSpace.s3),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(VexSpace.s3),
                        ) {
                            // Lingkaran Swatch Warna
                            val ringColor = if (isSelected) {
                                if (isDark) Color.White else Color.Black
                            } else {
                                colors.border
                            }

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(option.color)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = ringColor,
                                        shape = CircleShape,
                                    ),
                            )

                            Text(
                                text = option.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                ),
                                color = if (isSelected) colors.primaryText else colors.text,
                            )
                        }

                        if (isSelected) {
                            VexIcon(
                                id = VexIcons.Check,
                                contentDescription = "Selected",
                                size = VexSize.icon,
                                tint = colors.primaryText,
                            )
                        }
                    }
                }
            }

            // Tombol Selesai
            VexButton(
                text = "Done",
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = VexSpace.s2),
            )
        }
    }
}

// -------------------------------------------------------------
// Previews
// -------------------------------------------------------------

@androidx.compose.ui.tooling.preview.Preview(name = "Color Sheet - Dark", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun ExhibitColorSheetDarkPreview() {
    com.ven.app.ui.theme.VexTheme(mode = com.ven.app.ui.theme.ThemeMode.Dark) {
        ExhibitColorBottomSheet(
            selectedColor = Color(0xFF6C3BE8),
            onSelectColor = {},
            onDismiss = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Color Sheet - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ExhibitColorSheetLightPreview() {
    com.ven.app.ui.theme.VexTheme(mode = com.ven.app.ui.theme.ThemeMode.Light) {
        ExhibitColorBottomSheet(
            selectedColor = Color(0xFF6C3BE8),
            onSelectColor = {},
            onDismiss = {},
        )
    }
}
