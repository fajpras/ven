package com.ven.app.ui.screens.create.exhibit.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.ven.app.ui.components.ui.VexButton
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.theme.BrandPurple
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Panel Bawah Layar Utama Editor Pameran (Wireframe Create Exhibition - Top Screen).
 *
 * Menampilkan:
 * 1. Checklist Syarat Penerbitan:
 *    - "title & description" (merah ✕ jika belum, hijau ✓ jika lengkap)
 *    - "a panel" (merah ✕ jika 0 panel, hijau ✓ jika minimal 1 panel terisi)
 * 2. Tombol CTA "Publish":
 *    - Disabled / pudar jika syarat belum terpenuhi
 *    - Ungu solid aktif jika kedua syarat terpenuhi
 */
@Composable
fun ExhibitMainBottomPanel(
    isTitleDescComplete: Boolean,
    isPanelComplete: Boolean,
    canPublish: Boolean,
    isPublishing: Boolean,
    onTitleDescClick: () -> Unit,
    onPanelClick: () -> Unit,
    onPublish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors
    val panelShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)

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
        ) {
            // Checklist 2 Persyaratan
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Syarat 1: title & description
                WireframeChecklistItem(
                    isCompleted = isTitleDescComplete,
                    label = stringResource(R.string.create_exhibit_check_title_desc),
                    onClick = onTitleDescClick,
                )

                // Syarat 2: a panel
                WireframeChecklistItem(
                    isCompleted = isPanelComplete,
                    label = stringResource(R.string.create_exhibit_check_panel),
                    onClick = onPanelClick,
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Tombol Publish
            VexButton(
                text = stringResource(R.string.create_exhibit_publish_action),
                onClick = onPublish,
                enabled = canPublish && !isPublishing,
                loading = isPublishing,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * Item checklist sesuai wireframe (Image 2):
 * Menampilkan ikon status lingkaran kecil (merah ✕ atau hijau ✓) dan teks berwarna sesuai status.
 */
@Composable
private fun WireframeChecklistItem(
    isCompleted: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val completedColor = Color(0xFF00E676)
    val incompleteColor = Color(0xFFFF3B30)
    val statusColor = if (isCompleted) completedColor else incompleteColor

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(VexRadius.md))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = statusColor.copy(alpha = 0.15f)),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Lingkaran Ikon Status (Merah ✕ atau Hijau ✓)
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(statusColor),
            contentAlignment = Alignment.Center,
        ) {
            if (isCompleted) {
                VexIcon(
                    id = VexIcons.Check,
                    contentDescription = null,
                    size = 11.dp,
                    tint = Color.White,
                )
            } else {
                Text(
                    text = "✕",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                    ),
                    color = Color.White,
                )
            }
        }

        // Teks Kriteria berwarna sesuai status wireframe
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
            ),
            color = statusColor,
        )
    }
}

// -------------------------------------------------------------
// Previews
// -------------------------------------------------------------

@Preview(name = "Main Panel - Incomplete State")
@Composable
private fun ExhibitMainBottomPanelIncompletePreview() {
    VexTheme(mode = ThemeMode.Light) {
        ExhibitMainBottomPanel(
            isTitleDescComplete = false,
            isPanelComplete = false,
            canPublish = false,
            isPublishing = false,
            onTitleDescClick = {},
            onPanelClick = {},
            onPublish = {},
        )
    }
}

@Preview(name = "Main Panel - Complete State")
@Composable
private fun ExhibitMainBottomPanelCompletePreview() {
    VexTheme(mode = ThemeMode.Light) {
        ExhibitMainBottomPanel(
            isTitleDescComplete = true,
            isPanelComplete = true,
            canPublish = true,
            isPublishing = false,
            onTitleDescClick = {},
            onPanelClick = {},
            onPublish = {},
        )
    }
}
