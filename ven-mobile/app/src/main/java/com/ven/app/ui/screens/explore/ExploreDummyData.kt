package com.ven.app.ui.screens.explore

import com.ven.app.ui.components.feature.MediaGridItem

/**
 * Daftar 14 kategori minat VEN sesuai DESIGN.md §8.5.4 (Category Filter Sheet).
 * Sumber: Interest onboarding & wireframe `Wireframe - Explore filter.png`.
 */
val exploreCategories: List<String> = listOf(
    "Cybersecurity",
    "Software",
    "UI/UX",
    "Videography",
    "Photography",
    "3D",
    "Animation",
    "Internet Of Things",
    "Automation System",
    "Game Dev",
    "Fabrication",
    "Manufacturing",
    "2D",
    "Others",
)

/**
 * Riwayat pencarian awal. Default kosong, dimuat secara dinamis dari API/database.
 */
val dummyRecentSearches: List<RecentSearchItem> = emptyList()

/**
 * Grid media Discovery awal. Default kosong, dimuat secara dinamis dari API/database.
 */
val dummyDiscoveryMedia: List<MediaGridItem> = emptyList()

/**
 * Hasil pencarian akun awal. Default kosong, dimuat secara dinamis dari API/database.
 */
val dummyResultAccounts: List<ExploreAccountItem> = emptyList()

/**
 * Hasil pencarian post awal. Default kosong, dimuat secara dinamis dari API/database.
 */
val dummyResultPosts: List<MediaGridItem> = emptyList()

/**
 * Hasil pencarian pameran awal. Default kosong, dimuat secara dinamis dari API/database.
 */
val dummyResultExhibits: List<ExhibitItem> = emptyList()

/**
 * State awal ExploreScreen dengan list kosong yang akan diisi oleh ExploreViewModel.
 */
val dummyExploreUiState = ExploreUiState(
    isSearchActive = false,
    searchQuery = "",
    selectedTab = ExploreSearchTab.ForYou,
    isFilterSheetOpen = false,
    selectedCategories = emptySet(),
    availableCategories = exploreCategories,
    recentSearches = dummyRecentSearches,
    discoveryMedia = dummyDiscoveryMedia,
    resultAccounts = dummyResultAccounts,
    resultPosts = dummyResultPosts,
    resultExhibits = dummyResultExhibits,
)
