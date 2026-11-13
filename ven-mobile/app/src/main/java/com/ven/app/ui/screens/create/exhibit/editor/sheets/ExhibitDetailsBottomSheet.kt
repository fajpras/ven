package com.ven.app.ui.screens.create.exhibit.editor.sheets

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ven.app.R
import com.ven.app.ui.components.ui.VexBottomSheet
import com.ven.app.ui.components.ui.VexButton
import com.ven.app.ui.components.ui.VexTextArea
import com.ven.app.ui.components.ui.VexTextField
import com.ven.app.ui.components.ui.rememberAsyncImageBitmap
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Helper untuk memuat ImageBitmap dari URI galeri lokal (content:// / file://)
 * atau URL jarak jauh (http:// / https://).
 */
@Composable
private fun rememberBannerBitmap(uriString: String?): ImageBitmap? {
    val context = LocalContext.current
    val isNetwork = uriString?.startsWith("http://") == true || uriString?.startsWith("https://") == true
    val networkBmp = rememberAsyncImageBitmap(if (isNetwork) uriString else null)
    var localBmp by remember(uriString) { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(uriString) {
        if (uriString.isNullOrBlank() || isNetwork) {
            localBmp = null
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            try {
                val parsedUri = Uri.parse(uriString)
                val inputStream = context.contentResolver.openInputStream(parsedUri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                localBmp = bitmap?.asImageBitmap()
            } catch (e: Exception) {
                localBmp = null
            }
        }
    }

    return if (isNetwork) networkBmp else localBmp
}

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

    var titleError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }

    val bannerBitmap = rememberBannerBitmap(bannerUri)

    // Launcher galeri untuk memilih foto sampul (16:9)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        if (uri != null) {
            bannerUri = uri.toString()
        }
    }

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

            // Cover Banner 16:9 dengan pemilih galeri asli dan pratinjau gambar
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
                            color = if (bannerUri != null) colors.primary.copy(alpha = 0.5f) else colors.border,
                            shape = RoundedCornerShape(VexRadius.md),
                        )
                        .clickable(
                            role = Role.Button,
                            onClick = {
                                galleryLauncher.launch("image/*")
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (bannerBitmap != null) {
                        Image(
                            bitmap = bannerBitmap,
                            contentDescription = stringResource(R.string.create_exhibit_cover_banner),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )

                        // Badge overlay tombol ubah foto
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(VexSpace.s3)
                                .clip(RoundedCornerShape(VexRadius.xs))
                                .background(Color.Black.copy(alpha = 0.65f))
                                .padding(horizontal = VexSpace.s3, vertical = VexSpace.s1),
                        ) {
                            Text(
                                text = "Ganti Foto",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 11.sp,
                                ),
                                color = Color.White,
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(VexSpace.s4),
                        ) {
                            VexIcon(
                                id = VexIcons.Add,
                                contentDescription = null,
                                size = 28.dp,
                                tint = colors.primary,
                            )
                            Spacer(modifier = Modifier.height(VexSpace.s2))
                            Text(
                                text = stringResource(R.string.create_exhibit_cover_placeholder),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = colors.text,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Ketuk untuk memilih foto sampul (16:9)",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted,
                            )
                        }
                    }
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
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = null
                    },
                    placeholder = stringResource(R.string.create_exhibit_name_placeholder),
                    isError = titleError != null,
                    errorMessage = titleError.orEmpty(),
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
                    onValueChange = {
                        description = it
                        if (it.isNotBlank()) descriptionError = null
                    },
                    placeholder = stringResource(R.string.create_exhibit_desc_placeholder),
                    isError = descriptionError != null,
                    errorMessage = descriptionError.orEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    minHeight = 120.dp,
                )
            }

            // Tombol Simpan
            VexButton(
                text = stringResource(R.string.create_exhibit_save_details),
                onClick = {
                    val trimmedTitle = title.trim()
                    val trimmedDesc = description.trim()

                    var hasError = false
                    if (trimmedTitle.isEmpty()) {
                        titleError = "Judul pameran tidak boleh kosong"
                        hasError = true
                    }
                    if (trimmedDesc.isEmpty()) {
                        descriptionError = "Deskripsi pameran tidak boleh kosong"
                        hasError = true
                    }

                    if (!hasError) {
                        onSave(trimmedTitle, trimmedDesc, bannerUri)
                        onDismiss()
                    }
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
