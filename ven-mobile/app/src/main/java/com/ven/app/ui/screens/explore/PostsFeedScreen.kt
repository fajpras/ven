package com.ven.app.ui.screens.explore

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ven.app.R
import com.ven.app.ui.components.feature.PostCard
import com.ven.app.ui.components.layout.VexTopBar
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Model satu item feed pada [PostsFeedScreen].
 */
data class PostsFeedItem(
    val id: String,
    val username: String,
    val userId: String? = null,
    val userAvatarUrl: String? = null,
    val title: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    val pameranId: String? = null,
    val aspectRatio: Float = 4f / 5f,
)

/**
 * Layar detail feed post (scrollable) untuk VEN (Virtual Exhibition).
 *
 * Sesuai DESIGN.md §8.5.5 & wireframe `Wireframe - Post.png`:
 * - Top bar "Posts" yang terpaku (pinned), tidak ikut scroll.
 * - Body: `LazyColumn` vertikal penuh berisi kartu [PostCard] yang dapat di-scroll.
 * - Bila [initialPostId] diberikan, feed otomatis scroll ke item tersebut.
 */
@Composable
fun PostsFeedScreen(
    posts: List<PostsFeedItem>,
    onBackClick: () -> Unit,
    onRequestMeetingClick: (postId: String) -> Unit,
    onView3dClick: (postId: String) -> Unit,
    modifier: Modifier = Modifier,
    initialPostId: String? = null,
    onUserClick: ((userId: String) -> Unit)? = null,
) {
    val listState = rememberLazyListState()

    // Auto-scroll ke post yang diketuk dari grid
    LaunchedEffect(initialPostId, posts) {
        if (!initialPostId.isNullOrBlank()) {
            val index = posts.indexOfFirst { it.id == initialPostId }
            if (index >= 0) {
                listState.scrollToItem(index)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = VexTheme.colors.bg,
        topBar = {
            VexTopBar.Back(
                title = stringResource(R.string.explore_posts_title),
                onBackClick = onBackClick,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = VexSpace.s6),
        ) {
            itemsIndexed(
                items = posts,
                key = { _, post -> post.id },
            ) { _, post ->
                PostCard(
                    username = post.username,
                    userAvatarUrl = post.userAvatarUrl,
                    title = post.title,
                    description = post.description,
                    imageUrl = post.imageUrl,
                    aspectRatio = post.aspectRatio,
                    onRequestMeetingClick = { onRequestMeetingClick(post.id) },
                    onView3dClick = { onView3dClick(post.pameranId ?: post.id) },
                    onUsernameClick = {
                        post.userId?.let { onUserClick?.invoke(it) }
                    },
                )
            }
        }
    }
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

internal val dummyPostsFeed = listOf(
    PostsFeedItem(
        id = "feed_1",
        username = "Graaph",
        title = "Cyberpunk Low Poly Mech Robot 3D",
        description = "Peletakan kalimat ini akan diisi dengan deskripsi yang akan menjelaskan dengan lebih rinci lagi dari apa yang ingin disampaikan oleh kreator.",
    ),
    PostsFeedItem(
        id = "feed_2",
        username = "Shaf.One",
        title = "Fintech Neo-Glass Mobile Banking",
        description = "Eksplorasi desain antarmuka aplikasi mobile dengan pendekatan minimalis dan tipografi tebal. Kunjungi https://vex.art/exhibit untuk detail lengkap.",
    ),
)

@Preview(name = "PostsFeedScreen - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun PostsFeedScreenDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        PostsFeedScreen(
            posts = dummyPostsFeed,
            onBackClick = {},
            onRequestMeetingClick = {},
            onView3dClick = {},
        )
    }
}

@Preview(name = "PostsFeedScreen - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun PostsFeedScreenLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        PostsFeedScreen(
            posts = dummyPostsFeed,
            onBackClick = {},
            onRequestMeetingClick = {},
            onView3dClick = {},
        )
    }
}
