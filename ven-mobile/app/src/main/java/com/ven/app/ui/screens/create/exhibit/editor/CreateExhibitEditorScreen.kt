package com.ven.app.ui.screens.create.exhibit.editor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ven.app.R
import com.ven.app.ui.screens.create.exhibit.editor.components.Exhibit3dCanvas
import com.ven.app.ui.screens.create.exhibit.editor.components.ExhibitColorBottomPanel
import com.ven.app.ui.screens.create.exhibit.editor.components.ExhibitDetailsView
import com.ven.app.ui.screens.create.exhibit.editor.components.ExhibitFurnitureBottomPanel
import com.ven.app.ui.screens.create.exhibit.editor.components.ExhibitMainBottomPanel
import com.ven.app.ui.screens.create.exhibit.editor.components.ExhibitPanelsBottomPanel
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexTheme

/**
 * Layar Editor Ruang Pameran Virtual 3D (Langkah 2 Create Exhibit - Wireframe Create Exhibition).
 *
 * Alur & Tampilan Presisi Sesuai Wireframe:
 * 1. Layar Utama (Main Screen):
 *    - Kanvas 3D Isometrik memenuhi area atas dengan indikator badge "X/9 panels" di kiri-bawah.
 *    - Tombol Back di kiri-atas dan 4 Tombol Alat Melayang di kanan-atas:
 *      (1) Aa (Details), (2) Panel (Karya), (3) Sofa (Furnitur), (4) Warna (Wall Color).
 *    - Panel Bawah: Checklist 2 kriteria ("title & description" dan "a panel") dengan status ✕ merah atau ✓ hijau,
 *      serta tombol CTA "Publish" yang aktif jika kedua kriteria terpenuhi.
 * 2. Layar Details (Aa):
 *    - Header "Details" dengan tombol Centang (✓) di kanan-atas untuk simpan & kembali.
 *    - Pratinjau banner thumbnail 16:9, bidang input Title, dan bidang input Description.
 * 3. Mode Panel Karya (Panels):
 *    - Kanvas 3D tetap terlihat di atas dengan tombol Centang (✓) di kanan-atas.
 *    - Panel Bawah: Tab "Poster" & "Video" dengan 3 kartu karya rounded square dan paginasi titik (• • •).
 * 4. Mode Furnitur (Objects):
 *    - Kanvas 3D dengan badge "X/9 objects" dan tombol Centang (✓) di kanan-atas.
 *    - Panel Bawah: 4 Tab ("Light", "Chair", "Plant", "Other") dengan 3 kartu objek dan paginasi titik.
 * 5. Mode Warna (Color):
 *    - Kanvas 3D berubah warna dinding secara real-time dengan tombol Centang (✓) di kanan-atas.
 *    - Panel Bawah: Judul "Color" dan 12 lingkaran warna terbagi dalam 2 baris x 6 kolom.
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
        onToggleArtwork = viewModel::toggleArtworkAssignment,
        onToggleFurnitureItem = viewModel::toggleFurnitureItem,
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
    onToggleArtwork: (ExhibitArtwork) -> Unit,
    onToggleFurnitureItem: ((String) -> Unit)? = null,
    onIncrementFurniture: (ExhibitFurnitureType) -> Unit,
    onDecrementFurniture: (ExhibitFurnitureType) -> Unit,
    onSelectColor: (Color) -> Unit,
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
        if (state.activeSheet == ExhibitEditorSheet.Details) {
            // 1. Tampilan Full Layar Details Sesuai Wireframe Frame 2
            ExhibitDetailsView(
                initialTitle = state.title,
                initialDescription = state.description,
                coverBannerUri = state.coverBannerUri,
                onSave = { title, desc, banner ->
                    onSaveDetails(title, desc, banner)
                    onDismissSheet()
                },
            )
        } else {
            // 2. Kanvas 3D Isometrik & Panel Bawah Sesuai Wireframe
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                // Kanvas 3D di area atas (mengambil sisa tinggi layar responsif)
                Exhibit3dCanvas(
                    activeSheet = state.activeSheet,
                    wallColor = state.wallColor,
                    panelSlots = state.panelSlots,
                    totalPanels = state.totalPanelsCount,
                    assignedPanels = state.assignedPanelsCount,
                    assignedObjects = state.totalObjectsCount,
                    totalObjects = 9,
                    onBackClick = onBackClick,
                    onConfirmClick = onDismissSheet,
                    onToolClick = onOpenSheet,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                )

                // Panel Bawah Sesuai Status Sheet Alat
                when (state.activeSheet) {
                    ExhibitEditorSheet.None -> {
                        // Wireframe: Layar Utama (Checklist title & desc, a panel + Tombol Publish)
                        ExhibitMainBottomPanel(
                            isTitleDescComplete = state.isTitleDescComplete,
                            isPanelComplete = state.isPanelComplete,
                            canPublish = state.canPublish,
                            isPublishing = state.isPublishing,
                            onTitleDescClick = { onOpenSheet(ExhibitEditorSheet.Details) },
                            onPanelClick = { onOpenSheet(ExhibitEditorSheet.Panels) },
                            onPublish = onPublish,
                        )
                    }
                    ExhibitEditorSheet.Color -> {
                        // Wireframe Frame 1: Pemilihan Warna Dinding (12 Lingkaran Warna 2x6)
                        ExhibitColorBottomPanel(
                            selectedColor = state.wallColor,
                            onSelectColor = onSelectColor,
                        )
                    }
                    ExhibitEditorSheet.Panels -> {
                        // Wireframe Frame 3: Tab Poster/Video & 3 Kartu Karya
                        ExhibitPanelsBottomPanel(
                            panelSlots = state.panelSlots,
                            availableArtworks = state.availableArtworks,
                            onToggleArtwork = onToggleArtwork,
                        )
                    }
                    ExhibitEditorSheet.Furniture -> {
                        // Wireframe Frame 4: 4 Tab Furnitur & 3 Kartu Objek
                        ExhibitFurnitureBottomPanel(
                            furnitureCounts = state.furnitureCounts,
                            furnitureItemCounts = state.furnitureItemCounts,
                            onToggleFurnitureItem = onToggleFurnitureItem,
                            onIncrement = onIncrementFurniture,
                            onDecrement = onDecrementFurniture,
                        )
                    }
                    ExhibitEditorSheet.Details -> Unit
                }
            }
        }

        // Host Notifikasi / Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
        )
    }
}

// -------------------------------------------------------------
// Previews
// -------------------------------------------------------------

@Preview(name = "Exhibit Editor - Main Screen Incomplete", showBackground = true)
@Composable
private fun ExhibitEditorMainIncompletePreview() {
    VexTheme(mode = ThemeMode.Light) {
        CreateExhibitEditorContent(
            state = ExhibitEditorUiState(),
            onBackClick = {},
            onOpenSheet = {},
            onDismissSheet = {},
            onSaveDetails = { _, _, _ -> },
            onToggleArtwork = {},
            onIncrementFurniture = {},
            onDecrementFurniture = {},
            onSelectColor = {},
            onPublish = {},
        )
    }
}

@Preview(name = "Exhibit Editor - Color Mode", showBackground = true)
@Composable
private fun ExhibitEditorColorModePreview() {
    VexTheme(mode = ThemeMode.Light) {
        CreateExhibitEditorContent(
            state = ExhibitEditorUiState(
                activeSheet = ExhibitEditorSheet.Color,
            ),
            onBackClick = {},
            onOpenSheet = {},
            onDismissSheet = {},
            onSaveDetails = { _, _, _ -> },
            onToggleArtwork = {},
            onIncrementFurniture = {},
            onDecrementFurniture = {},
            onSelectColor = {},
            onPublish = {},
        )
    }
}

@Preview(name = "Exhibit Editor - Panels Mode", showBackground = true)
@Composable
private fun ExhibitEditorPanelsModePreview() {
    VexTheme(mode = ThemeMode.Light) {
        CreateExhibitEditorContent(
            state = ExhibitEditorUiState(
                activeSheet = ExhibitEditorSheet.Panels,
            ),
            onBackClick = {},
            onOpenSheet = {},
            onDismissSheet = {},
            onSaveDetails = { _, _, _ -> },
            onToggleArtwork = {},
            onIncrementFurniture = {},
            onDecrementFurniture = {},
            onSelectColor = {},
            onPublish = {},
        )
    }
}

@Preview(name = "Exhibit Editor - Furniture Mode", showBackground = true)
@Composable
private fun ExhibitEditorFurnitureModePreview() {
    VexTheme(mode = ThemeMode.Light) {
        CreateExhibitEditorContent(
            state = ExhibitEditorUiState(
                activeSheet = ExhibitEditorSheet.Furniture,
            ),
            onBackClick = {},
            onOpenSheet = {},
            onDismissSheet = {},
            onSaveDetails = { _, _, _ -> },
            onToggleArtwork = {},
            onIncrementFurniture = {},
            onDecrementFurniture = {},
            onSelectColor = {},
            onPublish = {},
        )
    }
}
