package com.ven.app.ui.screens.create.exhibit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ven.app.R
import com.ven.app.ui.components.layout.VexTopBar
import com.ven.app.ui.components.ui.CreateContentType
import com.ven.app.ui.components.ui.SegmentedPill
import com.ven.app.ui.screens.create.exhibit.components.IsometricRoomPreview
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Layar Pemilihan Template Ruang Pameran Virtual (New Exhibit - Langkah 1).
 * Sesuai spesifikasi DESIGN.md §8.14 & Wireframe Figma (node 4256-704):
 * - TopBar: X + "New Exhibit" (kiri)
 * - Daftar vertikal template: Small (9 Post), Medium (14 Post), Big (21 Post), Event (60 Post)
 * - Kartu pratinjau isometrik 3D ruang pameran berlatar grid gelap
 * - SegmentedPill melayang di bawah tengah ("Post | Exhibit", Exhibit aktif)
 */
@Composable
fun CreateExhibitTemplateScreen(
    onCloseClick: () -> Unit,
    onNavigateToPost: () -> Unit,
    onTemplateSelected: (ExhibitTemplateItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateExhibitViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateExhibitEvent.NavigateToEditor -> {
                    val template = state.templates.find { it.id == event.templateId }
                    if (template != null) {
                        onTemplateSelected(template)
                    }
                }
                CreateExhibitEvent.SwitchToPost -> onNavigateToPost()
            }
        }
    }

    CreateExhibitTemplateContent(
        state = state,
        onCloseClick = onCloseClick,
        onTemplateClick = viewModel::onTemplateSelected,
        onContentTypeSelected = viewModel::onContentTypeSelected,
        modifier = modifier,
    )
}

@Composable
fun CreateExhibitTemplateContent(
    state: CreateExhibitUiState,
    onCloseClick: () -> Unit,
    onTemplateClick: (ExhibitTemplateItem) -> Unit,
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
            // TopBar: X + "New Exhibit" di kiri
            VexTopBar.Create(
                title = stringResource(R.string.create_exhibit_title),
                onCloseOrBackClick = onCloseClick,
                isBack = false,
            )

            // Daftar Template Ruangan Isometrik 3D
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = VexSpace.s6,
                    end = VexSpace.s6,
                    top = VexSpace.s4,
                    bottom = 96.dp, // Ruang lega agar template terbawah tidak tertutup pill melayang
                ),
                verticalArrangement = Arrangement.spacedBy(VexSpace.s6),
            ) {
                items(
                    items = state.templates,
                    key = { it.id },
                ) { item ->
                    ExhibitTemplateCard(
                        template = item,
                        onClick = { onTemplateClick(item) },
                    )
                }
            }
        }

        // SegmentedPill "Post | Exhibit" melayang di bawah tengah kanvas
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
 * Komponen kartu template individual (Judul + Kapasitas + Pratinjau Ruang Isometrik 3D)
 */
@Composable
fun ExhibitTemplateCard(
    template: ExhibitTemplateItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        // Baris Header: Nama Template di kiri, Jumlah Kapasitas Post di kanan
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = VexSpace.s2),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(template.nameRes),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = colors.text,
            )

            Text(
                text = stringResource(R.string.create_exhibit_post_count, template.capacity),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textMuted,
            )
        }

        // Kartu Visualisasi Isometrik Ruangan 3D
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(VexRadius.md))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = colors.text.copy(alpha = 0.15f)),
                    role = Role.Button,
                    onClick = onClick,
                ),
        ) {
            IsometricRoomPreview(
                cardHeight = 176.dp,
                scaleMultiplier = template.scaleMultiplier,
            )
        }
    }
}

// -------------------------------------------------------------
// Previews
// -------------------------------------------------------------

@Preview(name = "New Exhibit - Dark", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun CreateExhibitDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        CreateExhibitTemplateContent(
            state = CreateExhibitUiState(),
            onCloseClick = {},
            onTemplateClick = {},
            onContentTypeSelected = {},
        )
    }
}

@Preview(name = "New Exhibit - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun CreateExhibitLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        CreateExhibitTemplateContent(
            state = CreateExhibitUiState(),
            onCloseClick = {},
            onTemplateClick = {},
            onContentTypeSelected = {},
        )
    }
}
