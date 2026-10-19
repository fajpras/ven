package com.ven.app.ui.screens.create.exhibit.editor.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.components.ui.VexBottomSheet
import com.ven.app.ui.components.ui.VexButton
import com.ven.app.ui.components.ui.VexTextArea
import com.ven.app.ui.components.ui.VexTextField
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Bottom Sheet 1: Detail Metadata Pameran (Aa).
 * Mengatur Cover Banner (16:9), Judul Pameran, dan Deskripsi Pameran.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExhibitDetailsBottomSheet(
    initialTitle: String,
    initialDescription: String,
    coverBannerUri: String?,
    onSave: (title: String, description: String, bannerUri: String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var bannerUri by remember { mutableStateOf(coverBannerUri) }

    VexBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = VexSpace.s6)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(bottom = VexSpace.s6),
            verticalArrangement = Arrangement.spacedBy(VexSpace.s4),
        ) {
            // Header: Judul Sheet + Tombol Tutup
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.create_exhibit_tool_details),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = colors.text,
                )

                IconButton(onClick = onDismiss) {
                    VexIcon(
                        id = VexIcons.Close,
                        contentDescription = stringResource(R.string.create_post_close_desc),
                        size = VexSize.icon,
                        tint = colors.textMuted,
                    )
                }
            }

            // Cover Banner 16:9
            Column(
                verticalArrangement = Arrangement.spacedBy(VexSpace.s2),
            ) {
                Text(
                    text = stringResource(R.string.create_exhibit_cover_banner),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                    color = colors.text,
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(VexRadius.md))
                        .background(colors.surfaceInput)
                        .border(
                            width = 1.dp,
                            color = colors.border,
                            shape = RoundedCornerShape(VexRadius.md),
                        )
                        .clickable(
                            role = Role.Button,
                            onClick = {
                                // Simulasi memilih cover banner 16:9
                                bannerUri = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=800"
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (bannerUri == null) {
                            stringResource(R.string.create_exhibit_cover_placeholder)
                        } else {
                            "Banner Cover Attached ✓"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (bannerUri == null) colors.textMuted else colors.primaryText,
                    )
                }
            }

            // Field Judul Pameran
            Column(
                verticalArrangement = Arrangement.spacedBy(VexSpace.s2),
            ) {
                Text(
                    text = stringResource(R.string.create_exhibit_name_label),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                    color = colors.text,
                )

                VexTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = stringResource(R.string.create_exhibit_name_placeholder),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Field Deskripsi Pameran
            Column(
                verticalArrangement = Arrangement.spacedBy(VexSpace.s2),
            ) {
                Text(
                    text = stringResource(R.string.create_exhibit_desc_label),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Medium),
                    color = colors.text,
                )

                VexTextArea(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = stringResource(R.string.create_exhibit_desc_placeholder),
                    modifier = Modifier.fillMaxWidth(),
                    minHeight = 120.dp,
                )
            }

            // Tombol Simpan
            VexButton(
                text = stringResource(R.string.create_exhibit_save_details),
                onClick = {
                    onSave(title, description, bannerUri)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = VexSpace.s2),
            )
        }
    }
}

// -------------------------------------------------------------
// Previews
// -------------------------------------------------------------

@androidx.compose.ui.tooling.preview.Preview(name = "Details Sheet - Dark", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun ExhibitDetailsSheetDarkPreview() {
    com.ven.app.ui.theme.VexTheme(mode = com.ven.app.ui.theme.ThemeMode.Dark) {
        ExhibitDetailsBottomSheet(
            initialTitle = "My Virtual Solo Exhibition",
            initialDescription = "A futuristic gallery space showcasing generative artworks.",
            coverBannerUri = null,
            onSave = { _, _, _ -> },
            onDismiss = {},
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Details Sheet - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ExhibitDetailsSheetLightPreview() {
    com.ven.app.ui.theme.VexTheme(mode = com.ven.app.ui.theme.ThemeMode.Light) {
        ExhibitDetailsBottomSheet(
            initialTitle = "My Virtual Solo Exhibition",
            initialDescription = "A futuristic gallery space showcasing generative artworks.",
            coverBannerUri = null,
            onSave = { _, _, _ -> },
            onDismiss = {},
        )
    }
}
