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

enum class ExhibitFurnitureCategory(val displayName: String) {
    Light("Light"),
    Chair("Chair"),
    Plant("Plant"),
    Other("Other"),
}

@Immutable
data class ExhibitFurnitureItem(
    val id: String,
    val name: String,
    val category: ExhibitFurnitureCategory,
    val type: ExhibitFurnitureType,
    @param:DrawableRes val iconRes: Int = R.drawable.ic_furniture,
)

val sampleFurnitureItems: List<ExhibitFurnitureItem> = listOf(
    // Light
    ExhibitFurnitureItem("f_light_1", "Spotlight", ExhibitFurnitureCategory.Light, ExhibitFurnitureType.Spotlight),
    ExhibitFurnitureItem("f_light_2", "Floor Lamp", ExhibitFurnitureCategory.Light, ExhibitFurnitureType.Spotlight),
    ExhibitFurnitureItem("f_light_3", "Track Light", ExhibitFurnitureCategory.Light, ExhibitFurnitureType.Spotlight),
    // Chair
    ExhibitFurnitureItem("f_chair_1", "Lounge Chair", ExhibitFurnitureCategory.Chair, ExhibitFurnitureType.LoungeChair),
    ExhibitFurnitureItem("f_chair_2", "Modern Stool", ExhibitFurnitureCategory.Chair, ExhibitFurnitureType.LoungeChair),
    ExhibitFurnitureItem("f_chair_3", "Gallery Bench", ExhibitFurnitureCategory.Chair, ExhibitFurnitureType.GalleryBench),
    // Plant
    ExhibitFurnitureItem("f_plant_1", "Monstera", ExhibitFurnitureCategory.Plant, ExhibitFurnitureType.IndoorPlant),
    ExhibitFurnitureItem("f_plant_2", "Indoor Ficus", ExhibitFurnitureCategory.Plant, ExhibitFurnitureType.IndoorPlant),
    ExhibitFurnitureItem("f_plant_3", "Desk Bonsai", ExhibitFurnitureCategory.Plant, ExhibitFurnitureType.IndoorPlant),
    // Other
    ExhibitFurnitureItem("f_other_1", "Info Stand", ExhibitFurnitureCategory.Other, ExhibitFurnitureType.GalleryBench),
    ExhibitFurnitureItem("f_other_2", "Room Divider", ExhibitFurnitureCategory.Other, ExhibitFurnitureType.GalleryBench),
    ExhibitFurnitureItem("f_other_3", "Pedestal", ExhibitFurnitureCategory.Other, ExhibitFurnitureType.GalleryBench),
)

@Immutable
data class WallColorOption(
    val name: String,
    val color: Color,
)

/**
 * 12 Palet Warna Dinding Ruang Pameran 3D sesuai Wireframe (2 baris x 6 warna):
 * Baris 1: White, Black, Coral, Cyan/Mint, Yellow, Purple
 * Baris 2: Light Grey, Dark Grey, Hot Pink, Soft Mint, Pale Yellow, Lavender
 */
val exhibitWallPalette: List<Color> = listOf(
    Color(0xFFFFFFFF), // White
    Color(0xFF1E1E1E), // Black
    Color(0xFFFF8A80), // Coral / Salmon
    Color(0xFF80FFE8), // Mint / Cyan
    Color(0xFFFFD54F), // Yellow / Gold
    Color(0xFF8B5CF6), // Purple
    Color(0xFFE5E5EA), // Light Grey
    Color(0xFF48484A), // Dark Grey
    Color(0xFFFF4081), // Hot Pink
    Color(0xFFA7F3D0), // Soft Mint
    Color(0xFFFFF59D), // Pale Yellow
    Color(0xFFC4B5FD), // Lavender
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
    val furnitureCounts: Map<ExhibitFurnitureType, Int> = emptyMap(),
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

    val totalObjectsCount: Int
        get() = furnitureCounts.values.sum()

    val isTitleDescComplete: Boolean
        get() = title.isNotBlank() && description.isNotBlank()

    val isPanelComplete: Boolean
        get() = assignedPanelsCount >= 1

    val canPublish: Boolean
        get() = isTitleDescComplete && isPanelComplete
}
