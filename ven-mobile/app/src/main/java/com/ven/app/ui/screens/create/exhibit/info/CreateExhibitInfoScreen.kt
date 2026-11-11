package com.ven.app.ui.screens.create.exhibit.info

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.components.layout.VexTopBar
import com.ven.app.ui.components.ui.CreateContentType
import com.ven.app.ui.components.ui.SegmentedPill
import com.ven.app.ui.components.ui.VexButton
import com.ven.app.ui.components.ui.VexButtonVariant
import com.ven.app.ui.components.ui.VexTextArea
import com.ven.app.ui.components.ui.VexTextField
import com.ven.app.ui.screens.create.exhibit.CreateExhibitUiState
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Layar INFORMASI PAMERAN (Langkah 1 Pembuatan Pameran).
 * Tampil pertama kali ketika pengguna memilih opsi "Pameran".
 * Mengumpulkan:
 * - Cover Banner (16:9)
 * - Judul Pameran (Maks 100 karakter, validasi wajib)
 * - Deskripsi Pameran
 * Data disimpan sementara di ViewModel sebelum lanjut ke pemilihan template.
 */
@Composable
fun CreateExhibitInfoScreen(
    state: CreateExhibitUiState,
    onCloseClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onBannerSelected: (android.net.Uri?) -> Unit,
    onNextClick: () -> Unit,
    onNavigateToPost: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        onBannerSelected(uri)
    }

    val bannerBitmap = remember(state.bannerUri) {
        state.bannerUri?.let { uri ->
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.asImageBitmap()
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // TopBar: Close icon + "Exhibit Information"
            VexTopBar.Create(
                title = stringResource(R.string.create_exhibit_info_title),
                onCloseOrBackClick = onCloseClick,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = VexSpace.s6)
                    .padding(top = VexSpace.s4, bottom = 100.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(VexSpace.s5),
            ) {
                // Selected Template Info Badge
                state.selectedTemplate?.let { tmpl ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(VexRadius.md))
                            .background(colors.surfaceInput)
                            .padding(horizontal = VexSpace.s4, vertical = VexSpace.s3),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Template: ${stringResource(tmpl.nameRes)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = colors.text,
                        )
                        Text(
                            text = "${tmpl.capacity} Post",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                            ),
                            color = colors.primaryText,
                        )
                    }
                }

                // Section: Cover Banner (16:9)
                Column(verticalArrangement = Arrangement.spacedBy(VexSpace.s2)) {
                    Text(
                        text = stringResource(R.string.create_exhibit_cover_banner),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
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
                                color = if (state.bannerError != null) colors.danger else colors.border,
                                shape = RoundedCornerShape(VexRadius.md),
                            )
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (bannerBitmap != null) {
                            Image(
                                bitmap = bannerBitmap,
                                contentDescription = stringResource(R.string.create_exhibit_cover_banner),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.create_exhibit_cover_placeholder),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(VexSpace.s4),
                            )
                        }
                    }

                    if (state.bannerError != null) {
                        Text(
                            text = state.bannerError,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.danger,
                        )
                    }
                }

                // Section: Judul Pameran (Maks 100 karakter)
                Column(verticalArrangement = Arrangement.spacedBy(VexSpace.s1)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.create_exhibit_name_label),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = colors.text,
                        )
                        Text(
                            text = "${state.title.length}/100",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.title.length > 100) colors.danger else colors.textMuted,
                        )
                    }

                    VexTextField(
                        value = state.title,
                        onValueChange = onTitleChange,
                        placeholder = stringResource(R.string.create_exhibit_name_placeholder),
                        isError = state.titleError != null,
                        errorMessage = state.titleError.orEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                // Section: Deskripsi Pameran
                Column(verticalArrangement = Arrangement.spacedBy(VexSpace.s1)) {
                    Text(
                        text = stringResource(R.string.create_exhibit_desc_label),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = colors.text,
                    )

                    VexTextArea(
                        value = state.description,
                        onValueChange = onDescriptionChange,
                        placeholder = stringResource(R.string.create_exhibit_desc_placeholder),
                        minHeight = 120.dp,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(modifier = Modifier.height(VexSpace.s2))

                // Tombol Lanjut ke Pilih Template
                VexButton(
                    text = stringResource(R.string.create_exhibit_next_step),
                    onClick = onNextClick,
                    variant = VexButtonVariant.Primary,
                    loading = state.isUploadingBanner,
                    enabled = !state.isUploadingBanner,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // SegmentedPill melayang di bawah tengah ("Post | Exhibit", Exhibit aktif)
        SegmentedPill(
            selectedType = CreateContentType.Exhibit,
            onTypeSelected = { type ->
                if (type == CreateContentType.Post) {
                    onNavigateToPost()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = VexSpace.s5),
        )
    }
}
