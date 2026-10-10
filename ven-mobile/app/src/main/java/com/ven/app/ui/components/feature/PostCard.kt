package com.ven.app.ui.components.feature

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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
 * Model potongan teks hasil parsing URL dalam deskripsi.
 */
data class CaptionTextPart(
    val text: String,
    val url: String? = null,
)

private val URL_REGEX = Regex(
    """(?i)\b(?:https?://|www\.)[^\s<>"'{}|\\^`]+"""
)

private val TRAILING_PUNCTUATION = charArrayOf('.', ',', '!', '?', ';', ':', ')', ']', '}')

/**
 * Memecah string menjadi daftar potongan teks biasa dan URL.
 * Tanda baca trailing (seperti titik atau tanda seru di akhir URL) dipisahkan agar tautan valid.
 */
fun parseCaptionTextWithUrls(input: String): List<CaptionTextPart> {
    if (input.isEmpty()) return emptyList()

    val parts = mutableListOf<CaptionTextPart>()
    var lastIndex = 0

    for (match in URL_REGEX.findAll(input)) {
        val matchStart = match.range.first
        val rawMatch = match.value

        var trimmedLength = rawMatch.length
        while (trimmedLength > 0 && rawMatch[trimmedLength - 1] in TRAILING_PUNCTUATION) {
            trimmedLength--
        }

        if (trimmedLength == 0) continue

        val actualUrlText = rawMatch.substring(0, trimmedLength)

        if (matchStart > lastIndex) {
            parts.add(CaptionTextPart(text = input.substring(lastIndex, matchStart)))
        }

        val destinationUrl = if (actualUrlText.startsWith("http://", ignoreCase = true) ||
            actualUrlText.startsWith("https://", ignoreCase = true)
        ) {
            actualUrlText
        } else {
            "https://$actualUrlText"
        }
        parts.add(CaptionTextPart(text = actualUrlText, url = destinationUrl))

        lastIndex = matchStart + actualUrlText.length
    }

    if (lastIndex < input.length) {
        parts.add(CaptionTextPart(text = input.substring(lastIndex)))
    }

    return parts
}

/**
 * Membangun [AnnotatedString] berisi nama pengguna tebal (bold) dan deskripsi dengan link yang dapat diklik.
 */
fun buildPostCaptionAnnotatedString(
    username: String,
    description: String,
    textColor: Color,
    linkColor: Color,
    uriHandler: UriHandler,
    onUsernameClick: (() -> Unit)? = null,
    onUrlClick: ((String) -> Unit)? = null,
): AnnotatedString {
    return buildAnnotatedString {
        val cleanUsername = if (username.startsWith("@")) username.removePrefix("@") else username
        if (cleanUsername.isNotEmpty()) {
            val usernameStyle = SpanStyle(
                fontWeight = FontWeight.SemiBold,
                color = textColor,
            )
            if (onUsernameClick != null) {
                pushLink(
                    LinkAnnotation.Clickable(
                        tag = "username",
                        styles = TextLinkStyles(style = usernameStyle),
                        linkInteractionListener = { onUsernameClick() },
                    )
                )
                append(cleanUsername)
                pop()
            } else {
                withStyle(usernameStyle) {
                    append(cleanUsername)
                }
            }

            if (description.isNotEmpty() && !description.startsWith("\n")) {
                append(" ")
            }
        }

        val parts = parseCaptionTextWithUrls(description)
        for (part in parts) {
            if (part.url != null) {
                val destinationUrl = part.url
                pushLink(
                    LinkAnnotation.Url(
                        url = destinationUrl,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline,
                                fontWeight = FontWeight.Medium,
                            ),
                        ),
                        linkInteractionListener = { link ->
                            val urlToOpen = (link as? LinkAnnotation.Url)?.url ?: destinationUrl
                            if (onUrlClick != null) {
                                onUrlClick(urlToOpen)
                            } else {
                                try {
                                    uriHandler.openUri(urlToOpen)
                                } catch (_: Exception) {
                                }
                            }
                        },
                    )
                )
                append(part.text)
                pop()
            } else {
                withStyle(
                    SpanStyle(
                        color = textColor,
                        fontWeight = FontWeight.Normal,
                    )
                ) {
                    append(part.text)
                }
            }
        }
    }
}

/**
 * Komponen teks caption untuk kartu post:
 * - Menampilkan username dan teks deskripsi.
 * - Memotong teks panjang ke [collapsedMaxLines] baris dengan opsi 'see more' / 'see less'.
 * - Link URL di dalam deskripsi dapat diklik untuk membuka peramban web.
 */
