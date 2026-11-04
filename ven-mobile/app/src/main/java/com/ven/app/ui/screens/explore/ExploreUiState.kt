package com.ven.app.ui.screens.explore

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import com.ven.app.ui.components.feature.MediaGridItem

/**
 * Tab pilihan pada halaman hasil pencarian Explore sesuai DESIGN.md §8.5.3:
 * - ForYou: Gabungan seksi Accounts (maks 2-3) + Posts (grid 2 kolom).
 * - Accounts: Daftar vertikal akun.
 * - Posts: Grid 2 kolom karya.
 * - Exhibits: Daftar vertikal banner pameran 16:9.
 */
enum class ExploreSearchTab {
    ForYou,
    Accounts,
    Posts,
    Exhibits,
}

/**
 * Tipe entri pada riwayat pencarian Explore ("Recents") sesuai DESIGN.md §8.5.2.
 * - Query: Kueri teks pencarian (ikon jam dalam lingkaran).
 * - Account: Akun pengguna yang pernah dibuka (avatar bulat).
 */
@Immutable
sealed interface RecentSearchItem {
    val id: String

    @Immutable
    data class Query(
        override val id: String,
        val query: String,
    ) : RecentSearchItem

    @Immutable
    data class Account(
        override val id: String,
        val username: String,
        val fullName: String,
        @param:DrawableRes val avatarRes: Int? = null,
    ) : RecentSearchItem
}

/**
 * Model item akun hasil pencarian Explore (tab Accounts / seksi Accounts tab For you).
 */
@Immutable
data class ExploreAccountItem(
    val id: String,
    val username: String,
    val fullName: String,
    @param:DrawableRes val avatarRes: Int? = null,
)

/**
 * Model item pameran hasil pencarian Explore (tab Exhibits).
 */
@Immutable
data class ExhibitItem(
    val id: String,
    val title: String,
    @param:DrawableRes val bannerRes: Int? = null,
    val bannerUrl: String? = null,
)

/**
 * UI State untuk [ExploreScreen] mengikuti pola MVI (Unidirectional Data Flow).
 * Sesuai DESIGN.md §8.5.6. Mengelola 4 state tampilan secara lokal:
 * Discovery, Search History, Search Results, dan Filter Sheet dengan paginasi chunk.
 */
@Immutable
data class ExploreUiState(
    val isSearchActive: Boolean = false,
    val searchQuery: String = "",
    val selectedTab: ExploreSearchTab = ExploreSearchTab.ForYou,
    val isFilterSheetOpen: Boolean = false,
    val selectedCategories: Set<String> = emptySet(),
    val availableCategories: List<String> = emptyList(),
    val recentSearches: List<RecentSearchItem> = emptyList(),
    val discoveryMedia: List<MediaGridItem> = emptyList(),
    val resultAccounts: List<ExploreAccountItem> = emptyList(),
    val resultPosts: List<MediaGridItem> = emptyList(),
    val resultExhibits: List<ExhibitItem> = emptyList(),
    val feedPosts: List<PostsFeedItem> = emptyList(),
    val isLoading: Boolean = false,
    val currentPostsPage: Int = 1,
    val canLoadMorePosts: Boolean = true,
    val isLoadingMorePosts: Boolean = false,
) {
    /** Apakah pengguna sedang menampilkan hasil pencarian (kueri tidak kosong). */
    val isShowingResults: Boolean
        get() = isSearchActive && searchQuery.isNotBlank()

    /** Apakah state berada di riwayat pencarian (search aktif tanpa kueri). */
    val isShowingHistory: Boolean
        get() = isSearchActive && searchQuery.isBlank()

    /** Daftar kategori yang sudah difilter (kosong = tanpa filter). */
    val hasActiveFilter: Boolean
        get() = selectedCategories.isNotEmpty()
}
