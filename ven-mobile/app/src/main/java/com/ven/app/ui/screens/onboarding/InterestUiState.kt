package com.ven.app.ui.screens.onboarding

import androidx.compose.runtime.Immutable
import com.ven.app.data.api.models.KategoriModel

/**
 * State halaman pemilihan kategori minat.
 */
@Immutable
data class InterestUiState(
    val categories: List<KategoriModel> = emptyList(),
    val selectedIds: Set<Int> = emptySet(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
) {
    val canSave: Boolean
        get() = selectedIds.isNotEmpty() && !isLoading && !isSaving

    val isCategoryEmpty: Boolean
        get() = categories.isEmpty() && !isLoading
}

sealed interface InterestEvent {
    data object Saved : InterestEvent
}
