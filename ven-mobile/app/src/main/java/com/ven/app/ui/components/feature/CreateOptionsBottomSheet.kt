package com.ven.app.ui.components.feature

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.components.ui.VexBottomSheet
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Bottom Sheet pilihan pembuatan konten baru (Create Flow).
 * Dipicu ketika tombol "+" pada TopBar HomeScreen atau ProfileScreen diklik.
 * Memberikan akses langsung untuk membuat:
 * 1. Post (Karya 2D / foto / poster linimasa).
 * 2. Exhibit (Ruang pameran virtual 3D interaktif).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOptionsBottomSheet(
    onDismissRequest: () -> Unit,
    onCreatePostClick: () -> Unit,
    onCreateExhibitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    VexBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = VexSpace.s6)
                .navigationBarsPadding(),
        ) {
            // Header Minimalis: Judul + Tombol Tutup Bersih
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = VexSpace.s6, end = VexSpace.s4)
                    .padding(top = VexSpace.s1, bottom = VexSpace.s2),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.create_sheet_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = colors.text,
                )

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.size(36.dp),
                ) {
                    VexIcon(
                        id = VexIcons.Close,
                        contentDescription = stringResource(R.string.create_sheet_close),
                        tint = colors.textMuted,
                        size = 20.dp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(VexSpace.s1))

            // Pilihan 1: New Post
            CreateOptionRow(
                iconRes = VexIcons.Tab.Posts.filled,
                iconTint = colors.primary,
                iconBgColor = colors.primary.copy(alpha = 0.10f),
                title = stringResource(R.string.create_sheet_post_title),
                description = stringResource(R.string.create_sheet_post_desc),
                onClick = onCreatePostClick,
            )

            // Garis Pembatas Halus (Hairline Divider)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = VexSpace.s6)
                    .height(0.6.dp)
                    .background(colors.border.copy(alpha = 0.4f)),
            )

            // Pilihan 2: New Exhibit (3D)
            CreateOptionRow(
                iconRes = VexIcons.Tab.Exhibits.filled,
                iconTint = Color(0xFFFF9A00),
                iconBgColor = Color(0xFFFF9A00).copy(alpha = 0.12f),
                title = stringResource(R.string.create_sheet_exhibit_title),
                description = stringResource(R.string.create_sheet_exhibit_desc),
                onClick = onCreateExhibitClick,
            )
        }
    }
}

@Composable
private fun CreateOptionRow(
    iconRes: Int,
    iconTint: Color,
    iconBgColor: Color,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = colors.text.copy(alpha = 0.08f)),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = VexSpace.s6, vertical = VexSpace.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Kotak Ikon Lembut
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(VexRadius.md))
                .background(iconBgColor),
            contentAlignment = Alignment.Center,
        ) {
            VexIcon(
                id = iconRes,
                contentDescription = null,
                tint = iconTint,
                size = 22.dp,
            )
        }

        Spacer(modifier = Modifier.width(VexSpace.s4))

        // Label Judul & Deskripsi
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.text,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
            )
        }

        Spacer(modifier = Modifier.width(VexSpace.s2))

        // Chevron Ramping
        VexIcon(
            id = VexIcons.ChevronRight,
            contentDescription = null,
            tint = colors.textMuted.copy(alpha = 0.6f),
            size = 18.dp,
        )
    }
}

// -------------------------------------------------------------
// Previews
// -------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "CreateOptionsBottomSheet - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun CreateOptionsBottomSheetDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        CreateOptionsBottomSheet(
            onDismissRequest = {},
            onCreatePostClick = {},
            onCreateExhibitClick = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "CreateOptionsBottomSheet - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CreateOptionsBottomSheetLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        CreateOptionsBottomSheet(
            onDismissRequest = {},
            onCreatePostClick = {},
            onCreateExhibitClick = {},
        )
    }
}
