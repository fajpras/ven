package com.ven.app.ui.screens.create.post

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ven.app.R
import com.ven.app.ui.components.feature.MediaGridItem
import com.ven.app.ui.components.layout.VexTopBar
import com.ven.app.ui.components.ui.CreateContentType
import com.ven.app.ui.components.ui.SegmentedPill
import com.ven.app.ui.components.ui.VexDropdown
import com.ven.app.ui.components.ui.VexTextArea
import com.ven.app.ui.components.ui.VexTextField
import com.ven.app.ui.components.ui.VexTextFieldVariant
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexAspect
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Screen utama untuk pembuatan karya baru (Create Post).
 * Mengimplementasikan alur 2 langkah sesuai spesifikasi DESIGN.md §8.13:
 * - Langkah 1: Media Picker (pratinjau besar terpilih + grid 3 kolom + SegmentedPill melayang).
 * - Langkah 2: Details Form (pratinjau strip + formulir Title, Category, Product Link, Description).
 */
@Composable
fun CreatePostScreen(
    onCloseClick: () -> Unit,
    onPostSuccess: () -> Unit,
    onNavigateToExhibit: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CreatePostViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                CreatePostEvent.PostSuccess -> onPostSuccess()
                CreatePostEvent.SwitchToExhibit -> onNavigateToExhibit()
            }
        }
    }

    BackHandler {
        if (!viewModel.onBackClick()) {
            onCloseClick()
        }
    }

    CreatePostContent(
        state = state,
        onCloseClick = onCloseClick,
        onNextClick = viewModel::onNextClick,
        onBackClick = { viewModel.onBackClick() },
        onPostClick = viewModel::submitPost,
        onMediaSelected = viewModel::onMediaSelected,
        onContentTypeSelected = viewModel::onContentTypeSelected,
        onTitleChange = viewModel::onTitleChange,
        onCategoryChange = viewModel::onCategoryChange,
        onProductLinkChange = viewModel::onProductLinkChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        modifier = modifier,
    )
}

@Composable
fun CreatePostContent(
    state: CreatePostUiState,
    onCloseClick: () -> Unit,
    onNextClick: () -> Unit,
    onBackClick: () -> Unit,
    onPostClick: () -> Unit,
    onMediaSelected: (MediaGridItem) -> Unit,
    onContentTypeSelected: (CreateContentType) -> Unit,
    onTitleChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onProductLinkChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        AnimatedContent(
            targetState = state.step,
            transitionSpec = {
                if (targetState == CreatePostStep.Details) {
                    slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
                } else {
                    slideInHorizontally { width -> -width } togetherWith slideOutHorizontally { width -> width }
                }
            },
            label = "CreatePostStepTransition",
        ) { step ->
            when (step) {
                CreatePostStep.MediaPicker -> {
                    CreatePostMediaPickerStep(
                        state = state,
                        onCloseClick = onCloseClick,
                        onNextClick = onNextClick,
                        onMediaSelected = onMediaSelected,
                        onContentTypeSelected = onContentTypeSelected,
                    )
                }
                CreatePostStep.Details -> {
                    CreatePostDetailsStep(
                        state = state,
                        onBackClick = onBackClick,
                        onPostClick = onPostClick,
                        onTitleChange = onTitleChange,
                        onCategoryChange = onCategoryChange,
                        onProductLinkChange = onProductLinkChange,
                        onDescriptionChange = onDescriptionChange,
                    )
                }
            }
        }
    }
}

/**
 * Langkah 1: Media Picker (Tampilan Image 1 / wireframe node 4256-704)
 */
@Composable
private fun CreatePostMediaPickerStep(
    state: CreatePostUiState,
    onCloseClick: () -> Unit,
    onNextClick: () -> Unit,
    onMediaSelected: (MediaGridItem) -> Unit,
    onContentTypeSelected: (CreateContentType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // TopBar: X + "New Post" di kiri, "Next" warna ungu di kanan
            VexTopBar.Create(
                title = stringResource(R.string.create_post_title),
                actionText = stringResource(R.string.create_post_next),
                onActionClick = onNextClick,
                onCloseOrBackClick = onCloseClick,
                isBack = false,
                actionEnabled = true,
            )

            // Grid vertikal berisi Header (Pratinjau Besar + Label "Recents") + 3-kolom galeri
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                contentPadding = PaddingValues(bottom = 88.dp),
            ) {
                // Header 1: Pratinjau media terpilih ukuran besar
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.85f)
                            .background(colors.surfaceInput),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (state.selectedMedia.imageRes != null) {
                            Image(
                                painter = painterResource(id = state.selectedMedia.imageRes),
                                contentDescription = state.selectedMedia.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Text(
                                text = state.selectedMedia.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.textMuted,
                            )
                        }
                    }
                }

                // Header 2: Label Seksi "Recents"
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = VexSpace.s6,
                                end = VexSpace.s6,
                                top = VexSpace.s4,
                                bottom = VexSpace.s3,
                            ),
                    ) {
                        Text(
                            text = stringResource(R.string.create_post_recents),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = colors.text,
                        )
                    }
                }

                // Item Galeri Media (3 kolom)
                items(
                    items = state.mediaItems,
                    key = { it.id },
                ) { item ->
                    val isSelected = item.id == state.selectedMedia.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(VexAspect.poster)
                            .background(colors.surfaceInput)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = colors.text.copy(alpha = 0.2f)),
                                role = Role.Button,
                                onClick = { onMediaSelected(item) },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (item.imageRes != null) {
                            Image(
                                painter = painterResource(id = item.imageRes),
                                contentDescription = item.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted,
                            )
                        }

                        // Overlay sorotan jika terpilih
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(colors.text.copy(alpha = 0.15f)),
                            )
                        }
                    }
                }
            }
        }

        // SegmentedPill "Post | Exhibit" melayang di bagian bawah tengah
        SegmentedPill(
            selectedType = state.selectedContentType,
            onTypeSelected = onContentTypeSelected,
            postLabel = stringResource(R.string.create_post_tab_post),
            exhibitLabel = stringResource(R.string.create_post_tab_exhibit),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = VexSpace.s4),
        )
    }
}