@Composable
fun PostCaption(
    username: String,
    description: String,
    modifier: Modifier = Modifier,
    collapsedMaxLines: Int = 2,
    seeMoreText: String = stringResource(R.string.post_see_more),
    seeLessText: String = stringResource(R.string.post_see_less),
    onUsernameClick: (() -> Unit)? = null,
    onUrlClick: ((String) -> Unit)? = null,
) {
    val colors = VexTheme.colors
    val uriHandler = LocalUriHandler.current

    var isExpanded by remember { mutableStateOf(false) }
    val isLikelyLong = remember(description, collapsedMaxLines) {
        description.length > 90 || description.count { it == '\n' } >= collapsedMaxLines
    }
    var hasVisualOverflow by remember(description) { mutableStateOf(isLikelyLong) }

    val captionText = remember(
        username,
        description,
        colors.text,
        colors.primaryText,
        uriHandler,
        onUsernameClick,
        onUrlClick,
    ) {
        buildPostCaptionAnnotatedString(
            username = username,
            description = description,
            textColor = colors.text,
            linkColor = colors.primaryText,
            uriHandler = uriHandler,
            onUsernameClick = onUsernameClick,
            onUrlClick = onUrlClick,
        )
    }

    Column(
        modifier = modifier.animateContentSize(),
    ) {
        Text(
            text = captionText,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (isExpanded) Int.MAX_VALUE else collapsedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
                if (!isExpanded) {
                    hasVisualOverflow = textLayoutResult.hasVisualOverflow
                }
            },
        )

        if (hasVisualOverflow || isExpanded) {
            Text(
                text = if (isExpanded) seeLessText else seeMoreText,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.textSecondary,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable(role = Role.Button) {
                        isExpanded = !isExpanded
                    },
            )
        }
    }
}

/**
 * Kartu Post Karya Seni / Pameran untuk Feed di Home Screen.
 *
 * Sesuai spesifikasi DESIGN.md §6.3 & §8.6:
 * - Header Kartu:
 *   - Kiri: Nama pembuat karya `@username` (18sp SemiBold 600, warna text).
 *   - Kanan: Baris 2 ikon aksi (24dp, warna text, jarak 16dp):
 *     1. Ikon Jadwal ([VexIcons.ScheduleAction]): Membuka alur Request Meeting.
 *     2. Ikon Play / Kubus 3D ([VexIcons.Play]): Membuka Virtual Exhibition Room 3D.
 * - Media Post (Gambar Karya):
 *   - Tampilan penuh selebar layar (edge-to-edge), tanpa radius sudut (0dp, sudut tajam).
 * - Konten Bawah Media (Caption):
 *   - Menampilkan `username` dan `description`.
 *   - Fitur 'see more' / 'see less' untuk deskripsi yang panjang.
 *   - Tautan URL (link) di dalam deskripsi dapat diklik langsung.
 */
@Composable
fun PostCard(
    username: String,
    onRequestMeetingClick: () -> Unit,
    onView3dClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String = "",
    imageUrl: String? = null,
    aspectRatio: Float = 4f / 5f,
    placeholderColor: Color = VexTheme.colors.surfaceInput,
    collapsedMaxLines: Int = 2,
    seeMoreText: String = stringResource(R.string.post_see_more),
    seeLessText: String = stringResource(R.string.post_see_less),
    onUsernameClick: (() -> Unit)? = null,
    onUrlClick: ((String) -> Unit)? = null,
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
                // Ikon Jadwal -> Request Meeting
                IconButton(
                    onClick = onRequestMeetingClick,
                    modifier = Modifier.padding(0.dp),
                ) {
                    VexIcon(
                        id = VexIcons.ScheduleAction,
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

        // ---------------------------------------------------------
        // Caption Post: Username, Description, See More & Clickable Links
        // ---------------------------------------------------------
        if (description.isNotBlank()) {
            PostCaption(
                username = username,
                description = description,
                collapsedMaxLines = collapsedMaxLines,
                seeMoreText = seeMoreText,
                seeLessText = seeLessText,
                onUsernameClick = onUsernameClick,
                onUrlClick = onUrlClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = VexSpace.s6,
                        end = VexSpace.s6,
                        top = VexSpace.s3,
                        bottom = VexSpace.s4,
                    ),
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
            description = "Concept art eksplorasi arsitektur cyberpunk dan pencahayaan neon untuk kompetisi open source. Kunjungi showroom 3D di https://vex.art/exhibit/digital-concept untuk melihat detail tekstur dan aset interaktif! Jangan lupa tinggalkan feedback Anda sebelum expo dimulai.",
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
            description = "Concept art eksplorasi arsitektur cyberpunk dan pencahayaan neon untuk kompetisi open source. Kunjungi showroom 3D di https://vex.art/exhibit/digital-concept untuk melihat detail tekstur dan aset interaktif! Jangan lupa tinggalkan feedback Anda sebelum expo dimulai.",
            onRequestMeetingClick = {},
            onView3dClick = {},
        )
    }
}
