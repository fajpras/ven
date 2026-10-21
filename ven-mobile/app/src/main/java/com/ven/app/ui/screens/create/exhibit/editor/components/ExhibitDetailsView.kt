package com.ven.app.ui.screens.create.exhibit.editor.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ven.app.R
import com.ven.app.ui.components.ui.VexTextArea
import com.ven.app.ui.components.ui.VexTextField
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Layar Tampilan Pengaturan Detail Metadata Pameran (Wireframe Create Exhibition - Frame 2).
 *
 * Menampilkan:
 * 1. Top Bar: "Details" di kiri dan Tombol Centang (✓) di kanan untuk konfirmasi & simpan.
 * 2. Pratinjau Thumbnail / Banner 16:9 (menampilkan poster PBL Expo 2026 secara default sesuai wireframe).
 * 3. Bidang input Judul ("Title").
 * 4. Bidang input Deskripsi ("Description") dengan placeholder "type your description...".
 */
@Composable
fun ExhibitDetailsView(
    initialTitle: String,
    initialDescription: String,
    coverBannerUri: String?,
    onSave: (title: String, description: String, bannerUri: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors
    var title by remember { mutableStateOf(if (initialTitle.isBlank()) "My Exhibition 1" else initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    val bannerOptions = remember { listOf(R.drawable.poster_2, R.drawable.poster_1, R.drawable.poster_3) }
    var selectedBannerIndex by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        // Top Bar: "Details" (kiri) & Checkmark ✓ (kanan)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = VexSpace.s6, vertical = VexSpace.s3),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.create_exhibit_details_header),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                ),
                color = colors.text,
            )

            // Tombol Centang (✓) Simpan & Selesai
            IconButton(
                onClick = { onSave(title, description, coverBannerUri) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceInput),
            ) {
                VexIcon(
                    id = VexIcons.Check,
                    contentDescription = stringResource(R.string.create_exhibit_save_details),
                    size = VexSize.icon,
                    tint = colors.text,
                )
            }
        }

        // Konten Formulir Scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = VexSpace.s6, vertical = VexSpace.s2),
            verticalArrangement = Arrangement.spacedBy(VexSpace.s5),
        ) {
            // Bagian 1: Thumbnail (16:9 Banner)
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.create_exhibit_thumbnail_label),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                    ),
                    color = colors.text,
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(VexRadius.md))
                        .background(colors.surfaceInput)
                        .border(width = 1.dp, color = colors.border, shape = RoundedCornerShape(VexRadius.md))
                        .clickable(
                            role = Role.Button,
                            onClick = {
                                selectedBannerIndex = (selectedBannerIndex + 1) % bannerOptions.size
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(id = bannerOptions[selectedBannerIndex]),
                        contentDescription = stringResource(R.string.create_exhibit_cover_banner),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }

            // Bagian 2: Title
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.create_exhibit_name_label),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                    ),
                    color = colors.text,
                )

                VexTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = stringResource(R.string.create_exhibit_name_placeholder),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Bagian 3: Description
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.create_exhibit_desc_label),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                    ),
                    color = colors.text,
                )

                VexTextArea(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = stringResource(R.string.create_exhibit_desc_placeholder),
                    minHeight = 180.dp,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(modifier = Modifier.height(VexSpace.s6))
        }
    }
}

// -------------------------------------------------------------
// Previews
// -------------------------------------------------------------

@Preview(name = "Exhibit Details View - Light")
@Composable
private fun ExhibitDetailsViewLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        ExhibitDetailsView(
            initialTitle = "My Exhibition 1",
            initialDescription = "",
            coverBannerUri = null,
            onSave = { _, _, _ -> },
        )
    }
}

@Preview(name = "Exhibit Details View - Dark")
@Composable
private fun ExhibitDetailsViewDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        ExhibitDetailsView(
            initialTitle = "My Exhibition 1",
            initialDescription = "",
            coverBannerUri = null,
            onSave = { _, _, _ -> },
        )
    }
}
