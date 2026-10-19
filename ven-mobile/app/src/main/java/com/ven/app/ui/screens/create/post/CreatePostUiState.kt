package com.ven.app.ui.screens.create.post

import androidx.compose.runtime.Immutable
import com.ven.app.R
import com.ven.app.ui.components.feature.MediaGridItem
import com.ven.app.ui.components.ui.CreateContentType

enum class CreatePostStep {
    MediaPicker, // Langkah 1: Pilih media dari galeri & pratinjau besar
    Details,     // Langkah 2: Formulir metadata (Title, Category, Product Link, Description)
}

/**
 * Daftar media default untuk pilihan karya pada Create Post.
 * Menggunakan aset lokal poster_1, poster_2, dan poster_3 yang telah tersedia.
 */
val dummyCreatePostMedia: List<MediaGridItem> = listOf(
    MediaGridItem("cp_1", "Virtual Exhibition VEX", R.drawable.poster_3),
    MediaGridItem("cp_2", "PBL Expo", R.drawable.poster_2),
    MediaGridItem("cp_3", "Senidz Mobile App", R.drawable.poster_1),
    MediaGridItem("cp_4", "Open Source Competition", R.drawable.poster_2),
    MediaGridItem("cp_5", "Virtual Exhibition VEX 2", R.drawable.poster_3),
    MediaGridItem("cp_6", "Senidz UI/UX Design", R.drawable.poster_1),
)

/**
 * State UI untuk alur Create Post (Langkah 1 & Langkah 2).
 */
@Immutable
data class CreatePostUiState(
    val step: CreatePostStep = CreatePostStep.MediaPicker,
    val selectedContentType: CreateContentType = CreateContentType.Post,
    val mediaItems: List<MediaGridItem> = dummyCreatePostMedia,
    val selectedMedia: MediaGridItem = dummyCreatePostMedia.first(),
    val title: String = "My Post 1",
    val category: String = "Internet Of Things",
    val productLink: String = "https://google.com",
    val description: String = "",
    val availableCategories: List<String> = defaultCreatePostCategories,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val isPostEnabled: Boolean
        get() = title.isNotBlank() && category.isNotBlank() && !isLoading
}

val defaultCreatePostCategories: List<String> = listOf(
    "Internet Of Things",
    "3D",
    "2D",
    "Animation",
    "Game Dev",
    "Cybersecurity",
    "Software",
    "UI/UX",
    "Videography",
    "Photography",
    "Automation System",
    "Fabrication",
    "Manufacturing",
    "Others",
)
