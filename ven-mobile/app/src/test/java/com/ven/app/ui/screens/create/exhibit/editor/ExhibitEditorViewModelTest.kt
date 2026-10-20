package com.ven.app.ui.screens.create.exhibit.editor

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ExhibitEditorViewModelTest {

    private lateinit var viewModel: ExhibitEditorViewModel

    @Before
    fun setUp() {
        viewModel = ExhibitEditorViewModel()
    }

    @Test
    fun initialState_matchesWireframeUnreadyState() {
        val state = viewModel.uiState.value

        assertEquals(ExhibitEditorSheet.None, state.activeSheet)
        assertEquals(0, state.assignedPanelsCount)
        assertEquals(0, state.totalObjectsCount)
        assertFalse("title & description should be incomplete initially", state.isTitleDescComplete)
        assertFalse("a panel should be incomplete initially", state.isPanelComplete)
        assertFalse("publish should be disabled initially", state.canPublish)
    }

    @Test
    fun detailsUpdate_completesTitleDescCriteria() {
        viewModel.updateTitle("My Exhibition 1")
        viewModel.updateDescription("Solo showcase description")

        val state = viewModel.uiState.value
        assertTrue("title & description must be complete", state.isTitleDescComplete)
        assertFalse("a panel is still incomplete", state.canPublish)
    }

    @Test
    fun artworkToggle_assignsAndUnassignsPanel() {
        val artwork = sampleUserArtworks[0]

        // Assign
        viewModel.toggleArtworkAssignment(artwork)
        var state = viewModel.uiState.value
        assertEquals(1, state.assignedPanelsCount)
        assertTrue("a panel criteria satisfied", state.isPanelComplete)

        // Unassign by toggling again
        viewModel.toggleArtworkAssignment(artwork)
        state = viewModel.uiState.value
        assertEquals(0, state.assignedPanelsCount)
        assertFalse("a panel criteria incomplete after unassigning", state.isPanelComplete)
    }

    @Test
    fun bothCriteriaComplete_enablesPublish() {
        viewModel.updateTitle("Exhibition PBL 2026")
        viewModel.updateDescription("Exhibition of student projects")
        viewModel.toggleArtworkAssignment(sampleUserArtworks[0])

        val state = viewModel.uiState.value
        assertTrue(state.isTitleDescComplete)
        assertTrue(state.isPanelComplete)
        assertTrue("Publish button must be enabled", state.canPublish)
    }

    @Test
    fun wallColorSelection_updatesWallColor() {
        val targetColor = Color(0xFFFF8A80)
        viewModel.selectWallColor(targetColor)

        assertEquals(targetColor, viewModel.uiState.value.wallColor)
    }

    @Test
    fun furnitureOperations_updatesObjectsCount() {
        viewModel.incrementFurniture(ExhibitFurnitureType.Spotlight)
        viewModel.incrementFurniture(ExhibitFurnitureType.LoungeChair)

        val state = viewModel.uiState.value
        assertEquals(2, state.totalObjectsCount)

        viewModel.decrementFurniture(ExhibitFurnitureType.Spotlight)
        assertEquals(1, viewModel.uiState.value.totalObjectsCount)
    }

    @Test
    fun sheetNavigation_opensAndDismissesCorrectly() {
        viewModel.openSheet(ExhibitEditorSheet.Color)
        assertEquals(ExhibitEditorSheet.Color, viewModel.uiState.value.activeSheet)

        viewModel.dismissSheet()
        assertEquals(ExhibitEditorSheet.None, viewModel.uiState.value.activeSheet)
    }
}
