package com.ven.app.ui.screens.create.exhibit.editor.sheets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.components.ui.VexBottomSheet
import com.ven.app.ui.components.ui.VexButton
import com.ven.app.ui.components.ui.VexButtonVariant
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.screens.create.exhibit.editor.ExhibitArtwork
import com.ven.app.ui.screens.create.exhibit.editor.ExhibitPanelSlot
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Bottom Sheet 2: Pengaturan Penempatan Karya pada Panel Pameran.
 * Memungkinkan pengguna memilih slot panel ruangan (mis. Panel 1..9) dan menugaskan karya postingannya.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExhibitPanelsBottomSheet(
    panelSlots: List<ExhibitPanelSlot>,
    selectedSlotIndex: Int,
    availableArtworks: List<ExhibitArtwork>,
    onSelectSlot: (Int) -> Unit,
    onAssignArtwork: (ExhibitArtwork) -> Unit,
    onRemoveArtwork: (Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors
    val currentSlot = panelSlots.getOrNull(selectedSlotIndex)

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
                    text = stringResource(R.string.create_exhibit_sheet_panels_title),
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

            // Carousel Pemilihan Slot Panel
            Text(
                text = stringResource(R.string.create_exhibit_panel_slot_title, selectedSlotIndex + 1),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = colors.primaryText,
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(VexSpace.s3),
                contentPadding = PaddingValues(vertical = VexSpace.s1),
            ) {
                items(panelSlots, key = { it.slotIndex }) { slot ->
                    val isSelected = slot.slotIndex == selectedSlotIndex
                    val hasArtwork = slot.assignedArtwork != null

                    Box(
                        modifier = Modifier
                            .width(88.dp)
                            .height(64.dp)
                            .clip(RoundedCornerShape(VexRadius.md))
                            .background(
                                if (isSelected) colors.primary.copy(alpha = 0.15f) else colors.surfaceInput
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) colors.primary else colors.border,
                                shape = RoundedCornerShape(VexRadius.md),
                            )
                            .clickable(
                                role = Role.Button,
                                onClick = { onSelectSlot(slot.slotIndex) },
                            )
                            .padding(VexSpace.s2),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = "Slot ${slot.slotIndex + 1}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                ),
                                color = if (isSelected) colors.primaryText else colors.text,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (hasArtwork) "Filled ✓" else "Empty",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (hasArtwork) colors.primaryText else colors.textMuted,
                            )
                        }
                    }
                }
            }

            // Info Slot Saat Ini
            if (currentSlot?.assignedArtwork != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(VexRadius.md))
                        .background(colors.surfaceInput)
                        .padding(VexSpace.s3),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(VexSpace.s3),
                    ) {
                        ArtworkThumbnail(
                            artwork = currentSlot.assignedArtwork,
                            modifier = Modifier.size(48.dp),
                        )

                        Column {
                            Text(
                                text = currentSlot.assignedArtwork.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                                color = colors.text,
                            )
                            Text(
                                text = currentSlot.assignedArtwork.type.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted,
                            )
                        }
                    }

                    VexButton(
                        text = "Remove",
                        onClick = { onRemoveArtwork(selectedSlotIndex) },
                        variant = VexButtonVariant.Outline,
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.create_exhibit_panel_select_prompt),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                )
            }

            // Daftar Postingan / Karya yang Tersedia
            Text(
                text = "Your Works / Posts",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = colors.text,
                modifier = Modifier.padding(top = VexSpace.s2),
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(VexSpace.s3),
            ) {
                availableArtworks.forEach { artwork ->
                    val isAssignedToCurrent = currentSlot?.assignedArtwork?.id == artwork.id

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(VexRadius.md))
                            .background(colors.surfaceInput)
                            .clickable(
                                role = Role.Button,
                                onClick = { onAssignArtwork(artwork) },
                            )
                            .padding(VexSpace.s3),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(VexSpace.s3),
                        ) {
                            ArtworkThumbnail(
                                artwork = artwork,
                                modifier = Modifier
                                    .size(54.dp)
                                    .aspectRatio(1f),
                            )

                            Column {
                                Text(
                                    text = artwork.title,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                                    color = colors.text,
                                )
                                Text(
                                    text = artwork.type.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted,
                                )
                            }
                        }

                        VexButton(
                            text = if (isAssignedToCurrent) "Assigned" else "Assign",
                            onClick = { onAssignArtwork(artwork) },
                            variant = if (isAssignedToCurrent) VexButtonVariant.Outline else VexButtonVariant.Primary,
                        )
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

@Composable
private fun ArtworkThumbnail(
    artwork: ExhibitArtwork,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(VexRadius.xs))
            .background(colors.primary.copy(alpha = 0.2f))
            .border(width = 1.dp, color = colors.border, shape = RoundedCornerShape(VexRadius.xs)),
        contentAlignment = Alignment.Center,
    ) {
        if (artwork.imageRes != null) {
            Image(
                painter = painterResource(id = artwork.imageRes),
                contentDescription = artwork.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            VexIcon(
                id = VexIcons.EditorPanel,
                contentDescription = null,
                size = 20.dp,
                tint = colors.primaryText,
            )
        }
    }
}

// -------------------------------------------------------------
// Previews
// -------------------------------------------------------------

@androidx.compose.ui.tooling.preview.Preview(name = "Panels Sheet - Dark", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun ExhibitPanelsSheetDarkPreview() {
    com.ven.app.ui.theme.VexTheme(mode = com.ven.app.ui.theme.ThemeMode.Dark) {
        ExhibitPanelsBottomSheet(
            panelSlots = listOf(
                ExhibitPanelSlot(slotIndex = 0, assignedArtwork = com.ven.app.ui.screens.create.exhibit.editor.sampleUserArtworks[0]),
                ExhibitPanelSlot(slotIndex = 1),
                ExhibitPanelSlot(slotIndex = 2),
            ),
            selectedSlotIndex = 0,
            availableArtworks = com.ven.app.ui.screens.create.exhibit.editor.sampleUserArtworks,
            onSelectSlot = {},
            onAssignArtwork = {},
            onRemoveArtwork = {},
            onDismiss = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Panels Sheet - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ExhibitPanelsSheetLightPreview() {
    com.ven.app.ui.theme.VexTheme(mode = com.ven.app.ui.theme.ThemeMode.Light) {
        ExhibitPanelsBottomSheet(
            panelSlots = listOf(
                ExhibitPanelSlot(slotIndex = 0, assignedArtwork = com.ven.app.ui.screens.create.exhibit.editor.sampleUserArtworks[0]),
                ExhibitPanelSlot(slotIndex = 1),
                ExhibitPanelSlot(slotIndex = 2),
            ),
            selectedSlotIndex = 0,
            availableArtworks = com.ven.app.ui.screens.create.exhibit.editor.sampleUserArtworks,
            onSelectSlot = {},
            onAssignArtwork = {},
            onRemoveArtwork = {},
            onDismiss = {},
        )
    }
}
