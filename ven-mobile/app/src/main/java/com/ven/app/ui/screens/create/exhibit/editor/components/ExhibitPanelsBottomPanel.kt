package com.ven.app.ui.screens.create.exhibit.editor.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ven.app.R
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.screens.create.exhibit.editor.ArtworkType
import com.ven.app.ui.screens.create.exhibit.editor.ExhibitArtwork
import com.ven.app.ui.screens.create.exhibit.editor.ExhibitPanelSlot
import com.ven.app.ui.screens.create.exhibit.editor.sampleUserArtworks
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Panel Bawah Penempatan Karya ke Panel 3D (Wireframe Create Exhibition - Frame 3).
 *
 * Menampilkan:
 * 1. Tab Navigasi: "Poster" dan "Video" dengan indikator garis bawah aktif.
 * 2. Baris 3 Kartu Karya berbentuk rounded square yang dapat langsung diklik untuk assign / unassign.
 * 3. Indikator Paginasi Titik (• • •).
 */
@Composable
fun ExhibitPanelsBottomPanel(
    panelSlots: List<ExhibitPanelSlot>,
    availableArtworks: List<ExhibitArtwork>,
    onToggleArtwork: (ExhibitArtwork) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors
    val panelShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    var selectedTab by remember { mutableStateOf(ArtworkType.Poster) }

    val filteredArtworks = remember(selectedTab, availableArtworks) {
        availableArtworks.filter { it.type == selectedTab }
    }

    // Ambil hingga 3 karya untuk halaman pertama
    val displayArtworks = filteredArtworks.take(3)

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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Tab Header: "Poster" | "Video"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Tab Poster
                PanelTabItem(
                    title = stringResource(R.string.create_exhibit_tab_poster),
                    isSelected = selectedTab == ArtworkType.Poster,
                    onClick = { selectedTab = ArtworkType.Poster },
                    modifier = Modifier.weight(1f),
                )

                // Tab Video
                PanelTabItem(
                    title = stringResource(R.string.create_exhibit_tab_video),
                    isSelected = selectedTab == ArtworkType.Video,
                    onClick = { selectedTab = ArtworkType.Video },
                    modifier = Modifier.weight(1f),
                )
            }

            // 3 Kartu Karya Berjajar Sisi ke Sisi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                for (i in 0 until 3) {
                    val artwork = displayArtworks.getOrNull(i)
                    val assignedSlot = if (artwork != null) {
                        panelSlots.firstOrNull { it.assignedArtwork?.id == artwork.id }
                    } else null

                    PanelArtworkCard(
                        artwork = artwork,
                        isAssigned = assignedSlot != null,
                        slotLabel = assignedSlot?.let { "P${it.slotIndex + 1}" },
                        onClick = {
                            if (artwork != null) {
                                onToggleArtwork(artwork)
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Indikator Paginasi Titik (• • •)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(colors.text),
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(colors.border),
                )
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(colors.border),
                )
            }
        }
    }
}

/**
 * Item Tab dengan Garis Bawah Aktif sesuai Wireframe.
 */
@Composable
private fun PanelTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 15.sp,
            ),
            color = if (isSelected) colors.text else colors.textMuted,
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Indikator Garis Bawah Aktif
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(2.dp)
                .background(if (isSelected) colors.text else Color.Transparent),
        )
    }
}

/**
 * Kartu Persegi Rounded (Aspect Ratio 1:1) Menampilkan Pratinjau Karya Sesuai Wireframe.
 */
@Composable
private fun PanelArtworkCard(
    artwork: ExhibitArtwork?,
    isAssigned: Boolean,
    slotLabel: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(VexRadius.md))
            .background(colors.surfaceInput)
            .border(
                width = if (isAssigned) 2.dp else 1.dp,
                color = if (isAssigned) colors.primary else colors.border,
                shape = RoundedCornerShape(VexRadius.md),
            )
            .clickable(
                enabled = artwork != null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (artwork?.imageRes != null) {
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
                size = 28.dp,
                tint = colors.textMuted,
            )
        }

        // Overlay & Badge Terpasang jika karya sudah ditugaskan ke slot
        if (isAssigned) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(colors.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        VexIcon(
                            id = VexIcons.Check,
                            contentDescription = null,
                            size = 14.dp,
                            tint = Color.White,
                        )
                    }

                    if (slotLabel != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = slotLabel,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Previews
// -------------------------------------------------------------

@Preview(name = "Panels Bottom Panel - Light")
@Composable
private fun ExhibitPanelsBottomPanelLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        ExhibitPanelsBottomPanel(
            panelSlots = listOf(
                ExhibitPanelSlot(slotIndex = 0, assignedArtwork = sampleUserArtworks[0]),
            ),
            availableArtworks = sampleUserArtworks,
            onToggleArtwork = {},
        )
    }
}
