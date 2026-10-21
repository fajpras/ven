package com.ven.app.ui.screens.create.exhibit.editor.sheets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.components.ui.VexBottomSheet
import com.ven.app.ui.components.ui.VexButton
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

            // Info Slot Saat Ini jika sudah terpasang karya
            if (currentSlot?.assignedArtwork != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(VexRadius.md))
                        .background(colors.primary.copy(alpha = 0.10f))
                        .border(
                            width = 1.dp,
                            color = colors.primary.copy(alpha = 0.40f),
                            shape = RoundedCornerShape(VexRadius.md),
                        )
                        .padding(horizontal = VexSpace.s4, vertical = VexSpace.s3),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(VexSpace.s3),
                    ) {
                        ArtworkThumbnail(
                            artwork = currentSlot.assignedArtwork,
                            modifier = Modifier.size(44.dp),
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentSlot.assignedArtwork.title,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = colors.text,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Placed on Slot ${selectedSlotIndex + 1}",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.primaryText,
                                maxLines = 1,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(VexSpace.s2))

                    // Tombol Remove ringkas
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(RoundedCornerShape(VexRadius.full))
                            .background(colors.danger.copy(alpha = 0.15f))
                            .border(1.dp, colors.danger.copy(alpha = 0.4f), RoundedCornerShape(VexRadius.full))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = colors.danger.copy(alpha = 0.2f)),
                                role = Role.Button,
                                onClick = { onRemoveArtwork(selectedSlotIndex) },
                            )
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "Remove",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.danger,
                        )
                    }
                }
            }

            // Header Daftar Karya / Postingan
            Column(
                modifier = Modifier.padding(top = VexSpace.s2),
            ) {
                Text(
                    text = "Your Works / Posts",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.text,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.create_exhibit_panel_select_prompt),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                )
            }

            // Daftar Postingan / Karya yang Tersedia
            Column(
                verticalArrangement = Arrangement.spacedBy(VexSpace.s3),
            ) {
                availableArtworks.forEach { artwork ->
                    val isAssignedToCurrent = currentSlot?.assignedArtwork?.id == artwork.id
                    val assignedSlot = panelSlots.firstOrNull { it.assignedArtwork?.id == artwork.id }
                    val isAssignedToOther = assignedSlot != null && !isAssignedToCurrent

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(VexRadius.md))
                            .background(
                                if (isAssignedToCurrent) colors.primary.copy(alpha = 0.10f) else colors.surfaceInput
                            )
                            .border(
                                width = if (isAssignedToCurrent) 1.5.dp else 1.dp,
                                color = if (isAssignedToCurrent) colors.primary else colors.border,
                                shape = RoundedCornerShape(VexRadius.md),
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = colors.text.copy(alpha = 0.1f)),
                                role = Role.Button,
                                onClick = {
                                    if (isAssignedToCurrent) {
                                        onRemoveArtwork(selectedSlotIndex)
                                    } else {
                                        onAssignArtwork(artwork)
                                    }
                                },
                            )
                            .padding(horizontal = VexSpace.s4, vertical = VexSpace.s3),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Sisi Kiri: Thumbnail & Metadata Karya
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(VexSpace.s3),
                        ) {
                            ArtworkThumbnail(
                                artwork = artwork,
                                modifier = Modifier.size(50.dp),
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = artwork.title,
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = colors.text,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Badge Tipe Karya (Poster / Video)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(VexRadius.xs))
                                        .background(colors.surface.copy(alpha = 0.7f))
                                        .border(0.5.dp, colors.border, RoundedCornerShape(VexRadius.xs))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = artwork.type.name,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Medium,
                                        ),
                                        color = colors.textMuted,
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(VexSpace.s3))

                        // Tombol Aksi Kompak "Assign" / "Assigned ✓" / "Slot N"
                        Box(
                            modifier = Modifier
                                .height(34.dp)
                                .clip(RoundedCornerShape(VexRadius.full))
                                .background(
                                    when {
                                        isAssignedToCurrent -> colors.primary.copy(alpha = 0.20f)
                                        isAssignedToOther -> colors.surface
                                        else -> colors.primary
                                    }
                                )
                                .border(
                                    width = if (isAssignedToCurrent || isAssignedToOther) 1.dp else 0.dp,
                                    color = when {
                                        isAssignedToCurrent -> colors.primary
                                        isAssignedToOther -> colors.border
                                        else -> Color.Transparent
                                    },
                                    shape = RoundedCornerShape(VexRadius.full),
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = Color.White.copy(alpha = 0.2f)),
                                    role = Role.Button,
                                    onClick = {
                                        if (isAssignedToCurrent) {
                                            onRemoveArtwork(selectedSlotIndex)
                                        } else {
                                            onAssignArtwork(artwork)
                                        }
                                    },
                                )
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                if (isAssignedToCurrent) {
                                    VexIcon(
                                        id = VexIcons.Check,
                                        contentDescription = null,
                                        size = 14.dp,
                                        tint = colors.primaryText,
                                    )
                                }

                                Text(
                                    text = when {
                                        isAssignedToCurrent -> "Assigned"
                                        isAssignedToOther -> "Slot ${assignedSlot.slotIndex + 1}"
                                        else -> "Assign"
                                    },
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = when {
                                        isAssignedToCurrent -> colors.primaryText
                                        isAssignedToOther -> colors.textMuted
                                        else -> Color.White
                                    },
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

@Composable
private fun ArtworkThumbnail(
    artwork: ExhibitArtwork,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(VexRadius.md))
            .background(colors.primary.copy(alpha = 0.15f))
            .border(width = 1.dp, color = colors.border, shape = RoundedCornerShape(VexRadius.md)),
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
