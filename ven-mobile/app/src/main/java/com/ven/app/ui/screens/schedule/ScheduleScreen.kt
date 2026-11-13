package com.ven.app.ui.screens.schedule

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.components.feature.EventListItem
import com.ven.app.ui.components.feature.RequestListItem
import com.ven.app.ui.components.layout.AppScaffold
import com.ven.app.ui.components.ui.SearchBar
import com.ven.app.ui.components.ui.VexTextTabs
import com.ven.app.ui.navigation.BottomNavTab
import com.ven.app.ui.screens.schedule.components.EventDetailSheet
import com.ven.app.ui.screens.schedule.components.RequestDetailSheet
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier,
    initialUiState: ScheduleUiState = dummyScheduleUiState,
) {
    val context = LocalContext.current
    var uiState by remember { mutableStateOf(initialUiState) }
    var isRefreshing by remember { mutableStateOf(false) }

    AppScaffold(
        currentTab = BottomNavTab.Schedule,
        onTabSelected = onTabSelected,
        modifier = modifier,
    ) { innerPadding ->
        ScheduleContent(
            uiState = uiState,
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                uiState = dummyScheduleUiState
                isRefreshing = false
            },
            onTabSelected = { tab ->
                uiState = uiState.copy(selectedTab = tab)
            },
            onSearchQueryChange = { query ->
                uiState = uiState.copy(searchQuery = query)
            },
            onEventClick = { event ->
                uiState = uiState.copy(activeEventDetail = event)
            },
            onRequestClick = { request ->
                uiState = uiState.copy(activeRequestDetail = request)
            },
            onDismissDetail = {
                uiState = uiState.copy(
                    activeEventDetail = null,
                    activeRequestDetail = null,
                )
            },
            onJoinRoom = { event ->
                Toast.makeText(
                    context,
                    context.getString(R.string.schedule_action_join_room_sim),
                    Toast.LENGTH_SHORT,
                ).show()
                uiState = uiState.copy(activeEventDetail = null)
            },
            onApproveRequest = { request ->
                val approvedEvent = ScheduleEventItem(
                    id = "approved_${request.id}",
                    title = request.requesterName,
                    dateText = request.dateText,
                    timeText = request.timeText,
                    fullDateFormatted = request.fullDateFormatted,
                    bannerRes = request.bannerRes,
                    isApproved = true,
                )
                val updatedRequests = uiState.requestItems.filterNot { it.id == request.id }
                val updatedApproved = listOf(approvedEvent) + uiState.approvedItems

                uiState = uiState.copy(
                    requestItems = updatedRequests,
                    approvedItems = updatedApproved,
                    activeRequestDetail = null,
                )

                Toast.makeText(
                    context,
                    context.getString(R.string.schedule_action_approved_success),
                    Toast.LENGTH_SHORT,
                ).show()
            },
            onRejectRequest = { request ->
                val updatedRequests = uiState.requestItems.filterNot { it.id == request.id }

                uiState = uiState.copy(
                    requestItems = updatedRequests,
                    activeRequestDetail = null,
                )

                Toast.makeText(
                    context,
                    context.getString(R.string.schedule_action_rejected_success),
                    Toast.LENGTH_SHORT,
                ).show()
            },
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleContent(
    uiState: ScheduleUiState,
    onTabSelected: (ScheduleTab) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEventClick: (ScheduleEventItem) -> Unit,
    onRequestClick: (ScheduleRequestItem) -> Unit,
    onDismissDetail: () -> Unit,
    onJoinRoom: (ScheduleEventItem) -> Unit,
    onApproveRequest: (ScheduleRequestItem) -> Unit,
    onRejectRequest: (ScheduleRequestItem) -> Unit,
    modifier: Modifier = Modifier,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
) {
    val colors = VexTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bg)
            .statusBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = VexSpace.s5)
                .padding(top = VexSpace.s4, bottom = VexSpace.s4),
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = onSearchQueryChange,
            )
        }

        VexTextTabs(
            tabs = ScheduleTab.entries,
            selectedTab = uiState.selectedTab,
            onTabSelected = onTabSelected,
            labelFor = { tab ->
                when (tab) {
                    ScheduleTab.Approved -> stringResource(R.string.schedule_tab_approved)
                    ScheduleTab.Waiting -> stringResource(R.string.schedule_tab_waiting)
                    ScheduleTab.Request -> stringResource(R.string.schedule_tab_request)
                }
            },
        )

        HorizontalDivider(
            thickness = 1.dp,
            color = colors.border,
        )

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = VexSpace.s2),
            ) {
                when (uiState.selectedTab) {
                    ScheduleTab.Approved -> {
                        val items = uiState.filteredApprovedItems
                        if (items.isEmpty()) {
                            item {
                                EmptyScheduleView(
                                    message = stringResource(R.string.schedule_empty_approved),
                                )
                            }
                        } else {
                            items(items, key = { it.id }) { item ->
                                EventListItem(
                                    entry = item,
                                    onClick = { onEventClick(item) },
                                )
                            }
                        }
                    }

                    ScheduleTab.Waiting -> {
                        val items = uiState.filteredWaitingItems
                        if (items.isEmpty()) {
                            item {
                                EmptyScheduleView(
                                    message = stringResource(R.string.schedule_empty_waiting),
                                )
                            }
                        } else {
                            items(items, key = { it.id }) { item ->
                                EventListItem(
                                    entry = item,
                                    onClick = { onEventClick(item) },
                                )
                            }
                        }
                    }

                    ScheduleTab.Request -> {
                        val items = uiState.filteredRequestItems
                        if (items.isEmpty()) {
                            item {
                                EmptyScheduleView(
                                    message = stringResource(R.string.schedule_empty_request),
                                )
                            }
                        } else {
                            items(items, key = { it.id }) { item ->
                                RequestListItem(
                                    entry = item,
                                    onClick = { onRequestClick(item) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    uiState.activeEventDetail?.let { event ->
        EventDetailSheet(
            event = event,
            onDismissRequest = onDismissDetail,
            onJoinRoom = { onJoinRoom(event) },
        )
    }

    uiState.activeRequestDetail?.let { request ->
        RequestDetailSheet(
            request = request,
            onDismissRequest = onDismissDetail,
            onApprove = { onApproveRequest(request) },
            onReject = { onRejectRequest(request) },
        )
    }
}

@Composable
private fun EmptyScheduleView(
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

@Preview(name = "ScheduleScreen Approved - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun ScheduleScreenApprovedDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        ScheduleContent(
            uiState = dummyScheduleUiState,
            onTabSelected = {},
            onSearchQueryChange = {},
            onEventClick = {},
            onRequestClick = {},
            onDismissDetail = {},
            onJoinRoom = {},
            onApproveRequest = {},
            onRejectRequest = {},
        )
    }
}

@Preview(name = "ScheduleScreen Request - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun ScheduleScreenRequestDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        ScheduleContent(
            uiState = dummyScheduleUiState.copy(selectedTab = ScheduleTab.Request),
            onTabSelected = {},
            onSearchQueryChange = {},
            onEventClick = {},
            onRequestClick = {},
            onDismissDetail = {},
            onJoinRoom = {},
            onApproveRequest = {},
            onRejectRequest = {},
        )
    }
}
