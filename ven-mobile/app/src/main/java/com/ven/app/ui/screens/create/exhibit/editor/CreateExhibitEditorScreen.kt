package com.ven.app.ui.screens.create.exhibit.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ven.app.R
import com.ven.app.ui.components.ui.VexButton
import com.ven.app.ui.screens.create.exhibit.editor.components.Exhibit3dCanvas
import com.ven.app.ui.screens.create.exhibit.editor.components.ExhibitPublishChecklist
import com.ven.app.ui.screens.create.exhibit.editor.sheets.ExhibitColorBottomSheet
import com.ven.app.ui.screens.create.exhibit.editor.sheets.ExhibitDetailsBottomSheet
import com.ven.app.ui.screens.create.exhibit.editor.sheets.ExhibitFurnitureBottomSheet
import com.ven.app.ui.screens.create.exhibit.editor.sheets.ExhibitPanelsBottomSheet
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Layar Editor Ruang Pameran Virtual 3D (Langkah 2 Create Exhibit - DESIGN.md §8.14.2).
 *
 * Fitur:
 * - Kanvas 3D Isometrik dinamis dengan sudut bawah melengkung 28dp.
 * - Tombol Back di kiri-atas untuk kembali ke pemilihan template.
 * - 4 Tombol Alat Melayang (Aa Details, Panel Karya, Furnitur Objek, dan Warna Dinding).
 * - Indikator jumlah panel "X/N panels" di kiri-bawah kanvas.
 * - Baris Checklist Syarat Penerbitan (title & description, a panel).
 * - Tombol CTA "Publish" yang aktif secara validasi real-time.
 */
