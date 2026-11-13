package com.ven.app.ui.screens.schedule

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable

enum class ScheduleTab {
    Approved,
    Waiting,
    Request,
}

@Immutable
data class ScheduleEventItem(
    val id: String,
    val title: String,
    val dateText: String,
    val timeText: String,
    val fullDateFormatted: String,
    @param:DrawableRes val bannerRes: Int,
    val isApproved: Boolean = true,
)

@Immutable
data class ScheduleRequestItem(
    val id: String,
    val requesterName: String,
    val dateText: String,
    val timeText: String,
    val fullDateFormatted: String,
    val note: String,
    @param:DrawableRes val bannerRes: Int,
    @param:DrawableRes val requesterAvatarRes: Int? = null,
)

@Immutable
data class ScheduleUiState(
    val selectedTab: ScheduleTab = ScheduleTab.Approved,
    val searchQuery: String = "",
    val approvedItems: List<ScheduleEventItem> = emptyList(),
    val waitingItems: List<ScheduleEventItem> = emptyList(),
    val requestItems: List<ScheduleRequestItem> = emptyList(),
    val activeEventDetail: ScheduleEventItem? = null,
    val activeRequestDetail: ScheduleRequestItem? = null,
    val userFeedbackMessage: String? = null,
) {
    val filteredApprovedItems: List<ScheduleEventItem>
        get() = if (searchQuery.isBlank()) {
            approvedItems
        } else {
            val q = searchQuery.trim().lowercase()
            approvedItems.filter {
                it.title.lowercase().contains(q) ||
                    it.dateText.lowercase().contains(q) ||
                    it.timeText.lowercase().contains(q)
            }
        }

    val filteredWaitingItems: List<ScheduleEventItem>
        get() = if (searchQuery.isBlank()) {
            waitingItems
        } else {
            val q = searchQuery.trim().lowercase()
            waitingItems.filter {
                it.title.lowercase().contains(q) ||
                    it.dateText.lowercase().contains(q) ||
                    it.timeText.lowercase().contains(q)
            }
        }

    val filteredRequestItems: List<ScheduleRequestItem>
        get() = if (searchQuery.isBlank()) {
            requestItems
        } else {
            val q = searchQuery.trim().lowercase()
            requestItems.filter {
                it.requesterName.lowercase().contains(q) ||
                    it.note.lowercase().contains(q) ||
                    it.dateText.lowercase().contains(q) ||
                    it.timeText.lowercase().contains(q)
            }
        }
}
