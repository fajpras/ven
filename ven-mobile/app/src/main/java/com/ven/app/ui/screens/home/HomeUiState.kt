package com.ven.app.ui.screens.home

import androidx.compose.runtime.Immutable

/**
 * Data model untuk item banner pameran yang akan datang ("Coming Soon").
 */
@Immutable
data class ComingSoonEvent(
    val id: String,
    val title: String,
    val dateText: String,
    val imageUrl: String? = null,
)

/**
 * Data model untuk postingan karya di Home Feed.
 */
@Immutable
data class PostItem(
    val id: String,
    val username: String,
    val title: String,
    val imageUrl: String? = null,
    val pameranId: String? = null,
    val aspectRatio: Float = 4f / 5f,
    val description: String = "",
)

/**
 * UI State untuk Home Screen.
 * Sesuai prinsip Clean Architecture & MVI/MVVM.
 */
@Immutable
data class HomeUiState(
    val isLoading: Boolean = false,
    val comingSoonEvents: List<ComingSoonEvent> = emptyList(),
    val posts: List<PostItem> = emptyList(),
    val errorMessage: String? = null,
)