@Composable
fun CreateExhibitEditorScreen(
    onBackClick: () -> Unit,
    onPublishSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExhibitEditorViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val publishSuccessMessage = stringResource(R.string.create_exhibit_publish_success)

    BackHandler {
        if (state.activeSheet != ExhibitEditorSheet.None) {
            viewModel.dismissSheet()
        } else {
            onBackClick()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ExhibitEditorEvent.PublishSuccess -> {
                    snackbarHostState.showSnackbar(publishSuccessMessage)
                    onPublishSuccess()
                }
                is ExhibitEditorEvent.ShowToast -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    CreateExhibitEditorContent(
        state = state,
        onBackClick = onBackClick,
        onOpenSheet = viewModel::openSheet,
        onDismissSheet = viewModel::dismissSheet,
        onSaveDetails = { title, desc, banner ->
            viewModel.updateTitle(title)
            viewModel.updateDescription(desc)
            viewModel.updateCoverBanner(banner)
        },
        onSelectSlot = viewModel::selectSlotForAssignment,
        onAssignArtwork = viewModel::assignArtworkToSlot,
        onRemoveArtwork = viewModel::removeArtworkFromSlot,
        onIncrementFurniture = viewModel::incrementFurniture,
        onDecrementFurniture = viewModel::decrementFurniture,
        onSelectColor = viewModel::selectWallColor,
        onPublish = viewModel::publishExhibit,
        modifier = modifier,
        snackbarHostState = snackbarHostState,
    )
}

@Composable
fun CreateExhibitEditorContent(
    state: ExhibitEditorUiState,
    onBackClick: () -> Unit,
    onOpenSheet: (ExhibitEditorSheet) -> Unit,
    onDismissSheet: () -> Unit,
    onSaveDetails: (title: String, desc: String, banner: String?) -> Unit,
    onSelectSlot: (Int) -> Unit,
    onAssignArtwork: (ExhibitArtwork) -> Unit,
    onRemoveArtwork: (Int) -> Unit,
    onIncrementFurniture: (ExhibitFurnitureType) -> Unit,
    onDecrementFurniture: (ExhibitFurnitureType) -> Unit,
    onSelectColor: (androidx.compose.ui.graphics.Color) -> Unit,
    onPublish: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val colors = VexTheme.colors

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // 1. Kanvas 3D Isometrik & 4 Alat Melayang
            Exhibit3dCanvas(
                wallColor = state.wallColor,
                panelSlots = state.panelSlots,
                totalPanels = state.totalPanelsCount,
                assignedPanels = state.assignedPanelsCount,
                onBackClick = onBackClick,
                onToolClick = onOpenSheet,
            )

            // 2. Area Bawah: Checklist Syarat Penerbitan & Tombol Publish
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = VexSpace.s6, vertical = VexSpace.s6)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(VexSpace.s6),
            ) {
                // Checklist Status Penerbitan
                ExhibitPublishChecklist(
                    isTitleDescComplete = state.isTitleDescComplete,
                    isPanelComplete = state.isPanelComplete,
                    onTitleDescClick = { onOpenSheet(ExhibitEditorSheet.Details) },
                    onPanelClick = { onOpenSheet(ExhibitEditorSheet.Panels) },
                )

                Spacer(modifier = Modifier.height(VexSpace.s2))

                // Tombol CTA Publish
                VexButton(
                    text = stringResource(R.string.create_exhibit_publish_action),
                    onClick = onPublish,
                    enabled = state.canPublish && !state.isPublishing,
                    loading = state.isPublishing,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Host Notifikasi / Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
        )

        // 3. Bottom Sheets Sesuai Alat yang Sedang Aktif
        when (state.activeSheet) {
            ExhibitEditorSheet.Details -> {
                ExhibitDetailsBottomSheet(
                    initialTitle = state.title,
                    initialDescription = state.description,
                    coverBannerUri = state.coverBannerUri,
                    onSave = onSaveDetails,
                    onDismiss = onDismissSheet,
                )
            }
            ExhibitEditorSheet.Panels -> {
                ExhibitPanelsBottomSheet(
                    panelSlots = state.panelSlots,
                    selectedSlotIndex = state.selectedSlotIndexForAssignment,
                    availableArtworks = state.availableArtworks,
                    onSelectSlot = onSelectSlot,
                    onAssignArtwork = onAssignArtwork,
                    onRemoveArtwork = onRemoveArtwork,
                    onDismiss = onDismissSheet,
                )
            }
            ExhibitEditorSheet.Furniture -> {
                ExhibitFurnitureBottomSheet(
                    furnitureCounts = state.furnitureCounts,
                    onIncrement = onIncrementFurniture,
                    onDecrement = onDecrementFurniture,
                    onDismiss = onDismissSheet,
                )
            }
            ExhibitEditorSheet.Color -> {
                ExhibitColorBottomSheet(
                    selectedColor = state.wallColor,
                    onSelectColor = onSelectColor,
                    onDismiss = onDismissSheet,
                )
            }
            ExhibitEditorSheet.None -> {
                // Tidak ada sheet terbuka
            }
        }
    }
}

// -------------------------------------------------------------
// Previews
// -------------------------------------------------------------

@Preview(name = "Exhibit Editor - Dark", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun ExhibitEditorDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        CreateExhibitEditorContent(
            state = ExhibitEditorUiState(
                title = "Virtual Retrospective",
                description = "Solo design showcase",
            ),
            onBackClick = {},
            onOpenSheet = {},
            onDismissSheet = {},
            onSaveDetails = { _, _, _ -> },
            onSelectSlot = {},
            onAssignArtwork = {},
            onRemoveArtwork = {},
            onIncrementFurniture = {},
            onDecrementFurniture = {},
            onSelectColor = {},
            onPublish = {},
        )
    }
}

@Preview(name = "Exhibit Editor - Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ExhibitEditorLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        CreateExhibitEditorContent(
            state = ExhibitEditorUiState(),
            onBackClick = {},
            onOpenSheet = {},
            onDismissSheet = {},
            onSaveDetails = { _, _, _ -> },
            onSelectSlot = {},
            onAssignArtwork = {},
            onRemoveArtwork = {},
            onIncrementFurniture = {},
            onDecrementFurniture = {},
            onSelectColor = {},
            onPublish = {},
        )
    }
}
