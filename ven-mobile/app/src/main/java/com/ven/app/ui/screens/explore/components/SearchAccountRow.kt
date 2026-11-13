package com.ven.app.ui.screens.explore.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.ven.app.ui.components.ui.Avatar
import com.ven.app.ui.components.ui.AvatarSize
import com.ven.app.ui.screens.explore.ExploreAccountItem
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Baris hasil pencarian akun pada Explore.
 * Sesuai DESIGN.md §8.5.3 & wireframe `Wireframe - Explore account.png`:
 * - Avatar bulat 40dp + @username (SemiBold) + nama lengkap (muted).
 * - Tap membuka profil akun.
 */
@Composable
fun SearchAccountRow(
    account: ExploreAccountItem,
    onClick: () -> Unit,
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
                onClick = onClick,
            )
            .padding(horizontal = VexSpace.s6, vertical = VexSpace.s3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            initials = account.username,
            size = AvatarSize.Md,
            imageRes = account.avatarRes,
            imageUrl = account.avatarUrl,
            contentDescription = account.fullName,
        )

        Spacer(modifier = Modifier.width(VexSpace.s4))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.username,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = account.fullName,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

@Preview(name = "SearchAccountRow - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun SearchAccountRowDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        SearchAccountRow(
            account = ExploreAccountItem(
                id = "acc_1",
                username = "vex.pbl204",
                fullName = "Virtual Exhibition (VEX)",
            ),
            onClick = {},
        )
    }
}

@Preview(name = "SearchAccountRow - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SearchAccountRowLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        SearchAccountRow(
            account = ExploreAccountItem(
                id = "acc_2",
                username = "valuexnet.310",
                fullName = "Value Exchange Network (VEN)",
            ),
            onClick = {},
        )
    }
}
