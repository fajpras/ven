package com.ven.app.ui.components.feature

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Kartu Post Karya Seni / Pameran untuk Feed di Home Screen.
 *
 * Sesuai spesifikasi DESIGN.md §6.3 & §8.6:
 * - Header Kartu:
 *   - Kiri: Nama pembuat karya `@username` (18sp SemiBold 600, warna text).
 *   - Kanan: Baris 2 ikon aksi (24dp, warna text, jarak 16dp):
 *     1. Ikon Kalender ([VexIcons.Calendar]): Membuka alur Request Meeting.
 *     2. Ikon Play / Kubus 3D ([VexIcons.Play]): Membuka Virtual Exhibition Room 3D.
 * - Media Post (Gambar Karya):
 *   - Tampilan penuh selebar layar (edge-to-edge), tanpa radius sudut (0dp, sudut tajam).
 */
@Composable
fun PostCard(
    username: String,
    onRequestMeetingClick: () -> Unit,
    onView3dClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    aspectRatio: Float = 4f / 5f,
    placeholderColor: Color = VexTheme.colors.surfaceInput,
) {
    val colors = VexTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.bg),
    ) {
        // ---------------------------------------------------------
        // Header Kartu Post
        // ---------------------------------------------------------
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = VexSpace.s6,
                    end = VexSpace.s6,
                    top = VexSpace.s4,
                    bottom = VexSpace.s3,
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Kiri: Username pembuat karya (mis. "@Graaph")
            Text(
                text = if (username.startsWith("@")) username else "@$username",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.text,
            )

            // Kanan: Baris 2 Ikon Aksi (Kalender & Play)
            Row(
                horizontalArrangement = Arrangement.spacedBy(VexSpace.s4),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Ikon Kalender -> Request Meeting
                IconButton(
                    onClick = onRequestMeetingClick,
                    modifier = Modifier.padding(0.dp),
                ) {
                    VexIcon(
                        id = VexIcons.Calendar,
                        contentDescription = stringResource(R.string.home_request_meeting),
                        size = VexSize.icon,
                        tint = colors.text,
                    )
                }

                // Ikon Play / Kubus 3D -> Lihat Pameran 3D
                IconButton(
                    onClick = onView3dClick,
                    modifier = Modifier.padding(0.dp),
                ) {
                    VexIcon(
                        id = VexIcons.Play,
                        contentDescription = stringResource(R.string.home_view_3d),
                        size = VexSize.icon,
                        tint = colors.text,
                    )
                }
            }
        }

        // ---------------------------------------------------------
        // Media Post (Gambar Karya - Full Edge-to-Edge, 0dp Radius)
        // ---------------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .background(placeholderColor),
            contentAlignment = Alignment.Center,
        ) {
            // Bila belum ada Coil image loader, tampilkan placeholder visual netral
            Text(
                text = "Media Content",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted,
            )
        }
    }
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

@Preview(name = "PostCard - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun PostCardDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        PostCard(
            username = "Graaph",
            onRequestMeetingClick = {},
            onView3dClick = {},
        )
    }
}

@Preview(name = "PostCard - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PostCardLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        PostCard(
            username = "Graaph",
            onRequestMeetingClick = {},
            onView3dClick = {},
        )
    }
}
