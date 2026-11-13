package com.ven.app.ui.components.feature

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                .padding(horizontal = VexSpace.s6)
                .padding(bottom = VexSpace.s8)
                .navigationBarsPadding(),
        ) {
            // Header: Judul, Subtitle, dan Tombol Tutup
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = VexSpace.s2, bottom = VexSpace.s5),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.create_sheet_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.2).sp,
                        ),
                        color = colors.text,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.create_sheet_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                    )
                }

                // Tombol Dismiss Lingkaran
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(VexRadius.full))
                        .background(colors.surfaceInput)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = colors.text.copy(alpha = 0.2f)),
                            role = Role.Button,
                            onClick = onDismissRequest,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    VexIcon(
                        id = VexIcons.Close,
                        contentDescription = stringResource(R.string.create_sheet_close),
                        tint = colors.textSecondary,
                        size = 18.dp,
                    )
                }
            }

            // Pilihan 1: New Post
            CreateOptionCard(
                iconRes = VexIcons.Tab.Posts.filled,
                accentColor = colors.primary,
                badgeText = stringResource(R.string.create_sheet_post_badge),
                title = stringResource(R.string.create_sheet_post_title),
                description = stringResource(R.string.create_sheet_post_desc),
                isFlagship = false,
                onClick = onCreatePostClick,
            )

            Spacer(modifier = Modifier.height(VexSpace.s3))

            // Pilihan 2: New Exhibit (3D)
            CreateOptionCard(
                iconRes = VexIcons.Tab.Exhibits.filled,
                accentColor = Color(0xFFFF9A00),
                badgeText = stringResource(R.string.create_sheet_exhibit_badge),
                title = stringResource(R.string.create_sheet_exhibit_title),
                description = stringResource(R.string.create_sheet_exhibit_desc),
                isFlagship = true,
                onClick = onCreateExhibitClick,
            )
        }
    }
}

@Composable
private fun CreateOptionCard(
    iconRes: Int,
    accentColor: Color,
    badgeText: String,
    title: String,
    description: String,
    isFlagship: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors
    val cardShape = RoundedCornerShape(VexRadius.lg)

    val borderColor = if (isFlagship) {
        accentColor.copy(alpha = 0.40f)
    } else {
        colors.border.copy(alpha = 0.70f)
    }

    val cardBg = if (isFlagship) {
        colors.surfaceInput.copy(alpha = 0.75f)
    } else {
        colors.surfaceInput.copy(alpha = 0.65f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(cardBg)
            .border(BorderStroke(1.dp, borderColor), cardShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = accentColor.copy(alpha = 0.15f)),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(VexSpace.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Kotak Ikon dengan Efek Gradien & Border Halus
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.22f),
                            accentColor.copy(alpha = 0.08f),
                        ),
                    ),
                )
                .border(
                    BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(14.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            VexIcon(
                id = iconRes,
                contentDescription = title,
                tint = accentColor,
                size = 26.dp,
            )
        }

        Spacer(modifier = Modifier.width(VexSpace.s4))

        // Konten Teks & Badge
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.1).sp,
                    ),
                    color = colors.text,
                )

                // Badge Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(VexRadius.full))
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(
                            BorderStroke(0.8.dp, accentColor.copy(alpha = 0.35f)),
                            RoundedCornerShape(VexRadius.full),
                        )
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp,
                        ),
                        color = accentColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(
                    lineHeight = 16.sp,
                ),
                color = colors.textSecondary,
            )
        }

        Spacer(modifier = Modifier.width(VexSpace.s2))

        // Chevron Right Bulat
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(VexRadius.full))
                .background(colors.surfaceInput.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center,
        ) {
            VexIcon(
                id = VexIcons.ChevronRight,
                contentDescription = null,
                tint = if (isFlagship) accentColor.copy(alpha = 0.8f) else colors.textMuted,
                size = 16.dp,
            )
        }
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
