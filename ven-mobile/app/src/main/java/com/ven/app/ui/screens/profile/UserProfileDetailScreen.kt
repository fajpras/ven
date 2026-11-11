package com.ven.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.data.api.ApiClient
import com.ven.app.data.api.ApiPencarian
import com.ven.app.data.api.models.UserProfileDetailModel
import com.ven.app.ui.components.feature.MediaGridItem
import com.ven.app.ui.components.feature.MediaGridTile
import com.ven.app.ui.components.layout.VexTopBar
import com.ven.app.ui.components.ui.Avatar
import com.ven.app.ui.components.ui.AvatarSize
import com.ven.app.ui.components.ui.VexIconTabs
import com.ven.app.ui.components.ui.VexSkillChip
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.theme.CategoryColorMap
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Layar Detail Profil Pengguna Publik (Instagram Style) untuk melihat karya, pameran,
 * dan identitas kreator lain di aplikasi VEN.
 */
@Composable
fun UserProfileDetailScreen(
    userId: String,
    onBackClick: () -> Unit,
    onMediaClick: (MediaGridItem) -> Unit,
    onExhibitClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors
    val apiPencarian = remember { ApiClient.createService(ApiPencarian::class.java) }

    var isLoading by remember { mutableStateOf(true) }
    var userDetail by remember { mutableStateOf<UserProfileDetailModel?>(null) }
    var selectedTab by remember { mutableStateOf(ProfileTab.Posts) }

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            isLoading = true
            withContext(Dispatchers.IO) {
                try {
                    val res = apiPencarian.getDetailPengguna(userId)
                    if (res.isSuccessful) {
                        userDetail = res.body()?.data
                    }
                } catch (_: Exception) {
                } finally {
                    isLoading = false
                }
            }
        }
    }

    val userInfo = userDetail?.pengguna
    val usernameDisplay = userInfo?.namaPanggilan?.let { if (it.startsWith("@")) it else "@$it" }
        ?: userInfo?.nama
        ?: stringResource(R.string.profile_unknown_user)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.bg,
        topBar = {
            VexTopBar.Back(
                title = usernameDisplay,
                onBackClick = onBackClick,
            )
        },
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = colors.primaryText)
            }
        } else if (userInfo == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.profile_user_not_found),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted,
                )
            }
        } else {
            val karyaItems = remember(userDetail?.karya) {
                userDetail?.karya?.map { k ->
                    MediaGridItem(
                        id = k.idKarya,
                        title = k.judul,
                        imageUrl = k.fileUrl ?: k.file,
                    )
                } ?: emptyList()
            }

            val pameranItems = remember(userDetail?.pameran) {
                userDetail?.pameran?.map { p ->
                    MediaGridItem(
                        id = p.idPameran.toString(),
                        title = p.judul,
                        imageUrl = p.banner,
                    )
                } ?: emptyList()
            }

            val currentItems = if (selectedTab == ProfileTab.Posts) karyaItems else pameranItems

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                // Header Profil Pengguna
                item(span = { GridItemSpan(3) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.bg)
                            .padding(horizontal = VexSpace.s6, vertical = VexSpace.s4),
                    ) {
                        // Avatar, Nama & Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Avatar(
                                initials = userInfo.namaPanggilan ?: userInfo.nama ?: "U",
                                size = AvatarSize.Lg,
                                imageUrl = userInfo.fotoProfil,
                                contentDescription = userInfo.nama,
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = VexSpace.s5),
                            ) {
                                Text(
                                    text = userInfo.nama ?: "",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = colors.text,
                                    maxLines = 2,
                                )

                                Spacer(modifier = Modifier.height(VexSpace.s3))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(VexSpace.s8),
                                ) {
                                    Column {
                                        Text(
                                            text = "${userInfo.postCount}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = colors.text,
                                        )
                                        Text(
                                            text = stringResource(R.string.profile_stat_posts),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.textMuted,
                                        )
                                    }

                                    Column {
                                        Text(
                                            text = "${userInfo.exhibitCount}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = colors.text,
                                        )
                                        Text(
                                            text = stringResource(R.string.profile_stat_exhibits),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = colors.textMuted,
                                        )
                                    }
                                }
                            }
                        }

                        // Skill / Kategori Minat
                        if (userInfo.kategori.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(VexSpace.s4))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(VexSpace.s2),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                userInfo.kategori.take(4).forEach { kat ->
                                    val catColor = CategoryColorMap[kat.nama]
                                        ?: CategoryColorMap["Others"]
                                        ?: colors.primary
                                    VexSkillChip(
                                        text = kat.nama,
                                        categoryColor = catColor,
                                        onClick = {},
                                    )
                                }
                            }
                        }

                        // Bio
                        if (!userInfo.deskripsi.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(VexSpace.s4))
                            Text(
                                text = userInfo.deskripsi,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.text,
                            )
                        }

                        Spacer(modifier = Modifier.height(VexSpace.s4))

                        // Tab Switcher (Posts & Exhibits)
                        VexIconTabs(
                            tabs = listOf(ProfileTab.Posts, ProfileTab.Exhibits),
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it },
                            iconFor = { tab, isSelected ->
                                when (tab) {
                                    ProfileTab.Posts -> if (isSelected) VexIcons.Tab.Posts.filled else VexIcons.Tab.Posts.outline
                                    ProfileTab.Exhibits -> if (isSelected) VexIcons.Tab.Exhibits.filled else VexIcons.Tab.Exhibits.outline
                                }
                            },
                            contentDescriptionFor = { tab ->
                                when (tab) {
                                    ProfileTab.Posts -> stringResource(R.string.profile_tab_posts)
                                    ProfileTab.Exhibits -> stringResource(R.string.profile_tab_exhibits)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                // Grid Karya atau Pameran
                if (currentItems.isNotEmpty()) {
                    items(
                        items = currentItems,
                        key = { it.id },
                    ) { item ->
                        MediaGridTile(
                            item = item,
                            onClick = {
                                if (selectedTab == ProfileTab.Posts) {
                                    onMediaClick(item)
                                } else {
                                    onExhibitClick(item.id)
                                }
                            },
                        )
                    }
                } else {
                    item(span = { GridItemSpan(3) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = if (selectedTab == ProfileTab.Posts) {
                                    stringResource(R.string.profile_empty_posts)
                                } else {
                                    stringResource(R.string.profile_empty_exhibits)
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textMuted,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}
