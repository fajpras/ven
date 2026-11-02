package com.ven.app.ui.screens.settings

import androidx.compose.runtime.Immutable
import com.ven.app.ui.components.feature.HistoryEntry
import com.ven.app.ui.theme.ThemeMode

/**
 * Model data profil untuk layar pengaturan profil pengguna.
 */
@Immutable
data class ProfileSettingsData(
    val username: String = "",
    val name: String = "",
    val phone: String = "",
    val description: String = "",
    val interestIds: Set<Int> = emptySet(),
)

/**
 * UI State untuk seluruh modul Settings.
 */
@Immutable
data class SettingsUiState(
    val profileData: ProfileSettingsData = ProfileSettingsData(),
    val themeMode: ThemeMode = ThemeMode.Dark,
    val selectedLanguage: String = "English",
    val isAccountDisabled: Boolean = false,
    val historyEntries: List<HistoryEntry> = emptyList(),
    val selectedHistoryEntry: HistoryEntry? = null,
    val isLogoutDialogOpen: Boolean = false,
)
