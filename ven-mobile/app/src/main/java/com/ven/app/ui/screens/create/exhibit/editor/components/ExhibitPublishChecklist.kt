package com.ven.app.ui.screens.create.exhibit.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ven.app.R
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Checklist Syarat Penerbitan Pameran Virtual (DESIGN.md §8.14.2):
 * - "title & description" (hijau ✓ jika lengkap, merah ✕ jika belum)
 * - "a panel" (hijau ✓ jika minimal 1 panel terisi, merah ✕ jika belum)
 */
@Composable
fun ExhibitPublishChecklist(
    isTitleDescComplete: Boolean,
    isPanelComplete: Boolean,
    modifier: Modifier = Modifier,
    onTitleDescClick: () -> Unit = {},
    onPanelClick: () -> Unit = {},
) {
    val colors = VexTheme.colors

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(VexSpace.s3),
    ) {
        // Syarat 1: Title & Description
        ChecklistItem(
            isCompleted = isTitleDescComplete,
            label = stringResource(R.string.create_exhibit_check_title_desc),
            onClick = onTitleDescClick,
        )

        // Syarat 2: A Panel (minimal 1 panel terisi)
        ChecklistItem(
            isCompleted = isPanelComplete,
            label = stringResource(R.string.create_exhibit_check_panel),
            onClick = onPanelClick,
        )
    }
}

@Composable
private fun ChecklistItem(
    isCompleted: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors
    val successColor = colors.success
    val errorColor = colors.danger

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(VexRadius.md))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = colors.text.copy(alpha = 0.12f)),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(vertical = VexSpace.s1, horizontal = VexSpace.s2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(VexSpace.s3),
    ) {
        // Indikator Lingkaran Status
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isCompleted) successColor else errorColor),
            contentAlignment = Alignment.Center,
        ) {
            if (isCompleted) {
                VexIcon(
                    id = VexIcons.Check,
                    contentDescription = null,
                    size = 12.dp,
                    tint = Color.White,
                )
            } else {
                Text(
                    text = "✕",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                    ),
                    color = Color.White,
                )
            }
        }

        // Teks Syarat
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isCompleted) FontWeight.SemiBold else FontWeight.Normal,
            ),
            color = if (isCompleted) colors.text else colors.textMuted,
        )
    }
}

// -------------------------------------------------------------
// Previews
// -------------------------------------------------------------

@androidx.compose.ui.tooling.preview.Preview(name = "Checklist - Dark", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun ExhibitPublishChecklistDarkPreview() {
    com.ven.app.ui.theme.VexTheme(mode = com.ven.app.ui.theme.ThemeMode.Dark) {
        ExhibitPublishChecklist(
            isTitleDescComplete = true,
            isPanelComplete = false,
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Checklist - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ExhibitPublishChecklistLightPreview() {
    com.ven.app.ui.theme.VexTheme(mode = com.ven.app.ui.theme.ThemeMode.Light) {
        ExhibitPublishChecklist(
            isTitleDescComplete = true,
            isPanelComplete = false,
        )
    }
}
