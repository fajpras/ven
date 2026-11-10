package com.ven.app.ui.components.feature

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.components.ui.rememberAsyncImageBitmap
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexAspect
import com.ven.app.ui.theme.VexTheme

/**
 * Model data representasi satu kartu media dalam [MediaGrid].
 */
@Immutable
data class MediaGridItem(
    val id: String,
    val title: String,
    @param:DrawableRes val imageRes: Int? = null,
    val imageUrl: String? = null,
    val span: Int = 1,
)

/**
 * Tile individual karya seni/poster untuk grid media dengan sistematis Memory & Disk Caching (360x480 thumbnail).
 */
@Composable
fun MediaGridTile(
    item: MediaGridItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    aspectRatio: Float = VexAspect.poster,
) {
    val colors = VexTheme.colors

    // Memuat bitmap dari Memory LruCache (0ms) atau Disk Cache ter-downsample (360x480)
    val bitmap = rememberAsyncImageBitmap(
        url = item.imageUrl,
        maxWidth = 360,
        maxHeight = 480,
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .background(colors.surfaceInput)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = colors.text.copy(alpha = 0.2f)),
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else if (item.imageRes != null) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        // Overlay gradient dan label judul karya
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.80f),
                        ),
                        startY = 60f,
                    ),
                ),
            contentAlignment = Alignment.BottomStart,
        ) {
            Text(
                text = item.title.ifBlank { "Karya Seni" },
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }
    }
}

/**
 * Grid Media untuk Explore dan Profile dengan dukungan paginasi scroll otomatis (chunk).
 * Sesuai spesifikasi DESIGN.md §5.3 & §6.3:
 * - Standar 3 kolom, celah antar item 2dp.
 * - Sudut tajam tanpa radius (0dp).
 */
@Composable
fun MediaGrid(
    items: List<MediaGridItem>,
    modifier: Modifier = Modifier,
    columns: Int = 3,
    onItemClick: (MediaGridItem) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues(0.dp),
    header: (@Composable () -> Unit)? = null,
    onLoadMore: (() -> Unit)? = null,
    isLoadingMore: Boolean = false,
    gridState: LazyGridState = rememberLazyGridState(),
) {
    val spacing = 2.dp

    if (onLoadMore != null) {
        val shouldLoadMore by remember {
            derivedStateOf {
                val total = gridState.layoutInfo.totalItemsCount
                val lastVisible = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                total > 0 && lastVisible >= total - 4
            }
        }
        LaunchedEffect(shouldLoadMore) {
            if (shouldLoadMore && !isLoadingMore) {
                onLoadMore()
            }
        }
    }

    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalArrangement = Arrangement.spacedBy(spacing),
    ) {
        val maxLineSpan = columns

        if (header != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                header()
            }
        }

        items(
            items = items,
            key = { it.id },
            span = { item -> GridItemSpan(item.span.coerceAtMost(maxLineSpan)) },
        ) { item ->
            MediaGridTile(
                item = item,
                onClick = { onItemClick(item) },
            )
        }

        if (isLoadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = VexTheme.colors.primaryText,
                        strokeWidth = 2.dp,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

@Preview(name = "MediaGrid - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun MediaGridDarkPreview() {
    val sampleItems = listOf(
        MediaGridItem("1", "Cyberpunk Robot 3D", R.drawable.ic_panel),
        MediaGridItem("2", "Zero-Day Threat Defense", R.drawable.ic_panel),
        MediaGridItem("3", "Fintech Mobile Banking", R.drawable.ic_panel),
    )
    VexTheme(mode = ThemeMode.Dark) {
        MediaGrid(items = sampleItems)
    }
}
