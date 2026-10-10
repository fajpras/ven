package com.ven.app.ui.components.feature

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.LinkAnnotation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PostCardTest {

    @Test
    fun parseCaptionTextWithUrls_emptyString_returnsEmptyList() {
        val parts = parseCaptionTextWithUrls("")
        assertTrue(parts.isEmpty())
    }

    @Test
    fun parseCaptionTextWithUrls_plainText_returnsSingleTextPartWithoutUrl() {
        val text = "Karya seni konsep digital tanpa link."
        val parts = parseCaptionTextWithUrls(text)

        assertEquals(1, parts.size)
        assertEquals(text, parts[0].text)
        assertNull(parts[0].url)
    }

    @Test
    fun parseCaptionTextWithUrls_httpsUrl_parsedCorrectly() {
        val text = "Lihat karya di https://vex.art/room-3d dan berikan komentar!"
        val parts = parseCaptionTextWithUrls(text)

        assertEquals(3, parts.size)
        assertEquals("Lihat karya di ", parts[0].text)
        assertNull(parts[0].url)

        assertEquals("https://vex.art/room-3d", parts[1].text)
        assertEquals("https://vex.art/room-3d", parts[1].url)

        assertEquals(" dan berikan komentar!", parts[2].text)
        assertNull(parts[2].url)
    }

    @Test
    fun parseCaptionTextWithUrls_trailingPunctuation_trimmedFromUrl() {
        val text = "Kunjungi https://vex.art/expo-2026! Atau (https://vex.art/info)."
        val parts = parseCaptionTextWithUrls(text)

        assertEquals(5, parts.size)

        // Part 0: "Kunjungi "
        assertEquals("Kunjungi ", parts[0].text)
        assertNull(parts[0].url)

        // Part 1: "https://vex.art/expo-2026"
        assertEquals("https://vex.art/expo-2026", parts[1].text)
        assertEquals("https://vex.art/expo-2026", parts[1].url)

        // Part 2: "! Atau ("
        assertEquals("! Atau (", parts[2].text)
        assertNull(parts[2].url)

        // Part 3: "https://vex.art/info"
        assertEquals("https://vex.art/info", parts[3].text)
        assertEquals("https://vex.art/info", parts[3].url)

        // Part 4: ")."
        assertEquals(").", parts[4].text)
        assertNull(parts[4].url)
    }

    @Test
    fun parseCaptionTextWithUrls_wwwUrl_prependsHttpsToDestination() {
        val text = "Info pameran di www.vex.art/expo."
        val parts = parseCaptionTextWithUrls(text)

        assertEquals(3, parts.size)
        assertEquals("Info pameran di ", parts[0].text)
        assertNull(parts[0].url)

        assertEquals("www.vex.art/expo", parts[1].text)
        assertEquals("https://www.vex.art/expo", parts[1].url)

        assertEquals(".", parts[2].text)
        assertNull(parts[2].url)
    }

    @Test
    fun buildPostCaptionAnnotatedString_containsUsernameAndLinkAnnotations() {
        val fakeUriHandler = object : UriHandler {
            override fun openUri(uri: String) {}
        }

        val annotated = buildPostCaptionAnnotatedString(
            username = "@Graaph",
            description = "Artwork baru di https://vex.art/show!",
            textColor = Color.White,
            linkColor = Color.Magenta,
            uriHandler = fakeUriHandler,
        )

        // Username prefix stripped
        assertTrue(annotated.text.startsWith("Graaph Artwork baru di https://vex.art/show!"))

        // Check LinkAnnotation presence
        val linkAnnotations = annotated.getLinkAnnotations(0, annotated.length)
        assertEquals(1, linkAnnotations.size)

        val link = linkAnnotations[0].item as? LinkAnnotation.Url
        assertNotNull(link)
        assertEquals("https://vex.art/show", link?.url)
    }

    @Test
    fun buildPostCaptionAnnotatedString_withUsernameClick_hasClickableUsername() {
        val fakeUriHandler = object : UriHandler {
            override fun openUri(uri: String) {}
        }
        var usernameClicked = false

        val annotated = buildPostCaptionAnnotatedString(
            username = "StudioVex",
            description = "Deskripsi sederhana",
            textColor = Color.White,
            linkColor = Color.Magenta,
            uriHandler = fakeUriHandler,
            onUsernameClick = { usernameClicked = true },
        )

        val linkAnnotations = annotated.getLinkAnnotations(0, annotated.length)
        assertEquals(1, linkAnnotations.size)

        val clickable = linkAnnotations[0].item as? LinkAnnotation.Clickable
        assertNotNull(clickable)
        assertEquals("username", clickable?.tag)
    }

    @Test
    fun parseCaptionTextWithUrls_complexUrlWithQueryParamsAndHash_parsedProperly() {
        val text = "Kunjungi link https://vex.art/path?query=1&sort=desc#section untuk melihat!"
        val parts = parseCaptionTextWithUrls(text)

        assertEquals(3, parts.size)
        assertEquals("Kunjungi link ", parts[0].text)
        assertNull(parts[0].url)

        assertEquals("https://vex.art/path?query=1&sort=desc#section", parts[1].text)
        assertEquals("https://vex.art/path?query=1&sort=desc#section", parts[1].url)

        assertEquals(" untuk melihat!", parts[2].text)
        assertNull(parts[2].url)
    }

    @Test
    fun buildPostCaptionAnnotatedString_emptyUsername_onlyDescriptionRendered() {
        val fakeUriHandler = object : UriHandler {
            override fun openUri(uri: String) {}
        }

        val annotated = buildPostCaptionAnnotatedString(
            username = "",
            description = "Deskripsi tanpa nama",
            textColor = Color.White,
            linkColor = Color.Magenta,
            uriHandler = fakeUriHandler,
        )

        assertEquals("Deskripsi tanpa nama", annotated.text)
    }
}
