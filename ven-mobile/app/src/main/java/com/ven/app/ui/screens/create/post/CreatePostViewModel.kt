package com.ven.app.ui.screens.create.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ven.app.ui.components.feature.MediaGridItem
import com.ven.app.ui.components.ui.CreateContentType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface CreatePostEvent {
    data object PostSuccess : CreatePostEvent
    data object SwitchToExhibit : CreatePostEvent
}

class CreatePostViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    private val _events = Channel<CreatePostEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onMediaSelected(media: MediaGridItem) {
        _uiState.update { it.copy(selectedMedia = media) }
    }

    fun onNextClick() {
        _uiState.update { it.copy(step = CreatePostStep.Details) }
    }

    fun onBackClick(): Boolean {
        // Return true jika step ditangani di dalam viewModel (kembali ke picker), false jika harus popBackStack
        return if (_uiState.value.step == CreatePostStep.Details) {
            _uiState.update { it.copy(step = CreatePostStep.MediaPicker) }
            true
        } else {
            false
        }
    }

    fun onContentTypeSelected(type: CreateContentType) {
        _uiState.update { it.copy(selectedContentType = type) }
        if (type == CreateContentType.Exhibit) {
            viewModelScope.launch {
                _events.send(CreatePostEvent.SwitchToExhibit)
            }
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title) }
    }

    fun onCategoryChange(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun onProductLinkChange(link: String) {
        _uiState.update { it.copy(productLink = link) }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun submitPost() {
        if (!_uiState.value.isPostEnabled) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            // Simulasi proses upload media & pembuatan post
            kotlinx.coroutines.delay(400)
            _uiState.update { it.copy(isLoading = false) }
            _events.send(CreatePostEvent.PostSuccess)
        }
    }
}
