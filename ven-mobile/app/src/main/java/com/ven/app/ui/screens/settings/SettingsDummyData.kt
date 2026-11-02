package com.ven.app.ui.screens.settings

import com.ven.app.R
import com.ven.app.ui.components.feature.HistoryEntry
import com.ven.app.ui.theme.ThemeMode

val dummySettingsProfile = ProfileSettingsData(
    username = "gra._.aph",
    name = "Terra Faqih Satria Madjid",
    phone = "082387593452",
    description = "I’m a fullstack game developer",
    interestIds = setOf(3, 6, 7, 10),
)

val dummyHistoryEntries = listOf(
    HistoryEntry(
        id = "h1",
        title = "Open Source Competition",
        dateText = "24th Sep 2026",
        timeText = "02.00 PM",
        fullDateFormatted = "Thursday, 24th September 2026",
        bannerRes = R.drawable.poster_1,
    ),
    HistoryEntry(
        id = "h2",
        title = "PBL EXPO 2026",
        dateText = "16th Oct 2026",
        timeText = "04.00 PM",
        fullDateFormatted = "Friday, 16th October 2026",
        bannerRes = R.drawable.poster_2,
    ),
)

val dummySettingsUiState = SettingsUiState(
    profileData = dummySettingsProfile,
    themeMode = ThemeMode.Dark,
    selectedLanguage = "English",
    isAccountDisabled = false,
    historyEntries = dummyHistoryEntries,
    selectedHistoryEntry = null,
    isLogoutDialogOpen = false,
)
