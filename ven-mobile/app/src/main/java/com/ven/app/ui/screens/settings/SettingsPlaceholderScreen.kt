package com.ven.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ven.app.R
import com.ven.app.ui.components.layout.VexTopBar
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexTheme

/**
 * Halaman sementara (placeholder) untuk Settings and Activity.
 * Memastikan navigasi menu dari ProfileScreen berjalan lancar.
 */
@Composable
fun SettingsPlaceholderScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.settings_title),
) {
    val colors = VexTheme.colors

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.bg,
        topBar = {
            VexTopBar.Back(
                title = title,
                onBackClick = onBackClick,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.bg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$title (Coming Soon)",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textMuted,
            )
        }
    }
}

@Preview(name = "Settings - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun SettingsDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        SettingsPlaceholderScreen(onBackClick = {})
    }
}

@Preview(name = "Settings - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SettingsLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        SettingsPlaceholderScreen(onBackClick = {})
    }
}
