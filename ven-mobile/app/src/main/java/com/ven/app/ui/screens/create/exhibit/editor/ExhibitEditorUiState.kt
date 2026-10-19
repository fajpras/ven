package com.ven.app.ui.screens.create.exhibit.editor

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.ven.app.R
import com.ven.app.ui.screens.create.exhibit.ExhibitTemplateItem
import com.ven.app.ui.screens.create.exhibit.defaultExhibitTemplates

enum class ExhibitEditorSheet {
    None,
    Details,
    Panels,
    Furniture,
    Color,
}

enum class ArtworkType {
    Poster,
    Video,
}

@Immutable
data class ExhibitArtwork(
    val id: String,
    val title: String,
    @param:DrawableRes val imageRes: Int? = null,
    val type: ArtworkType = ArtworkType.Poster,
)

@Immutable
data class ExhibitPanelSlot(
    val slotIndex: Int,
    val assignedArtwork: ExhibitArtwork? = null,
)

enum class ExhibitFurnitureType(val displayName: String) {
    Spotlight("Spotlight"),
    LoungeChair("Lounge Chair"),
    IndoorPlant("Indoor Plant"),
    GalleryBench("Gallery Bench"),
}

@Immutable
data class WallColorOption(
    val name: String,
    val color: Color,
)

val defaultWallColorOptions: List<WallColorOption> = listOf(
    WallColorOption("VEX Purple", Color(0xFF6C3BE8)),
    WallColorOption("Midnight Blue", Color(0xFF1E3A8A)),
    WallColorOption("Emerald Teal", Color(0xFF0D9488)),
    WallColorOption("Crimson Red", Color(0xFFE11D48)),
    WallColorOption("Studio Charcoal", Color(0xFF262626)),
    WallColorOption("Warm Ochre", Color(0xFFD97706)),
)

val sampleUserArtworks: List<ExhibitArtwork> = listOf(
    ExhibitArtwork(
        id = "art_1",
        title = "Virtual Exhibition VEX",
        imageRes = R.drawable.poster_3,
        type = ArtworkType.Poster,
    ),
    ExhibitArtwork(
        id = "art_2",
        title = "PBL Expo 2026",
        imageRes = R.drawable.poster_2,
        type = ArtworkType.Poster,
    ),
    ExhibitArtwork(
        id = "art_3",
        title = "Senidz Mobile App",
        imageRes = R.drawable.poster_1,
        type = ArtworkType.Video,
    ),
    ExhibitArtwork(
        id = "art_4",
        title = "Open Source Showcase",
        imageRes = R.drawable.poster_2,
        type = ArtworkType.Poster,
    ),
    ExhibitArtwork(
        id = "art_5",
        title = "Senidz UI/UX Design",
        imageRes = R.drawable.poster_1,
        type = ArtworkType.Poster,
    ),
)

@Immutable
data class ExhibitEditorUiState(
    val template: ExhibitTemplateItem = defaultExhibitTemplates[0],
    val wallColor: Color = Color(0xFF6C3BE8),
    val title: String = "",
    val description: String = "",
    val coverBannerUri: String? = null,
    val panelSlots: List<ExhibitPanelSlot> = List(defaultExhibitTemplates[0].capacity) { index ->
        ExhibitPanelSlot(slotIndex = index)
    },
    val furnitureCounts: Map<ExhibitFurnitureType, Int> = mapOf(
        ExhibitFurnitureType.Spotlight to 2,
        ExhibitFurnitureType.LoungeChair to 1,
        ExhibitFurnitureType.IndoorPlant to 1,
        ExhibitFurnitureType.GalleryBench to 0,
    ),
    val activeSheet: ExhibitEditorSheet = ExhibitEditorSheet.None,
    val selectedSlotIndexForAssignment: Int = 0,
    val availableArtworks: List<ExhibitArtwork> = sampleUserArtworks,
    val isPublishing: Boolean = false,
    val isPublishSuccess: Boolean = false,
) {
    val assignedPanelsCount: Int
        get() = panelSlots.count { it.assignedArtwork != null }

    val totalPanelsCount: Int
        get() = template.capacity

    val isTitleDescComplete: Boolean
        get() = title.isNotBlank() && description.isNotBlank()

    val isPanelComplete: Boolean
        get() = assignedPanelsCount >= 1

    val canPublish: Boolean
        get() = isTitleDescComplete && isPanelComplete
}
