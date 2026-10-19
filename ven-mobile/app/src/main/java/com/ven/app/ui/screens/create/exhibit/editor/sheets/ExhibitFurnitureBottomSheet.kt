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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.components.ui.VexBottomSheet
import com.ven.app.ui.components.ui.VexButton
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.screens.create.exhibit.editor.ExhibitFurnitureType
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Bottom Sheet 3: Pengaturan Dekorasi & Objek Furnitur 3D.
 * Memungkinkan penempatan furnitur/objek pelengkap di dalam ruang pameran.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExhibitFurnitureBottomSheet(
    furnitureCounts: Map<ExhibitFurnitureType, Int>,
    onIncrement: (ExhibitFurnitureType) -> Unit,
    onDecrement: (ExhibitFurnitureType) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

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
                    text = stringResource(R.string.create_exhibit_sheet_furniture_title),
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

            // Daftar Objek Dekorasi
            Column(
                verticalArrangement = Arrangement.spacedBy(VexSpace.s3),
            ) {
                ExhibitFurnitureType.entries.forEach { type ->
                    val count = furnitureCounts[type] ?: 0

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(VexRadius.md))
                            .background(colors.surfaceInput)
                            .border(width = 1.dp, color = colors.border, shape = RoundedCornerShape(VexRadius.md))
                            .padding(horizontal = VexSpace.s4, vertical = VexSpace.s3),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(VexSpace.s3),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(colors.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                VexIcon(
                                    id = VexIcons.EditorFurniture,
                                    contentDescription = type.displayName,
                                    size = 20.dp,
                                    tint = colors.primaryText,
                                )
                            }

                            Text(
                                text = type.displayName,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = colors.text,
                            )
                        }

                        // Stepper (- count +)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(VexSpace.s3),
                        ) {
                            // Tombol Minus
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (count > 0) colors.surface else colors.surfaceInput)
                                    .border(width = 1.dp, color = colors.border, shape = CircleShape)
                                    .clickable(
                                        enabled = count > 0,
                                        role = Role.Button,
                                        onClick = { onDecrement(type) },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "−",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (count > 0) colors.text else colors.textMuted,
                                )
                            }

                            // Angka Jumlah
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = colors.text,
                            )

                            // Tombol Plus
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(colors.surface)
                                    .border(width = 1.dp, color = colors.border, shape = CircleShape)
                                    .clickable(
                                        role = Role.Button,
                                        onClick = { onIncrement(type) },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "+",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = colors.text,
                                )
                            }
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

@androidx.compose.ui.tooling.preview.Preview(name = "Furniture Sheet - Dark", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun ExhibitFurnitureSheetDarkPreview() {
    com.ven.app.ui.theme.VexTheme(mode = com.ven.app.ui.theme.ThemeMode.Dark) {
        ExhibitFurnitureBottomSheet(
            furnitureCounts = mapOf(
                ExhibitFurnitureType.Spotlight to 2,
                ExhibitFurnitureType.LoungeChair to 1,
            ),
            onIncrement = {},
            onDecrement = {},
            onDismiss = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Furniture Sheet - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ExhibitFurnitureSheetLightPreview() {
    com.ven.app.ui.theme.VexTheme(mode = com.ven.app.ui.theme.ThemeMode.Light) {
        ExhibitFurnitureBottomSheet(
            furnitureCounts = mapOf(
                ExhibitFurnitureType.Spotlight to 2,
                ExhibitFurnitureType.LoungeChair to 1,
            ),
            onIncrement = {},
            onDecrement = {},
            onDismiss = {},
        )
    }
}
