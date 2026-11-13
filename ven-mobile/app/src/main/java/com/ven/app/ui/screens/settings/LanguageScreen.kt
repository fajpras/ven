package com.ven.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ven.app.R
import com.ven.app.ui.components.layout.VexTopBar
import com.ven.app.ui.components.ui.VexCheckRow
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexTheme

/**
 * Halaman Pengaturan Bahasa (Language).
 * Sesuai wireframe `Wireframe - Setting Password-2.png` dan DESIGN.md §8.8:
 * - Opsi "English" & "Bahasa Indonesia" dengan tanda centang [VexCheckRow].
 */
@Composable
fun LanguageScreen(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors
    var selectedLang by remember { mutableStateOf(currentLanguage) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.bg,
        topBar = {
            VexTopBar.Back(
                title = stringResource(R.string.settings_item_language),
                onBackClick = onBackClick,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.bg),
        ) {
            VexCheckRow(
                label = stringResource(R.string.settings_language_english),
                selected = selectedLang == "English",
                onClick = {
                    selectedLang = "English"
                    onLanguageSelected("English")
                },
            )

            VexCheckRow(
                label = stringResource(R.string.settings_language_indonesia),
                selected = selectedLang == "Bahasa Indonesia",
                onClick = {
                    selectedLang = "Bahasa Indonesia"
                    onLanguageSelected("Bahasa Indonesia")
                },
            )
        }
    }
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

@Preview(name = "LanguageScreen - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun LanguageScreenDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        LanguageScreen(
            currentLanguage = "English",
            onLanguageSelected = {},
            onBackClick = {},
        )
    }
}

@Preview(name = "LanguageScreen - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun LanguageScreenLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        LanguageScreen(
            currentLanguage = "English",
            onLanguageSelected = {},
            onBackClick = {},
        )
    }
}
