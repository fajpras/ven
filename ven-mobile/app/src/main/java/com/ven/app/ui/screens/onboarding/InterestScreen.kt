package com.ven.app.ui.screens.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ven.app.R
import com.ven.app.data.api.models.KategoriModel
import com.ven.app.ui.components.feature.InterestCategoryChips
import com.ven.app.ui.components.layout.AuthLayout
import com.ven.app.ui.components.ui.VexButton
import com.ven.app.ui.components.ui.VexButtonVariant
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Penghubung antara [InterestViewModel] dan [InterestScreen] (stateless).
 */
@Composable
fun InterestRoute(
    onFinished: () -> Unit,
    viewModel: InterestViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                InterestEvent.Saved -> onFinished()
            }
        }
    }

    InterestScreen(
        state = state,
        onCategoryToggle = viewModel::toggleKategori,
        onSaveClick = viewModel::simpan,
        onMaybeLaterClick = viewModel::lewati,
        onRetryClick = viewModel::muatKategori,
    )
}

/**
 * Halaman pemilihan kategori minat (DESIGN.md §8.1).
 */
@Composable
fun InterestScreen(
    state: InterestUiState,
    onCategoryToggle: (idKategori: Int) -> Unit,
    onSaveClick: () -> Unit,
    onMaybeLaterClick: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    AuthLayout(
        title = stringResource(R.string.interest_title),
        contentMaxWidth = VexSize.interestContentWidth,
        modifier = modifier,
        formContent = {
            when {
                state.isLoading && state.categories.isEmpty() -> {
                    CircularProgressIndicator(color = colors.primary)
                }

                state.errorMessage != null && state.categories.isEmpty() -> {
                    ErrorContent(
                        message = state.errorMessage,
                        onRetryClick = onRetryClick,
                    )
                }

                state.isCategoryEmpty -> {
                    Text(
                        text = stringResource(R.string.interest_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center,
                    )
                }

                else -> {
                    InterestCategoryChips(
                        categories = state.categories,
                        selectedIds = state.selectedIds,
                        onToggle = onCategoryToggle,
                        modifier = Modifier.padding(horizontal = VexSpace.s1),
                        enabled = !state.isSaving,
                    )

                    state.errorMessage?.let { message ->
                        Spacer(modifier = Modifier.height(VexSpace.s4))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.danger,
                            textAlign = TextAlign.Center,
                        )
                        VexButton(
                            text = stringResource(R.string.interest_retry),
                            onClick = onRetryClick,
                            variant = VexButtonVariant.TextLink,
                        )
                    }
                }
            }
        },
        footerContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                VexButton(
                    text = stringResource(R.string.interest_maybe_later),
                    onClick = onMaybeLaterClick,
                    variant = VexButtonVariant.TextLink,
                    enabled = !state.isSaving,
                )

                Spacer(modifier = Modifier.height(VexSpace.s3))

                HorizontalDivider(
                    modifier = Modifier.width(VexSize.authContentWidth),
                    thickness = 1.dp,
                    color = colors.border,
                )

                Spacer(modifier = Modifier.height(VexSpace.s6))

                VexButton(
                    text = stringResource(R.string.interest_save),
                    onClick = onSaveClick,
                    variant = VexButtonVariant.Primary,
                    enabled = state.canSave,
                    loading = state.isSaving,
                    modifier = Modifier.width(VexSize.authContentWidth),
                )
            }
        },
    )
}

@Composable
private fun ErrorContent(
    message: String,
    onRetryClick: () -> Unit,
) {
    val colors = VexTheme.colors

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(VexSpace.s4),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.danger,
            textAlign = TextAlign.Center,
        )

        VexButton(
            text = stringResource(R.string.interest_retry),
            onClick = onRetryClick,
            variant = VexButtonVariant.Outline,
            modifier = Modifier.width(VexSize.authContentWidth),
        )
    }
}

// ---------------------------------------------------------
// Previews untuk 2 Mode: Dark Mode & Light Mode
// ---------------------------------------------------------

private val sampleCategories = listOf(
    KategoriModel(1, "Cybersecurity"),
    KategoriModel(2, "Software"),
    KategoriModel(3, "UI/UX"),
    KategoriModel(4, "3D"),
    KategoriModel(5, "Animation"),
    KategoriModel(6, "Game Dev"),
)

@Preview(name = "InterestScreen - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun InterestScreenDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        InterestScreen(
            state = InterestUiState(
                categories = sampleCategories,
                selectedIds = setOf(3, 4, 6),
            ),
            onCategoryToggle = {},
            onSaveClick = {},
            onMaybeLaterClick = {},
            onRetryClick = {},
        )
    }
}

@Preview(name = "InterestScreen - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun InterestScreenLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        InterestScreen(
            state = InterestUiState(
                categories = sampleCategories,
                selectedIds = setOf(1, 5),
            ),
            onCategoryToggle = {},
            onSaveClick = {},
            onMaybeLaterClick = {},
            onRetryClick = {},
        )
    }
}
