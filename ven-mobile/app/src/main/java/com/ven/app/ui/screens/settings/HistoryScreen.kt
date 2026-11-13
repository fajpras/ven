package com.ven.app.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.components.feature.HistoryEntry
import com.ven.app.ui.components.feature.HistoryListItem
import com.ven.app.ui.components.layout.VexTopBar
import com.ven.app.ui.components.ui.VexBottomSheet
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Halaman History.
 * Sesuai wireframe `Wireframe - History.png` dan `History-1.png`:
 * - Menampilkan daftar kartu [HistoryListItem].
 * - Klik item menampilkan modal [VexBottomSheet] berisi banner poster besar dan detail status request.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    entries: List<HistoryEntry>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors
    var activeEntry by remember { mutableStateOf<HistoryEntry?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.bg,
        topBar = {
            VexTopBar.Back(
                title = stringResource(R.string.settings_history_title),
                onBackClick = onBackClick,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.bg),
        ) {
            items(
                items = entries,
                key = { it.id },
            ) { entry ->
                HistoryListItem(
                    entry = entry,
                    onClick = { activeEntry = entry },
                )
            }
        }

        // Bottom Sheet Detail History
        activeEntry?.let { entry ->
            VexBottomSheet(
                onDismissRequest = { activeEntry = null },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = VexSpace.s6)
                        .padding(bottom = VexSpace.s8)
                        .navigationBarsPadding(),
                ) {
                    // Banner Besar 16:9
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(VexRadius.md))
                            .background(colors.surfaceInput),
                    ) {
                        Image(
                            painter = painterResource(id = entry.bannerRes),
                            contentDescription = entry.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }

                    Spacer(modifier = Modifier.height(VexSpace.s5))

                    // Deskripsi Status Request Terjadwal
                    val annotatedText = buildAnnotatedString {
                        append("Your request which is scheduled in ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = colors.text)) {
                            append(entry.fullDateFormatted)
                        }
                        append(" at ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = colors.text)) {
                            append(entry.timeText)
                        }
                        append(" has been finished.")
                    }

                    Text(
                        text = annotatedText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.text,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

@Preview(name = "HistoryScreen - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun HistoryScreenDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        HistoryScreen(
            entries = dummyHistoryEntries,
            onBackClick = {},
        )
    }
}

@Preview(name = "HistoryScreen - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun HistoryScreenLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        HistoryScreen(
            entries = dummyHistoryEntries,
            onBackClick = {},
        )
    }
}
