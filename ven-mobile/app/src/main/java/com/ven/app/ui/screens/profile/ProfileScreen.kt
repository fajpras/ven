package com.ven.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.components.feature.MediaGridItem
import com.ven.app.ui.components.feature.MediaGridTile
import com.ven.app.ui.components.layout.AppScaffold
import com.ven.app.ui.components.layout.VexTopBar
import com.ven.app.ui.components.ui.Avatar
import com.ven.app.ui.components.ui.AvatarSize
import com.ven.app.ui.components.ui.VexBottomSheet
import com.ven.app.ui.components.ui.VexCounterChip
import com.ven.app.ui.components.ui.VexIconTabs
import com.ven.app.ui.components.ui.VexSkillChip
import com.ven.app.ui.icons.VexIcons
import com.ven.app.ui.navigation.BottomNavTab
import com.ven.app.ui.theme.CategoryColorMap
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexAspect
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onTabSelected: (BottomNavTab) -> Unit,
    onCreateClick: () -> Unit,
    onMenuClick: () -> Unit,
    onProfileTabChange: (ProfileTab) -> Unit,
    onSkillSheetToggle: (Boolean) -> Unit,
    onMediaClick: (MediaGridItem) -> Unit,
    modifier: Modifier = Modifier,
    onRetryClick: () -> Unit = {},
) {
    val colors = VexTheme.colors
    val profile = uiState.profile

    AppScaffold(
        currentTab = BottomNavTab.Profile,
        onTabSelected = onTabSelected,
        modifier = modifier,
        topBar = {
            VexTopBar.Profile(
                username = profile?.username.orEmpty(),
                onCreateClick = onCreateClick,
                onMenuClick = onMenuClick,
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }
            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = VexSpace.s6),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = stringResource(R.string.profile_error),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.text,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(VexSpace.s4))
                    Button(
                        onClick = onRetryClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary,
                        ),
                    ) {
                        Text(text = stringResource(R.string.profile_retry))
                    }
                }
            }
            profile != null -> {
                val currentMediaItems = when (uiState.selectedTab) {
                    ProfileTab.Posts -> uiState.posts
                    ProfileTab.Exhibits -> uiState.exhibits
                }

                PullToRefreshBox(
                    isRefreshing = uiState.isLoading,
                    onRefresh = onRetryClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        contentPadding = PaddingValues(bottom = VexSpace.s6),
                    ) {
                    // -----------------------------------------------------
                    // Item Span Penuh: Header Profil, Bio, Kontak & Tabs
                    // -----------------------------------------------------
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colors.bg)
                                .padding(horizontal = VexSpace.s6, vertical = VexSpace.s4),
                        ) {
                            // Baris 1: Avatar 96dp + Nama Lengkap & Stats
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Avatar(
                                    initials = profile.username,
                                    size = AvatarSize.Lg,
                                    imageRes = profile.avatarRes,
                                    imageBitmap = uiState.avatarBitmap,
                                    contentDescription = profile.fullName,
                                )

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = VexSpace.s5),
                                ) {
                                    Text(
                                        text = profile.fullName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                        ),
                                        color = colors.text,
                                        maxLines = 2,
                                    )

                                    Spacer(modifier = Modifier.height(VexSpace.s3))

                                    // Stat Block: posts & exhibits
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(VexSpace.s8),
                                    ) {
                                        Column {
                                            Text(
                                                text = "${profile.postCount}",
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
                                                text = "${profile.exhibitCount}",
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

                            Spacer(modifier = Modifier.height(VexSpace.s5))

                            // Baris 2: Skill Chips (Maks 4 + chip "+N")
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(VexSpace.s2),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                uiState.visibleSkills.forEach { skill ->
                                    val catColor = CategoryColorMap[skill.label]
                                        ?: CategoryColorMap["Others"]
                                        ?: colors.primary

                                    VexSkillChip(
                                        text = skill.label,
                                        categoryColor = catColor,
                                        onClick = { onSkillSheetToggle(true) },
                                    )
                                }

                                if (uiState.extraSkillCount > 0) {
                                    VexCounterChip(
                                        count = uiState.extraSkillCount,
                                        onClick = { onSkillSheetToggle(true) },
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(VexSpace.s4))

                            // Baris 3: Bio Pengguna
                            if (profile.bio.isNotBlank()) {
                                Text(
                                    text = profile.bio,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.text,
                                )
                                Spacer(modifier = Modifier.height(VexSpace.s4))
                            }

                            // Baris 4: Email
                            if (profile.email.isNotBlank()) {
                                Text(
                                    text = stringResource(R.string.profile_email_label),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textMuted,
                                )
                                Text(
                                    text = profile.email,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colors.text,
                                )
                                Spacer(modifier = Modifier.height(VexSpace.s3))
                            }

                            // Baris 5: Phone
                            if (profile.phone.isNotBlank()) {
                                Text(
                                    text = stringResource(R.string.profile_phone_label),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.textMuted,
                                )
                                Text(
                                    text = profile.phone,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colors.text,
                                )
                                Spacer(modifier = Modifier.height(VexSpace.s4))
                            }

                            // Baris 6: Tab Switcher (Posts / Exhibits)
                            VexIconTabs(
                                tabs = listOf(ProfileTab.Posts, ProfileTab.Exhibits),
                                selectedTab = uiState.selectedTab,
                                onTabSelected = onProfileTabChange,
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
                            )
                        }
                    }

                    // -----------------------------------------------------
                    // Media Items (Poster Grid 3 Kolom untuk Posts, Full Banner 16:9 untuk Exhibits)
                    // -----------------------------------------------------
                    if (currentMediaItems.isNotEmpty()) {
                        val isExhibits = uiState.selectedTab == ProfileTab.Exhibits
                        items(
                            items = currentMediaItems,
                            key = { it.id },
                            span = {
                                if (isExhibits) GridItemSpan(maxLineSpan) else GridItemSpan(1)
                            },
                        ) { item ->
                            MediaGridTile(
                                item = item,
                                onClick = { onMediaClick(item) },
                                aspectRatio = if (isExhibits) (16f / 9f) else VexAspect.poster,
                                modifier = if (isExhibits) {
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 4.dp)
                                } else {
                                    Modifier
                                },
                            )
                        }
                    } else {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                val emptyText = when (uiState.selectedTab) {
                                    ProfileTab.Posts -> stringResource(R.string.profile_empty_posts)
                                    ProfileTab.Exhibits -> stringResource(R.string.profile_empty_exhibits)
                                }
                                Text(
                                    text = emptyText,
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

        if (uiState.isSkillSheetOpen) {
            VexBottomSheet(
                onDismissRequest = { onSkillSheetToggle(false) },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = VexSpace.s6)
                        .padding(bottom = VexSpace.s8)
                        .navigationBarsPadding(),
                ) {
                    Text(
                        text = stringResource(R.string.profile_skills_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = colors.text,
                        modifier = Modifier.padding(bottom = VexSpace.s4),
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(VexSpace.s2),
                        verticalArrangement = Arrangement.spacedBy(VexSpace.s2),
                    ) {
                        uiState.allSkills.forEach { skill ->
                            val catColor = CategoryColorMap[skill.label]
                                ?: CategoryColorMap["Others"]
                                ?: colors.primary

                            VexSkillChip(
                                text = skill.label,
                                categoryColor = catColor,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

@Preview(name = "Profile Screen - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun ProfileScreenDarkPreview() {
    var state by remember { mutableStateOf(dummyProfileUiState) }

    VexTheme(mode = ThemeMode.Dark) {
        ProfileScreen(
            uiState = state,
            onTabSelected = {},
            onCreateClick = {},
            onMenuClick = {},
            onProfileTabChange = { state = state.copy(selectedTab = it) },
            onSkillSheetToggle = { state = state.copy(isSkillSheetOpen = it) },
            onMediaClick = {},
        )
    }
}

@Preview(name = "Profile Screen - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ProfileScreenLightPreview() {
    var state by remember { mutableStateOf(dummyProfileUiState) }

    VexTheme(mode = ThemeMode.Light) {
        ProfileScreen(
            uiState = state,
            onTabSelected = {},
            onCreateClick = {},
            onMenuClick = {},
            onProfileTabChange = { state = state.copy(selectedTab = it) },
            onSkillSheetToggle = { state = state.copy(isSkillSheetOpen = it) },
            onMediaClick = {},
        )
    }
}
