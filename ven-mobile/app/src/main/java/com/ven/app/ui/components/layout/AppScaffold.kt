package com.ven.app.ui.components.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import com.ven.app.ui.navigation.BottomNavTab
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexTheme

/**
 * Kerangka layout utama aplikasi VEN (Virtual Exhibition).
 * Mengintegrasikan Scaffold Material 3 dengan custom [BottomNav] dan penanganan inset.
 *
 * Sesuai DESIGN.md §5.1 & §5.2:
 * - Digunakan pada: Home, Schedule, AI, Explore, Profile.
 * - Konten di-scroll, bottom nav tetap diam.
 * - Menggunakan `innerPadding` agar konten tidak tertutup bottom nav.
 */
@Composable
fun AppScaffold(
    currentTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    showBottomNav: Boolean = true,
    profilePainter: Painter? = null,
    profileInitials: String = "V",
    content: @Composable (PaddingValues) -> Unit,
) {
    val colors = VexTheme.colors

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.bg,
        topBar = topBar,
        bottomBar = {
            if (showBottomNav) {
                BottomNav(
                    selectedTab = currentTab,
                    onTabSelected = onTabSelected,
                    profilePainter = profilePainter,
                    profileInitials = profileInitials,
                )
            }
        },
    ) { innerPadding ->
        content(innerPadding)
    }
}

// ---------------------------------------------------------
// Previews untuk 2 Mode
// ---------------------------------------------------------

@Preview(name = "AppScaffold - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun AppScaffoldDarkPreview() {
    var selectedTab by remember { mutableStateOf(BottomNavTab.Home) }

    VexTheme(mode = ThemeMode.Dark) {
        AppScaffold(
            currentTab = selectedTab,
            onTabSelected = { selectedTab = it },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Current Screen: ${selectedTab.title}",
                    color = VexTheme.colors.text,
                )
            }
        }
    }
}

@Preview(name = "AppScaffold - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AppScaffoldLightPreview() {
    var selectedTab by remember { mutableStateOf(BottomNavTab.Home) }

    VexTheme(mode = ThemeMode.Light) {
        AppScaffold(
            currentTab = selectedTab,
            onTabSelected = { selectedTab = it },
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Current Screen: ${selectedTab.title}",
                    color = VexTheme.colors.text,
                )
            }
        }
    }
}