/**
 * Langkah 2: Details Form (Tampilan Image 2 / wireframe node 4256-861)
 */
@Composable
private fun CreatePostDetailsStep(
    state: CreatePostUiState,
    onBackClick: () -> Unit,
    onPostClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onProductLinkChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        // TopBar: Panah kembali + "New Post" di kiri, "Post" di kanan (aktif jika valid)
        VexTopBar.Create(
            title = stringResource(R.string.create_post_title),
            actionText = stringResource(R.string.create_post_action),
            onActionClick = onPostClick,
            onCloseOrBackClick = onBackClick,
            isBack = true,
            actionEnabled = state.isPostEnabled,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .imePadding(),
        ) {
            // Pita pratinjau karya terpilih (latar abu terang / surfaceInput, gambar centered)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(colors.surfaceInput),
                contentAlignment = Alignment.Center,
            ) {
                if (state.selectedMedia.imageRes != null) {
                    Image(
                        painter = painterResource(id = state.selectedMedia.imageRes),
                        contentDescription = state.title,
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(vertical = VexSpace.s3),
                        contentScale = ContentScale.Fit,
                    )
                }
            }

            // Formulir Metadata Postingan
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = VexSpace.s6, vertical = VexSpace.s5),
                verticalArrangement = Arrangement.spacedBy(VexSpace.s4),
            ) {
                // 1. Title
                VexTextField(
                    value = state.title,
                    onValueChange = onTitleChange,
                    variant = VexTextFieldVariant.Boxed,
                    label = stringResource(R.string.create_post_title_label),
                    placeholder = stringResource(R.string.create_post_title_placeholder),
                )

                // 2. Category
                VexDropdown(
                    label = stringResource(R.string.create_post_category_label),
                    selectedOption = state.category,
                    options = state.availableCategories,
                    onOptionSelected = onCategoryChange,
                )

                // 3. Product Link (dengan header baris Product Link + label "optional")
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = VexSpace.s2),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.create_post_product_link_label),
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.text,
                        )

                        Text(
                            text = stringResource(R.string.create_post_optional),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted,
                        )
                    }

                    VexTextField(
                        value = state.productLink,
                        onValueChange = onProductLinkChange,
                        variant = VexTextFieldVariant.Boxed,
                        placeholder = stringResource(R.string.create_post_product_link_placeholder),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    )
                }

                // 4. Description (VexTextArea multiline)
                VexTextArea(
                    value = state.description,
                    onValueChange = onDescriptionChange,
                    label = stringResource(R.string.create_post_description_label),
                    placeholder = stringResource(R.string.create_post_description_placeholder),
                    minHeight = 175.dp,
                )

                Spacer(modifier = Modifier.height(VexSpace.s8))
            }
        }
    }
}

// -------------------------------------------------------------
// Previews
// -------------------------------------------------------------

@Preview(name = "Step 1: Media Picker - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CreatePostMediaPickerLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        CreatePostContent(
            state = CreatePostUiState(step = CreatePostStep.MediaPicker),
            onCloseClick = {},
            onNextClick = {},
            onBackClick = {},
            onPostClick = {},
            onMediaSelected = {},
            onContentTypeSelected = {},
            onTitleChange = {},
            onCategoryChange = {},
            onProductLinkChange = {},
            onDescriptionChange = {},
        )
    }
}

@Preview(name = "Step 1: Media Picker - Dark", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun CreatePostMediaPickerDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        CreatePostContent(
            state = CreatePostUiState(step = CreatePostStep.MediaPicker),
            onCloseClick = {},
            onNextClick = {},
            onBackClick = {},
            onPostClick = {},
            onMediaSelected = {},
            onContentTypeSelected = {},
            onTitleChange = {},
            onCategoryChange = {},
            onProductLinkChange = {},
            onDescriptionChange = {},
        )
    }
}

@Preview(name = "Step 2: Details - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CreatePostDetailsLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        CreatePostContent(
            state = CreatePostUiState(step = CreatePostStep.Details),
            onCloseClick = {},
            onNextClick = {},
            onBackClick = {},
            onPostClick = {},
            onMediaSelected = {},
            onContentTypeSelected = {},
            onTitleChange = {},
            onCategoryChange = {},
            onProductLinkChange = {},
            onDescriptionChange = {},
        )
    }
}

@Preview(name = "Step 2: Details - Dark", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun CreatePostDetailsDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        CreatePostContent(
            state = CreatePostUiState(step = CreatePostStep.Details),
            onCloseClick = {},
            onNextClick = {},
            onBackClick = {},
            onPostClick = {},
            onMediaSelected = {},
            onContentTypeSelected = {},
            onTitleChange = {},
            onCategoryChange = {},
            onProductLinkChange = {},
            onDescriptionChange = {},
        )
    }
}
