package com.ven.app.ui.screens.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.components.feature.MediaGrid
import com.ven.app.ui.components.feature.MediaGridItem
import com.ven.app.ui.components.layout.AppScaffold
import com.ven.app.ui.components.ui.SearchBar
import com.ven.app.ui.components.ui.VexSectionLabel
import com.ven.app.ui.components.ui.VexTextTabs
import com.ven.app.ui.icons.VexIcon
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.navigation.BottomNavTab
import com.ven.app.ui.screens.explore.components.CategoryFilterSheet
import com.ven.app.ui.screens.explore.components.ExhibitBannerCard
import com.ven.app.ui.screens.explore.components.SearchAccountRow
import com.ven.app.ui.screens.explore.components.SearchHistoryItem
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSize
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Layar utama Explore untuk VEN (Virtual Exhibition).
 *
 * Sesuai DESIGN.md §8.5 & wireframe dark mode `D:\front-mobile\dark-mode\explore\`:
 * - Berjalan murni di sisi frontend dengan mock data lokal mandiri.
 * - Terbungkus [AppScaffold] dengan [BottomNavTab.Explore] aktif.
 * - Mengelola 4 state tampilan secara lokal (MVI): Discovery, Search History,
 *   Search Results (4 tab), dan Filter Sheet.
 */
@Composable
fun ExploreScreen(
    onTabSelected: (BottomNavTab) -> Unit,
    onMediaClick: (MediaGridItem) -> Unit,
    modifier: Modifier = Modifier,
    onExhibitClick: (ExhibitItem) -> Unit = {},
    initialUiState: ExploreUiState = dummyExploreUiState,
) {
    var uiState by remember { mutableStateOf(initialUiState) }

    AppScaffold(
        currentTab = BottomNavTab.Explore,
        onTabSelected = onTabSelected,
        modifier = modifier,
    ) { innerPadding ->
        ExploreContent(
            uiState = uiState,
            onSearchActiveChange = { active ->
                uiState = if (active) {
                    uiState.copy(isSearchActive = true)
                } else {
                    uiState.copy(isSearchActive = false, searchQuery = "")
                }
            },
            onQueryChange = { query ->
                uiState = uiState.copy(searchQuery = query)
            },
            onTabChange = { tab ->
                uiState = uiState.copy(selectedTab = tab)
            },
            onOpenFilterSheet = {
                uiState = uiState.copy(isFilterSheetOpen = true)
            },
            onDismissFilterSheet = {
                uiState = uiState.copy(isFilterSheetOpen = false)
            },
            onCategoryToggle = { category ->
                val updated = uiState.selectedCategories.toMutableSet().apply {
                    if (!add(category)) remove(category)
                }
                uiState = uiState.copy(selectedCategories = updated)
            },
            onResetFilter = {
                uiState = uiState.copy(selectedCategories = emptySet())
            },
            onHistoryItemClick = { item ->
                uiState = when (item) {
                    is RecentSearchItem.Query -> uiState.copy(searchQuery = item.query)
                    is RecentSearchItem.Account -> uiState.copy(searchQuery = item.username)
                }
            },
            onRemoveHistoryItem = { item ->
                uiState = uiState.copy(
                    recentSearches = uiState.recentSearches.filterNot { it.id == item.id },
                )
            },
            onMediaClick = onMediaClick,
            onExhibitClick = onExhibitClick,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
        )
    }
}

/**
 * Komponen konten murni (stateless) untuk [ExploreScreen].
 */
@Composable
fun ExploreContent(
    uiState: ExploreUiState,
    onSearchActiveChange: (Boolean) -> Unit,
    onQueryChange: (String) -> Unit,
    onTabChange: (ExploreSearchTab) -> Unit,
    onOpenFilterSheet: () -> Unit,
    onDismissFilterSheet: () -> Unit,
    onCategoryToggle: (String) -> Unit,
    onResetFilter: () -> Unit,
    onHistoryItemClick: (RecentSearchItem) -> Unit,
    onRemoveHistoryItem: (RecentSearchItem) -> Unit,
    onMediaClick: (MediaGridItem) -> Unit,
    modifier: Modifier = Modifier,
    onExhibitClick: (ExhibitItem) -> Unit = {},
) {
    val colors = VexTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding(),
    ) {
        ExploreTopBar(
            isSearchActive = uiState.isSearchActive,
            query = uiState.searchQuery,
            onQueryChange = onQueryChange,
            onSearchActivate = { onSearchActiveChange(true) },
            onBackClick = { onSearchActiveChange(false) },
            onFilterClick = onOpenFilterSheet,
        )

        when {
            uiState.isShowingResults -> {
                ExploreResults(
                    uiState = uiState,
                    onTabChange = onTabChange,
                    onAccountClick = { },
                    onMediaClick = onMediaClick,
                    onExhibitClick = onExhibitClick,
                    modifier = Modifier.weight(1f),
                )
            }

            uiState.isShowingHistory -> {
                ExploreSearchHistory(
                    items = uiState.recentSearches,
                    onItemClick = onHistoryItemClick,
                    onRemoveClick = onRemoveHistoryItem,
                    modifier = Modifier.weight(1f),
                )
            }

            else -> {
                ExploreDiscovery(
                    items = uiState.discoveryMedia,
                    onMediaClick = onMediaClick,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    // Modal Bottom Sheet Filter Kategori
    if (uiState.isFilterSheetOpen) {
        CategoryFilterSheet(
            categories = uiState.availableCategories,
            selectedCategories = uiState.selectedCategories,
            onCategoryToggle = onCategoryToggle,
            onResetClick = onResetFilter,
            onDismissRequest = onDismissFilterSheet,
            resetLabel = stringResource(R.string.explore_filter_reset),
            titleLabel = stringResource(R.string.explore_filter_title),
        )
    }
}

/**
 * Top bar Explore: SearchBar (weight 1f) + tombol filter.
 * Mode Search Active: tombol kembali di kiri + SearchBar mengisi sisa lebar.
 * Di mode Discovery, mengetuk SearchBar mengaktifkan mode pencarian.
 */
@Composable
private fun ExploreTopBar(
    isSearchActive: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchActivate: () -> Unit,
    onBackClick: () -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = VexSpace.s6)
            .padding(top = VexSpace.s4, bottom = VexSpace.s4),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isSearchActive) {
            IconButton(onClick = onBackClick) {
                VexIcon(
                    id = VexIcons.Back,
                    contentDescription = stringResource(R.string.explore_back_action),
                    size = VexSize.icon,
                    tint = colors.text,
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            SearchBar(
                query = query,
                onQueryChange = onQueryChange,
                placeholder = stringResource(R.string.explore_search_placeholder),
                enabled = isSearchActive,
                modifier = Modifier.fillMaxWidth(),
            )

            // Overlay transparan di mode Discovery: tap untuk mengaktifkan pencarian
            if (!isSearchActive) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            role = Role.Button,
                            onClick = onSearchActivate,
                        ),
                )
            }
        }

        if (!isSearchActive) {
            Spacer(modifier = Modifier.width(VexSpace.s2))
            IconButton(onClick = onFilterClick) {
                VexIcon(
                    id = VexIcons.Filter,
                    contentDescription = stringResource(R.string.explore_filter_action),
                    size = VexSize.icon,
                    tint = colors.text,
                )
            }
        }
    }
}

/**
 * State Discovery: grid media 3 kolom.
 */
@Composable
private fun ExploreDiscovery(
    items: List<MediaGridItem>,
    onMediaClick: (MediaGridItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (items.isEmpty()) {
        EmptyExploreView(
            message = stringResource(R.string.explore_empty_discovery),
            modifier = modifier,
        )
        return
    }

    MediaGrid(
        items = items,
        columns = 3,
        onItemClick = onMediaClick,
        contentPadding = PaddingValues(bottom = VexSpace.s6),
        modifier = modifier,
    )
}

/**
 * State Search History ("Recents").
 */
@Composable
private fun ExploreSearchHistory(
    items: List<RecentSearchItem>,
    onItemClick: (RecentSearchItem) -> Unit,
    onRemoveClick: (RecentSearchItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.explore_recents_title),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = colors.text,
            modifier = Modifier.padding(horizontal = VexSpace.s6, vertical = VexSpace.s2),
        )

        if (items.isEmpty()) {
            EmptyExploreView(
                message = stringResource(R.string.explore_empty_history),
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = VexSpace.s6),
            ) {
                items(items = items, key = { it.id }) { item ->
                    SearchHistoryItem(
                        item = item,
                        onItemClick = onItemClick,
                        onRemoveClick = onRemoveClick,
                    )
                }
            }
        }
    }
}

/**
 * State Search Results dengan 4 tab: For you | Accounts | Posts | Exhibits.
 */
@Composable
private fun ExploreResults(
    uiState: ExploreUiState,
    onTabChange: (ExploreSearchTab) -> Unit,
    onAccountClick: (ExploreAccountItem) -> Unit,
    onMediaClick: (MediaGridItem) -> Unit,
    onExhibitClick: (ExhibitItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Column(modifier = modifier.fillMaxWidth()) {
        VexTextTabs(
            tabs = ExploreSearchTab.entries,
            selectedTab = uiState.selectedTab,
            onTabSelected = onTabChange,
            labelFor = { tab ->
                when (tab) {
                    ExploreSearchTab.ForYou -> stringResource(R.string.explore_tab_for_you)
                    ExploreSearchTab.Accounts -> stringResource(R.string.explore_tab_accounts)
                    ExploreSearchTab.Posts -> stringResource(R.string.explore_tab_posts)
                    ExploreSearchTab.Exhibits -> stringResource(R.string.explore_tab_exhibits)
                }
            },
        )

        HorizontalDivider(
            thickness = 1.dp,
            color = colors.border,
        )

        when (uiState.selectedTab) {
            ExploreSearchTab.ForYou -> {
                // Grid 2 kolom dengan header seksi Accounts
                MediaGrid(
                    items = uiState.resultPosts,
                    columns = 2,
                    onItemClick = onMediaClick,
                    contentPadding = PaddingValues(bottom = VexSpace.s6),
                    header = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (uiState.resultAccounts.isNotEmpty()) {
                                VexSectionLabel(
                                    title = stringResource(R.string.explore_section_accounts),
                                )
                                uiState.resultAccounts.take(3).forEach { account ->
                                    SearchAccountRow(
                                        account = account,
                                        onClick = { onAccountClick(account) },
                                    )
                                }
                                VexSectionLabel(
                                    title = stringResource(R.string.explore_section_posts),
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            ExploreSearchTab.Accounts -> {
                if (uiState.resultAccounts.isEmpty()) {
                    EmptyExploreView(
                        message = stringResource(R.string.explore_empty_results, uiState.searchQuery),
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = VexSpace.s6),
                    ) {
                        items(items = uiState.resultAccounts, key = { it.id }) { account ->
                            SearchAccountRow(
                                account = account,
                                onClick = { onAccountClick(account) },
                            )
                        }
                    }
                }
            }

            ExploreSearchTab.Posts -> {
                if (uiState.resultPosts.isEmpty()) {
                    EmptyExploreView(
                        message = stringResource(R.string.explore_empty_results, uiState.searchQuery),
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    MediaGrid(
                        items = uiState.resultPosts,
                        columns = 2,
                        onItemClick = onMediaClick,
                        contentPadding = PaddingValues(bottom = VexSpace.s6),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            ExploreSearchTab.Exhibits -> {
                if (uiState.resultExhibits.isEmpty()) {
                    EmptyExploreView(
                        message = stringResource(R.string.explore_empty_results, uiState.searchQuery),
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = VexSpace.s4,
                            end = VexSpace.s4,
                            top = VexSpace.s4,
                            bottom = VexSpace.s6,
                        ),
                        verticalArrangement = Arrangement.spacedBy(VexSpace.s3),
                    ) {
                        items(items = uiState.resultExhibits, key = { it.id }) { exhibit ->
                            ExhibitBannerCard(
                                title = exhibit.title,
                                imageRes = exhibit.bannerRes,
                                onClick = { onExhibitClick(exhibit) },
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tampilan kosong (Empty state) untuk Explore.
 */
@Composable
private fun EmptyExploreView(
    message: String,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textMuted,
        )
    }
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

@Preview(name = "ExploreScreen Discovery - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun ExploreScreenDiscoveryDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        ExploreContent(
            uiState = dummyExploreUiState,
            onSearchActiveChange = {},
            onQueryChange = {},
            onTabChange = {},
            onOpenFilterSheet = {},
            onDismissFilterSheet = {},
            onCategoryToggle = {},
            onResetFilter = {},
            onHistoryItemClick = {},
            onRemoveHistoryItem = {},
            onMediaClick = {},
        )
    }
}

@Preview(name = "ExploreScreen History - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun ExploreScreenHistoryDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        ExploreContent(
            uiState = dummyExploreUiState.copy(isSearchActive = true, searchQuery = ""),
            onSearchActiveChange = {},
            onQueryChange = {},
            onTabChange = {},
            onOpenFilterSheet = {},
            onDismissFilterSheet = {},
            onCategoryToggle = {},
            onResetFilter = {},
            onHistoryItemClick = {},
            onRemoveHistoryItem = {},
            onMediaClick = {},
        )
    }
}

@Preview(name = "ExploreScreen Results - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun ExploreScreenResultsDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        ExploreContent(
            uiState = dummyExploreUiState.copy(isSearchActive = true, searchQuery = "vex"),
            onSearchActiveChange = {},
            onQueryChange = {},
            onTabChange = {},
            onOpenFilterSheet = {},
            onDismissFilterSheet = {},
            onCategoryToggle = {},
            onResetFilter = {},
            onHistoryItemClick = {},
            onRemoveHistoryItem = {},
            onMediaClick = {},
        )
    }
}
