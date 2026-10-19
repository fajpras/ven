package com.ven.app.ui.screens.create.exhibit.editor

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ven.app.ui.screens.create.exhibit.defaultExhibitTemplates
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ExhibitEditorEvent {
    data object PublishSuccess : ExhibitEditorEvent
    data class ShowToast(val message: String) : ExhibitEditorEvent
}

class ExhibitEditorViewModel(
    savedStateHandle: SavedStateHandle? = null,
) : ViewModel() {

    private val templateId: String = savedStateHandle?.get<String>("templateId") ?: "tpl_small"

    private val initialTemplate = defaultExhibitTemplates.find { it.id == templateId }
        ?: defaultExhibitTemplates[0]

    private val _uiState = MutableStateFlow(
        ExhibitEditorUiState(
            template = initialTemplate,
            panelSlots = List(initialTemplate.capacity) { ExhibitPanelSlot(slotIndex = it) },
        )
    )
    val uiState: StateFlow<ExhibitEditorUiState> = _uiState.asStateFlow()

    private val _events = Channel<ExhibitEditorEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun openSheet(sheet: ExhibitEditorSheet) {
        _uiState.update { it.copy(activeSheet = sheet) }
    }

    fun dismissSheet() {
        _uiState.update { it.copy(activeSheet = ExhibitEditorSheet.None) }
    }

    fun updateTitle(newTitle: String) {
        _uiState.update { it.copy(title = newTitle) }
    }

    fun updateDescription(newDesc: String) {
        _uiState.update { it.copy(description = newDesc) }
    }

    fun updateCoverBanner(uri: String?) {
        _uiState.update { it.copy(coverBannerUri = uri) }
    }

    fun selectWallColor(color: Color) {
        _uiState.update { it.copy(wallColor = color) }
    }

    fun selectSlotForAssignment(slotIndex: Int) {
        _uiState.update {
            it.copy(
                selectedSlotIndexForAssignment = slotIndex,
                activeSheet = ExhibitEditorSheet.Panels,
            )
        }
    }

    fun assignArtworkToSlot(artwork: ExhibitArtwork, slotIndex: Int = _uiState.value.selectedSlotIndexForAssignment) {
        _uiState.update { state ->
            val updatedSlots = state.panelSlots.map { slot ->
                if (slot.slotIndex == slotIndex) {
                    slot.copy(assignedArtwork = artwork)
                } else {
                    slot
                }
            }
            state.copy(panelSlots = updatedSlots)
        }
    }

    fun removeArtworkFromSlot(slotIndex: Int) {
        _uiState.update { state ->
            val updatedSlots = state.panelSlots.map { slot ->
                if (slot.slotIndex == slotIndex) {
                    slot.copy(assignedArtwork = null)
                } else {
                    slot
                }
            }
            state.copy(panelSlots = updatedSlots)
        }
    }

    fun incrementFurniture(type: ExhibitFurnitureType) {
        _uiState.update { state ->
            val current = state.furnitureCounts[type] ?: 0
            state.copy(
                furnitureCounts = state.furnitureCounts + (type to (current + 1).coerceAtMost(10))
            )
        }
    }

    fun decrementFurniture(type: ExhibitFurnitureType) {
        _uiState.update { state ->
            val current = state.furnitureCounts[type] ?: 0
            state.copy(
                furnitureCounts = state.furnitureCounts + (type to (current - 1).coerceAtLeast(0))
            )
        }
    }

    fun publishExhibit() {
        if (!_uiState.value.canPublish) return

        viewModelScope.launch {
            _uiState.update { it.copy(isPublishing = true) }
            delay(800) // Simulasi publish room ke server
            _uiState.update { it.copy(isPublishing = false, isPublishSuccess = true) }
            _events.send(ExhibitEditorEvent.PublishSuccess)
        }
    }
}
