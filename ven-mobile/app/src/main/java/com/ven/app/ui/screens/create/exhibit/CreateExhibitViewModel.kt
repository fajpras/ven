package com.ven.app.ui.screens.create.exhibit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ven.app.ui.components.ui.CreateContentType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface CreateExhibitEvent {
    data class NavigateToEditor(val templateId: String) : CreateExhibitEvent
    data object SwitchToPost : CreateExhibitEvent
}

class CreateExhibitViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreateExhibitUiState())
    val uiState: StateFlow<CreateExhibitUiState> = _uiState.asStateFlow()

    private val _events = Channel<CreateExhibitEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onTemplateSelected(template: ExhibitTemplateItem) {
        _uiState.update { it.copy(selectedTemplate = template) }
        viewModelScope.launch {
            _events.send(CreateExhibitEvent.NavigateToEditor(template.id))
        }
    }

    fun onContentTypeSelected(type: CreateContentType) {
        _uiState.update { it.copy(selectedContentType = type) }
        if (type == CreateContentType.Post) {
            viewModelScope.launch {
                _events.send(CreateExhibitEvent.SwitchToPost)
            }
        }
    }
}
