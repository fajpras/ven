package com.ven.app.ui.components.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.icons.VexLogo
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * TopBar khusus untuk aplikasi VEN (Virtual Exhibition).
 *
 * Sesuai spesifikasi DESIGN.md §6.2 & §8.6:
 * - VexTopBar.Home:
 *   - Sisi Kiri: Tombol Tambah ("+") untuk membuka alur Create Flow (New Post / New Exhibit).
 *   - Sisi Kanan: Logo resmi VEN.
 *   - Latar belakang menyatu dengan canvas (VexTheme.colors.bg / #1F1F1F).
 *   - Tanpa garis pembatas bawah di Dark Mode [OBSERVED].
 *
 * - VexTopBar.Back:
 *   - SubPage header: Tombol Kembali ("<-", ic_action_back) di kiri.
 *   - Judul halaman (SemiBold 600, warna text).
 *   - Garis pembatas bawah 1dp (VexTheme.colors.border).
 */
object VexTopBar {

    @Composable
    fun Home(
        onCreateClick: () -> Unit,
        modifier: Modifier = Modifier,
    ) {
        val colors = VexTheme.colors

        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(colors.bg)
                .statusBarsPadding()
                .height(VexSize.topBar),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = VexSpace.s6),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Sisi Kiri: Ikon "+" (ic_action_add)
                IconButton(
                    onClick = onCreateClick,
                    modifier = Modifier.padding(start = 0.dp),
                ) {
                    VexIcon(
                        id = VexIcons.Add,
                        contentDescription = stringResource(R.string.home_create_post),
                        size = VexSize.icon,
                        tint = colors.text,
                    )
                }

                // Sisi Kanan: Logo Resmi VEN
                VexLogo(
                    modifier = Modifier.height(28.dp),
                    contentDescription = stringResource(R.string.app_name),
                )
            }
        }
    }

    @Composable
    fun Back(
        title: String,
        onBackClick: () -> Unit,
        modifier: Modifier = Modifier,
        showDivider: Boolean = true,
    ) {
        val colors = VexTheme.colors

        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(colors.bg)
                .statusBarsPadding(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(VexSize.topBar),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = VexSpace.s4),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onBackClick,
                    ) {
                        VexIcon(
                            id = VexIcons.Back,
                            contentDescription = "Back",
                            size = VexSize.icon,
                            tint = colors.text,
                        )
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = colors.text,
                        modifier = Modifier.padding(start = VexSpace.s2),
                    )
                }
            }

            if (showDivider) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = colors.border,
                )
            }
        }
    }
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

@Preview(name = "Home TopBar - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun HomeTopBarDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        VexTopBar.Home(onCreateClick = {})
    }
}

@Preview(name = "Home TopBar - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun HomeTopBarLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        VexTopBar.Home(onCreateClick = {})
    }
}

@Preview(name = "Back TopBar - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun BackTopBarDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        VexTopBar.Back(title = "Forgot Password", onBackClick = {})
    }
}

@Preview(name = "Back TopBar - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BackTopBarLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        VexTopBar.Back(title = "Forgot Password", onBackClick = {})
    }
}
