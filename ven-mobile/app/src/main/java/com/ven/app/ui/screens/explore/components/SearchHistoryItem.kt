package com.ven.app.ui.screens.explore.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.ui.components.ui.Avatar
import com.ven.app.ui.components.ui.AvatarSize
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.screens.explore.RecentSearchItem
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Baris riwayat pencarian Explore ("Recents").
 * Sesuai DESIGN.md §8.5.2 & wireframe `Wireframe - Explore history.png`.
 * Mendukung dua varian melalui [RecentSearchItem]:
 * - [RecentSearchItem.Query]: lingkaran abu-abu + ikon jam + teks kueri.
 * - [RecentSearchItem.Account]: avatar bulat + @username + nama lengkap.
 * Kedua varian memiliki tombol hapus "✕" di sisi kanan.
 */
@Composable
fun SearchHistoryItem(
    item: RecentSearchItem,
    onItemClick: (RecentSearchItem) -> Unit,
    onRemoveClick: (RecentSearchItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = colors.text.copy(alpha = 0.12f)),
                role = Role.Button,
                onClick = { onItemClick(item) },
            )
            .padding(horizontal = VexSpace.s6, vertical = VexSpace.s3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (item) {
            is RecentSearchItem.Query -> {
                // Lingkaran abu-abu + ikon jam
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceInput),
                    contentAlignment = Alignment.Center,
                ) {
                    VexIcon(
                        id = VexIcons.History,
                        contentDescription = null,
                        size = VexSize.iconSm,
                        tint = colors.textMuted,
                    )
                }

                Spacer(modifier = Modifier.width(VexSpace.s4))

                Text(
                    text = item.query,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }

            is RecentSearchItem.Account -> {
                Avatar(
                    initials = item.username,
                    size = AvatarSize.Md,
                    imageRes = item.avatarRes,
                    imageUrl = item.avatarUrl,
                    contentDescription = item.fullName,
                )

                Spacer(modifier = Modifier.width(VexSpace.s4))

                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = item.username,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = colors.text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Text(
                        text = item.fullName,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        // Tombol hapus "✕"
        Box(
            modifier = Modifier
                .size(VexSize.minTouch)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, radius = 20.dp),
                    role = Role.Button,
                    onClick = { onRemoveClick(item) },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "✕",
                style = MaterialTheme.typography.bodyLarge,
                color = colors.textMuted,
            )
        }
    }
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

@Preview(name = "SearchHistoryItem - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun SearchHistoryItemDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SearchHistoryItem(
                item = RecentSearchItem.Query(id = "q1", query = "vex"),
                onItemClick = {},
                onRemoveClick = {},
            )
            SearchHistoryItem(
                item = RecentSearchItem.Account(
                    id = "a1",
                    username = "gra._.aph",
                    fullName = "Terra Faqih Satria Madjid",
                ),
                onItemClick = {},
                onRemoveClick = {},
            )
        }
    }
}

@Preview(name = "SearchHistoryItem - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SearchHistoryItemLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SearchHistoryItem(
                item = RecentSearchItem.Account(
                    id = "a1",
                    username = "shaf.one",
                    fullName = "Shafwan Fuad",
                ),
                onItemClick = {},
                onRemoveClick = {},
            )
        }
    }
}
