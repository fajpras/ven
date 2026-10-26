package com.ven.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ven.app.R
import com.ven.app.ui.components.layout.VexTopBar
import com.ven.app.ui.components.ui.VexListRow
import com.ven.app.ui.components.ui.VexSectionLabel
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Halaman utama Settings and activity.
 * Sesuai wireframe dan DESIGN.md §8.8:
 * - Mengelompokkan pengaturan ke dalam 4 seksi: Account, Interface, Security & Others, Login.
 * - Logout menampilkan dialog konfirmasi.
 */
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChangeEmail: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToAccountStatus: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToTheme: () -> Unit,
    onNavigateToLanguage: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onLogoutConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.bg,
        topBar = {
            VexTopBar.Back(
                title = stringResource(R.string.settings_activity_title),
                onBackClick = onBackClick,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.bg)
                .verticalScroll(rememberScrollState())
                .padding(bottom = VexSpace.s8),
        ) {
            // ---------------------------------------------------------
            // Seksi 1: Account
            // ---------------------------------------------------------
            VexSectionLabel(title = stringResource(R.string.settings_section_account))

            VexListRow(
                icon = R.drawable.ic_settings_profile,
                label = stringResource(R.string.settings_item_profile),
                onClick = onNavigateToProfile,
            )
            VexListRow(
                icon = R.drawable.ic_settings_email,
                label = stringResource(R.string.settings_item_change_email),
                onClick = onNavigateToChangeEmail,
            )
            VexListRow(
                icon = R.drawable.ic_settings_password,
                label = stringResource(R.string.settings_item_change_password),
                onClick = onNavigateToChangePassword,
            )
            VexListRow(
                icon = R.drawable.ic_settings_account_status,
                label = stringResource(R.string.settings_item_account_status),
                onClick = onNavigateToAccountStatus,
            )
            VexListRow(
                icon = R.drawable.ic_settings_history,
                label = stringResource(R.string.settings_item_history),
                onClick = onNavigateToHistory,
            )

            // ---------------------------------------------------------
            // Seksi 2: Interface
            // ---------------------------------------------------------
            VexSectionLabel(title = stringResource(R.string.settings_section_interface))

            VexListRow(
                icon = R.drawable.ic_settings_theme,
                label = stringResource(R.string.settings_item_theme),
                onClick = onNavigateToTheme,
            )
            VexListRow(
                icon = R.drawable.ic_settings_language,
                label = stringResource(R.string.settings_item_language),
                onClick = onNavigateToLanguage,
            )

            // ---------------------------------------------------------
            // Seksi 3: Security & Others
            // ---------------------------------------------------------
            VexSectionLabel(title = stringResource(R.string.settings_section_security_others))

            VexListRow(
                icon = R.drawable.ic_settings_privacy,
                label = stringResource(R.string.settings_item_privacy_policy),
                onClick = onNavigateToPrivacyPolicy,
            )
            VexListRow(
                icon = R.drawable.ic_settings_help,
                label = stringResource(R.string.settings_item_help),
                onClick = onNavigateToHelp,
            )

            // ---------------------------------------------------------
            // Seksi 4: Login
            // ---------------------------------------------------------
            VexSectionLabel(title = stringResource(R.string.settings_section_login))

            VexListRow(
                icon = R.drawable.ic_settings_logout,
                label = stringResource(R.string.settings_item_logout),
                isDanger = true,
                onClick = { showLogoutDialog = true },
            )
        }

        // Dialog Konfirmasi Logout
        if (showLogoutDialog) {
            LogoutConfirmationDialog(
                onConfirm = {
                    showLogoutDialog = false
                    onLogoutConfirmed()
                },
                onDismiss = { showLogoutDialog = false },
            )
        }
    }
}

/**
 * Dialog konfirmasi logout dengan frame yang rapi, simetris, dan presisi.
 * Menggunakan radius 28dp (VexRadius.xl), kontainer colors.surface,
 * dan garis outline border 1dp colors.border yang elegan.
 */
@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors
    val dialogShape = RoundedCornerShape(VexRadius.xl)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.border(
            width = 1.dp,
            color = colors.border,
            shape = dialogShape,
        ),
        shape = dialogShape,
        containerColor = colors.surface,
        tonalElevation = 0.dp,
        titleContentColor = colors.text,
        textContentColor = colors.textSecondary,
        title = {
            Text(
                text = stringResource(R.string.settings_logout_dialog_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.settings_logout_dialog_msg),
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
            ) {
                Text(
                    text = stringResource(R.string.settings_logout_confirm),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.danger,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(
                    text = stringResource(R.string.settings_logout_cancel),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = colors.text,
                )
            }
        },
    )
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

@Preview(name = "Settings - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun SettingsDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        SettingsScreen(
            onBackClick = {},
            onNavigateToProfile = {},
            onNavigateToChangeEmail = {},
            onNavigateToChangePassword = {},
            onNavigateToAccountStatus = {},
            onNavigateToHistory = {},
            onNavigateToTheme = {},
            onNavigateToLanguage = {},
            onNavigateToPrivacyPolicy = {},
            onNavigateToHelp = {},
            onLogoutConfirmed = {},
        )
    }
}

@Preview(name = "Settings - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SettingsLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        SettingsScreen(
            onBackClick = {},
            onNavigateToProfile = {},
            onNavigateToChangeEmail = {},
            onNavigateToChangePassword = {},
            onNavigateToAccountStatus = {},
            onNavigateToHistory = {},
            onNavigateToTheme = {},
            onNavigateToLanguage = {},
            onNavigateToPrivacyPolicy = {},
            onNavigateToHelp = {},
            onLogoutConfirmed = {},
        )
    }
}

@Preview(name = "Logout Dialog - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun LogoutDialogDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        LogoutConfirmationDialog(
            onConfirm = {},
            onDismiss = {},
        )
    }
}

@Preview(name = "Logout Dialog - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun LogoutDialogLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        LogoutConfirmationDialog(
            onConfirm = {},
            onDismiss = {},
        )
    }
}
