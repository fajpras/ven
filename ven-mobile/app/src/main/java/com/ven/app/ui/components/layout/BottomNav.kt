package com.ven.app.ui.components.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.navigation.BottomNavTab
import com.ven.app.ui.theme.DarkVexColors
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexTheme

/**
 * Custom Bottom Navigation Bar untuk aplikasi VEN (Virtual Exhibition).
 *
 * Sesuai spesifikasi DESIGN.md:
 * - Dibuat custom (bukan NavigationBar M3 bawaan).
 * - Tinggi: [VexSize.bottomNav] (64dp) + inset navigation bar.
 * - Latar belakang: [VexTheme.colors.navBar] (#252525 di Dark Mode, #FFFFFF di Light Mode).
 * - Di Dark Mode: TANPA garis atas.
 * - Di Light Mode: Ada garis atas 1dp border (#DADADA).
 * - 5 Tab: Home, Schedule, AI Assistant, Explore, Profile.
 * - State aktif: Ikon terisi (filled).
 * - State nonaktif: Ikon outline.
 * - Tab Profile: Avatar melingkar 28dp dengan border warna [VexTheme.colors.text] saat aktif.
 * - Warna ikon: Selalu [VexTheme.colors.text] (putih di Dark, hitam di Light).
 * - Tanpa label teks dan tanpa pill indikator.
 */
@Composable
fun BottomNav(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
    tabs: List<BottomNavTab> = BottomNavTab.entries,
    profilePainter: Painter? = null,
    profileInitials: String = "V",
    profileContent: (@Composable (isSelected: Boolean) -> Unit)? = null,
) {
    val colors = VexTheme.colors
    val isDark = colors.bg == DarkVexColors.bg

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.navBar,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            // Garis pembatas atas hanya di Light Mode (di Dark Mode tanpa garis sesuai wireframe)
            if (!isDark) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = colors.border,
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(VexSize.bottomNav),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEach { tab ->
                    val isSelected = tab == selectedTab

                    BottomNavItem(
                        tab = tab,
                        isSelected = isSelected,
                        onClick = { onTabSelected(tab) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        profilePainter = profilePainter,
                        profileInitials = profileInitials,
                        profileContent = profileContent,
                    )
                }
            }
        }
    }
}

/**
 * Item individual pada [BottomNav].
 */
@Composable
private fun BottomNavItem(
    tab: BottomNavTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    profilePainter: Painter? = null,
    profileInitials: String = "V",
    profileContent: (@Composable (isSelected: Boolean) -> Unit)? = null,
) {
    val colors = VexTheme.colors
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.Tab,
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = false,
                    radius = VexSize.minTouch / 2,
                ),
            )
            .semantics {
                this.selected = isSelected
                this.role = Role.Tab
                this.contentDescription = tab.title
            },
        contentAlignment = Alignment.Center,
    ) {
        if (tab.isProfile) {
            // Tab Profile: Foto avatar bulat 28dp dengan border saat aktif
            ProfileTabItem(
                isSelected = isSelected,
                contentDescription = tab.title,
                profilePainter = profilePainter,
                profileInitials = profileInitials,
                profileContent = profileContent,
            )
        } else {
            // Tab 1-4: Home, Schedule, AI, Explore
            val iconRes = if (isSelected) {
                tab.iconPair?.filled ?: 0
            } else {
                tab.iconPair?.outline ?: 0
            }

            VexIcon(
                id = iconRes,
                contentDescription = tab.title,
                size = VexSize.iconLg,
                tint = colors.text,
            )
        }
    }
}

/**
 * Avatar item khusus untuk tab Profile.
 */
@Composable
private fun ProfileTabItem(
    isSelected: Boolean,
    contentDescription: String,
    modifier: Modifier = Modifier,
    profilePainter: Painter? = null,
    profileInitials: String = "V",
    profileContent: (@Composable (isSelected: Boolean) -> Unit)? = null,
) {
    val colors = VexTheme.colors
    val avatarSize = VexSize.iconLg // 28dp

    val borderModifier = if (isSelected) {
        Modifier.border(
            width = 1.5.dp,
            color = colors.text,
            shape = CircleShape,
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(avatarSize)
            .then(borderModifier),
        contentAlignment = Alignment.Center,
    ) {
        when {
            profileContent != null -> {
                profileContent(isSelected)
            }
            profilePainter != null -> {
                Image(
                    painter = profilePainter,
                    contentDescription = contentDescription,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            }
            else -> {
                // Placeholder avatar bila belum ada foto
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(colors.surfaceInput),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = profileInitials.take(1).uppercase(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = colors.text,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// Previews untuk 2 Mode: Dark Mode & Light Mode
// ---------------------------------------------------------

@Preview(name = "BottomNav - Dark Mode (Home Selected)", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun BottomNavDarkHomePreview() {
    VexTheme(mode = ThemeMode.Dark) {
        BottomNav(
            selectedTab = BottomNavTab.Home,
            onTabSelected = {},
        )
    }
}

@Preview(name = "BottomNav - Dark Mode (Profile Selected)", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun BottomNavDarkProfilePreview() {
    VexTheme(mode = ThemeMode.Dark) {
        BottomNav(
            selectedTab = BottomNavTab.Profile,
            onTabSelected = {},
        )
    }
}

@Preview(name = "BottomNav - Light Mode (Home Selected)", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BottomNavLightHomePreview() {
    VexTheme(mode = ThemeMode.Light) {
        BottomNav(
            selectedTab = BottomNavTab.Home,
            onTabSelected = {},
        )
    }
}

@Preview(name = "BottomNav - Light Mode (AI Selected)", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BottomNavLightAiPreview() {
    VexTheme(mode = ThemeMode.Light) {
        BottomNav(
            selectedTab = BottomNavTab.Ai,
            onTabSelected = {},
        )
    }
}

@Preview(name = "BottomNav - Light Mode (Profile Selected)", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BottomNavLightProfilePreview() {
    VexTheme(mode = ThemeMode.Light) {
        BottomNav(
            selectedTab = BottomNavTab.Profile,
            onTabSelected = {},
        )
    }
}
