package com.ven.app.ui.screens.create.exhibit

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ven.app.data.api.ApiPameran
import com.ven.app.ui.components.ui.CreateContentType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

sealed interface CreateExhibitEvent {
    data object NavigateToTemplates : CreateExhibitEvent
    data class NavigateToEditor(val templateId: String) : CreateExhibitEvent
    data object SwitchToPost : CreateExhibitEvent
}

class CreateExhibitViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CreateExhibitUiState())
    val uiState: StateFlow<CreateExhibitUiState> = _uiState.asStateFlow()

    private val _events = Channel<CreateExhibitEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onTitleChange(title: String) {
        _uiState.update {
            it.copy(
                title = title,
                titleError = if (title.length > 100) "Maksimal 100 karakter" else null,
            )
        }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun onBannerSelected(uri: Uri?) {
        _uiState.update {
            it.copy(
                bannerUri = uri,
                bannerError = null,
            )
        }
    }

    fun validateAndProceedToEditor(context: Context, onSuccess: () -> Unit) {
        val current = _uiState.value
        val trimmedTitle = current.title.trim()

        val titleError = when {
            trimmedTitle.isEmpty() -> "Judul pameran tidak boleh kosong"
            trimmedTitle.length > 100 -> "Maksimal 100 karakter"
            else -> null
        }

        if (titleError != null) {
            _uiState.update { it.copy(titleError = titleError) }
            return
        }

        _uiState.update {
            it.copy(
                title = trimmedTitle,
                titleError = null,
            )
        }

        val uri = current.bannerUri
        if (uri != null && current.bannerUrl.isBlank()) {
            _uiState.update { it.copy(isUploadingBanner = true) }
            viewModelScope.launch {
                try {
                    val file = uriToFile(context, uri)
                    val result = ApiPameran.uploadBanner(file)
                    result.onSuccess { data ->
                        _uiState.update {
                            it.copy(
                                bannerUrl = data.url,
                                isUploadingBanner = false,
                            )
                        }
                        onSuccess()
                    }.onFailure { error ->
                        _uiState.update {
                            it.copy(
                                isUploadingBanner = false,
                                bannerError = error.message ?: "Gagal mengunggah banner",
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isUploadingBanner = false,
                            bannerError = e.message ?: "Gagal memproses gambar",
                        )
                    }
                }
            }
        } else {
            onSuccess()
        }
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val tempFile = File.createTempFile("banner_upload_", ".jpg", context.cacheDir)
        context.contentResolver.openInputStream(uri)?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return tempFile
    }

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

    fun clearTemporaryData() {
        _uiState.update {
            CreateExhibitUiState()
        }
    }
}
