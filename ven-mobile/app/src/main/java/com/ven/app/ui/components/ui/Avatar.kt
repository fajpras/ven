package com.ven.app.ui.components.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexTheme

enum class AvatarSize(val dp: Dp) {
    Sm(VexSize.avatarSm),
    Md(VexSize.avatarMd),
    Lg(VexSize.avatarLg),
}

/**
 * Komponen Avatar melingkar untuk VEN (Virtual Exhibition).
 * Sesuai DESIGN.md §6.1:
 * - Ukuran Sm (24dp), Md (40dp), Lg (96dp).
 * - Menampilkan gambar karya/pengguna bila [imageRes] tersedia.
 * - Menampilkan inisial huruf besar pada latar [surfaceInput] bila belum ada gambar.
 */
@Composable
fun Avatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: AvatarSize = AvatarSize.Md,
    @DrawableRes imageRes: Int? = null,
    imageBitmap: ImageBitmap? = null,
    contentDescription: String? = null,
) {
    val colors = VexTheme.colors

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (imageBitmap != null) {
            Image(
                bitmap = imageBitmap,
                contentDescription = contentDescription,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        } else if (imageRes != null) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = contentDescription,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            val textStyle = when (size) {
                AvatarSize.Sm -> MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                AvatarSize.Md -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                AvatarSize.Lg -> MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(colors.surfaceInput),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initials.trim().take(1).uppercase(),
                    style = textStyle,
                    color = colors.text,
                )
            }
        }
    }
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

@Preview(name = "Avatar - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun AvatarDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        Avatar(initials = "G", size = AvatarSize.Lg)
    }
}

@Preview(name = "Avatar - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AvatarLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        Avatar(initials = "G", size = AvatarSize.Lg)
    }
}
