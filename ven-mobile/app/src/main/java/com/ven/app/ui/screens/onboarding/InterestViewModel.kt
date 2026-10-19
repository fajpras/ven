package com.ven.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ven.app.data.api.ApiProfil
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Mengelola pemuatan kategori, pilihan pengguna, dan penyimpanan minat.
 * Tombol "Maybe later" mengirim daftar kosong agar onboarding tetap ditandai selesai.
 */
class InterestViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(InterestUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<InterestEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        muatKategori()
    }

    fun muatKategori() {
        if (_uiState.value.isLoading || _uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            ApiProfil.kategori()
                .onSuccess { categories ->
                    val minat = ApiProfil.minat()

                    _uiState.update {
                        it.copy(
                            categories = categories,
                            selectedIds = minat.getOrElse { emptyList() }
                                .map { item -> item.idKategori }
                                .toSet(),
                            isLoading = false,
                            errorMessage = minat.exceptionOrNull()?.message,
                        )
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Gagal memuat kategori minat",
                        )
                    }
                }
        }
    }

    fun toggleKategori(idKategori: Int) {
        _uiState.update { state ->
            if (idKategori in state.selectedIds) {
                state.copy(selectedIds = state.selectedIds - idKategori)
            } else {
                state.copy(selectedIds = state.selectedIds + idKategori)
            }
        }
    }

    fun simpan() {
        val state = _uiState.value
        if (!state.canSave) return

        simpanMinat(state.selectedIds)
    }

    fun lewati() {
        if (_uiState.value.isSaving) return

        simpanMinat(emptySet())
    }

    private fun simpanMinat(idKategori: Set<Int>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            ApiProfil.simpanMinat(idKategori)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.send(InterestEvent.Saved)
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = exception.message ?: "Gagal menyimpan kategori minat",
                        )
                    }
                }
        }
    }
}
