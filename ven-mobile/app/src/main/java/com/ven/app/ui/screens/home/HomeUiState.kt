package com.ven.app.ui.screens.home

import androidx.compose.runtime.Immutable
import com.ven.app.data.api.models.KaryaItemModel
import com.ven.app.data.api.models.PameranItemModel
import com.ven.app.ui.screens.explore.PostsFeedItem

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
    val userId: String? = null,
    val userAvatarUrl: String? = null,
    val title: String,
    val imageUrl: String? = null,
    val pameranId: String? = null,
    val aspectRatio: Float = 4f / 5f,
    val description: String = "",
)

/**
 * UI State untuk Home Screen.
 * Sesuai prinsip Clean Architecture & MVI/MVVM dengan dukungan paginasi chunk.
 */
@Immutable
data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val currentPage: Int = 1,
    val comingSoonEvents: List<ComingSoonEvent> = emptyList(),
    val posts: List<PostItem> = emptyList(),
    val errorMessage: String? = null,
)

/**
 * Helper mapper dari DTO ke UI models.
 */
fun PameranItemModel.toComingSoonEvent(): ComingSoonEvent {
    val dateDisplay = createdAt?.take(10) ?: "Coming Soon"
    return ComingSoonEvent(
        id = idPameran.toString(),
        title = judul,
        dateText = dateDisplay,
        imageUrl = banner,
    )
}

fun KaryaItemModel.toPostItem(): PostItem {
    val author = pengguna?.namaPanggilan ?: pengguna?.nama ?: "Anonim"
    return PostItem(
        id = idKarya,
        username = "@$author",
        userId = pengguna?.idPengguna,
        userAvatarUrl = pengguna?.fotoProfil,
        title = judul,
        imageUrl = fileUrl ?: file,
        pameranId = idPameran?.toString(),
        description = deskripsi ?: "",
    )
}

fun PostItem.toFeedItem(): PostsFeedItem {
    return PostsFeedItem(
        id = id,
        username = username,
        userId = userId,
        userAvatarUrl = userAvatarUrl,
        title = title,
        description = description,
        imageUrl = imageUrl,
        pameranId = pameranId,
        aspectRatio = aspectRatio,
    )
}
