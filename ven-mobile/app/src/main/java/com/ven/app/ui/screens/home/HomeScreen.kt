package com.ven.app.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.components.feature.CarouselExhibitions
import com.ven.app.ui.components.feature.EventBanner
import com.ven.app.ui.components.feature.PostCard
import com.ven.app.ui.components.layout.AppScaffold
import com.ven.app.ui.components.layout.VexTopBar
import com.ven.app.ui.navigation.BottomNavTab
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Halaman Utama (Home Screen) Aplikasi VEN (Virtual Exhibition).
 *
 * Sesuai spesifikasi DESIGN.md §8.6 (Dark Mode - Figma Wireframe Home.png):
 * 1. TopBar ([VexTopBar.Home]):
 *    - Tombol "+" di kiri (membuka Create Flow).
 *    - Logo VEN di kanan.
 *    - Latar menyatu dengan kanvas (#1F1F1F), tanpa garis pembatas bawah.
 * 2. Seksi "Coming Soon":
 *    - Judul: "Coming Soon" (20sp SemiBold, padding horizontal 24dp).
 *    - Carousel horizontal poster pameran ([CarouselExhibitions] + [EventBanner]) dengan snap behavior, rasio 16:9, radius 8–12dp.
 *    - Garis pembatas 1dp warna border (#313131).
 * 3. Feed Karya ([PostCard]):
 *    - Header: @username di kiri, ikon kalender (Request Meeting) & ikon play (Virtual 3D Room) di kanan.
 *    - Media post: Gambar selebar layar (edge-to-edge), 0dp radius.
 * 4. Bottom Navigation Bar ([BottomNavTab.Home] aktif).
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onTabSelected: (BottomNavTab) -> Unit,
    onCreateClick: () -> Unit,
    onRequestMeetingClick: (postId: String) -> Unit,
    onView3dClick: (pameranId: String?) -> Unit,
    onBannerClick: (eventId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    AppScaffold(
        currentTab = BottomNavTab.Home,
        onTabSelected = onTabSelected,
        modifier = modifier,
        topBar = {
            VexTopBar.Home(
                onCreateClick = onCreateClick,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = VexSpace.s6),
        ) {
            // ---------------------------------------------------------
            // Seksi 1: Coming Soon Carousel
            // ---------------------------------------------------------
            if (uiState.comingSoonEvents.isNotEmpty()) {
                item {
                    CarouselExhibitions(
                        title = stringResource(R.string.home_coming_soon),
                    ) {
                        items(
                            items = uiState.comingSoonEvents,
                            key = { it.id },
                        ) { event ->
                            EventBanner(
                                title = event.title,
                                dateText = event.dateText,
                                imageUrl = event.imageUrl,
                                onClick = { onBannerClick(event.id) },
                            )
                        }
                    }
                }
            }

            // ---------------------------------------------------------
            // Seksi 2: Feed Post Karya
            // ---------------------------------------------------------
            if (uiState.posts.isNotEmpty()) {
                items(
                    items = uiState.posts,
                    key = { it.id },
                ) { post ->
                    PostCard(
                        username = post.username,
                        description = post.description,
                        imageUrl = post.imageUrl,
                        aspectRatio = post.aspectRatio,
                        onRequestMeetingClick = { onRequestMeetingClick(post.id) },
                        onView3dClick = { onView3dClick(post.pameranId) },
                    )
                }
            } else if (!uiState.isLoading) {
                // Empty State Feed
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.home_empty_feed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted,
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// Previews untuk Dark Mode & Light Mode
// ---------------------------------------------------------

val SampleHomeUiState = HomeUiState(
    comingSoonEvents = listOf(
        ComingSoonEvent(
            id = "1",
            title = "Open Source Competition",
            dateText = "24th Sep 2026",
        ),
        ComingSoonEvent(
            id = "2",
            title = "PBL EXPO 2026",
            dateText = "1st Oct 2026",
        ),
    ),
    posts = listOf(
        PostItem(
            id = "p1",
            username = "Graaph",
            title = "Digital Painting Concept",
            pameranId = "ex1",
            description = "Concept art eksplorasi arsitektur cyberpunk dan pencahayaan neon untuk kompetisi open source. Kunjungi showroom 3D di https://vex.art/exhibit/digital-concept untuk melihat detail tekstur dan aset interaktif! Jangan lupa tinggalkan feedback Anda sebelum expo dimulai.",
        ),
        PostItem(
            id = "p2",
            username = "StudioVex",
            title = "3D Environment Design",
            pameranId = "ex2",
            description = "Desain lingkungan 3D untuk PBL EXPO 2026. Info lebih lanjut kunjungi https://vex.art/expo-2026",
        ),
    ),
)

@Preview(name = "Home Screen - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun HomeScreenDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        HomeScreen(
            uiState = SampleHomeUiState,
            onTabSelected = {},
            onCreateClick = {},
            onRequestMeetingClick = {},
            onView3dClick = {},
            onBannerClick = {},
        )
    }
}

@Preview(name = "Home Screen - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun HomeScreenLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        HomeScreen(
            uiState = SampleHomeUiState,
            onTabSelected = {},
            onCreateClick = {},
            onRequestMeetingClick = {},
            onView3dClick = {},
            onBannerClick = {},
        )
    }
}
