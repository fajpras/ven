package com.ven.app.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.components.feature.CarouselExhibitions
import com.ven.app.ui.components.feature.EventBanner
import com.ven.app.ui.components.feature.PostCard
import com.ven.app.ui.components.layout.AppScaffold
import com.ven.app.ui.components.layout.VexTopBar
import com.ven.app.ui.navigation.BottomNavTab
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onTabSelected: (BottomNavTab) -> Unit,
    onCreateClick: () -> Unit,
    onRequestMeetingClick: (postId: String) -> Unit,
    onView3dClick: (pameranId: String?) -> Unit,
    onBannerClick: (eventId: String) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    onTabReselected: ((BottomNavTab) -> Unit)? = null,
    onRefresh: () -> Unit = {},
    onLoadMore: () -> Unit = {},
    onUserClick: (userId: String) -> Unit = {},
) {
    val colors = VexTheme.colors

    val shouldLoadMore by remember {
        derivedStateOf {
            val total = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 0 && lastVisible >= total - 3
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && uiState.canLoadMore && !uiState.isLoadingMore && !uiState.isLoading) {
            onLoadMore()
        }
    }

    AppScaffold(
        currentTab = BottomNavTab.Home,
        onTabSelected = onTabSelected,
        onTabReselected = onTabReselected,
        modifier = modifier,
        topBar = {
            VexTopBar.Home(
                onCreateClick = onCreateClick,
            )
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = VexSpace.s6),
            ) {
            if (uiState.comingSoonEvents.isNotEmpty()) {
                item {
                    CarouselExhibitions(
                        title = stringResource(R.string.home_coming_soon),
                    ) {
                        items(
                            items = uiState.comingSoonEvents,
                            key = { it.id },
                        ) { event ->
                            EventBanner(
                                title = event.title,
                                dateText = event.dateText,
                                imageUrl = event.imageUrl,
                                onClick = { onBannerClick(event.id) },
                            )
                        }
                    }
                }
            }

            if (uiState.posts.isNotEmpty()) {
                items(
                    items = uiState.posts,
                    key = { it.id },
                ) { post ->
                    PostCard(
                        username = post.username,
                        userAvatarUrl = post.userAvatarUrl,
                        title = post.title,
                        category = post.category,
                        timestampText = post.timestampText,
                        description = post.description,
                        imageUrl = post.imageUrl,
                        aspectRatio = post.aspectRatio,
                        onRequestMeetingClick = { onRequestMeetingClick(post.id) },
                        onView3dClick = { onView3dClick(post.pameranId) },
                        onUsernameClick = {
                            post.userId?.let { onUserClick(it) }
                        },
                    )
                }

                if (uiState.isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = colors.primaryText,
                                strokeWidth = 2.dp,
                            )
                        }
                    }
                }
            } else if (!uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.home_empty_feed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textMuted,
                        )
                    }
                }
            }
        }
    }
}
}

val SampleHomeUiState = HomeUiState()

@Preview(name = "Home Screen - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun HomeScreenDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        HomeScreen(
            uiState = SampleHomeUiState,
            onTabSelected = {},
            onCreateClick = {},
            onRequestMeetingClick = {},
            onView3dClick = {},
            onBannerClick = {},
        )
    }
}

@Preview(name = "Home Screen - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun HomeScreenLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        HomeScreen(
            uiState = SampleHomeUiState,
            onTabSelected = {},
            onCreateClick = {},
            onRequestMeetingClick = {},
            onView3dClick = {},
            onBannerClick = {},
        )
    }
}
