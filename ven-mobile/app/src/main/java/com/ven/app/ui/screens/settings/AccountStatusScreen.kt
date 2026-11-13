package com.ven.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ven.app.R
import com.ven.app.ui.components.layout.VexBottomActionBar
import com.ven.app.ui.components.layout.VexTopBar
import com.ven.app.ui.components.ui.VexButton
import com.ven.app.ui.components.ui.VexButtonVariant
import com.ven.app.ui.components.ui.VexPasswordField
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexRadius
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Halaman Account Status (UC-04: Mengelola akun).
 * - Pilihan "Deaktivasi akun sementara".
 * - Form konfirmasi: kata sandi + persetujuan ketentuan.
 * - Dialog konfirmasi "Ya" sebelum menonaktifkan akun.
 */
@Composable
fun AccountStatusScreen(
    state: AccountStatusUiState,
    isConfirmDialogOpen: Boolean,
    onKataSandiChange: (String) -> Unit,
    onSetujuChange: (Boolean) -> Unit,
    onBackClick: () -> Unit,
    onDeactivateClick: () -> Unit,
    onConfirmDeactivate: () -> Unit,
    onDismissDialog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    if (isConfirmDialogOpen) {
        DeactivationConfirmDialog(
            onConfirm = onConfirmDeactivate,
            onDismiss = onDismissDialog,
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.bg,
        topBar = {
            VexTopBar.Back(
                title = stringResource(R.string.settings_item_account_status),
                onBackClick = onBackClick,
            )
        },
        bottomBar = {
            VexBottomActionBar {
                VexButton(
                    text = stringResource(R.string.settings_deactivate_button),
                    onClick = onDeactivateClick,
                    variant = VexButtonVariant.Primary,
                    enabled = state.isFormValid,
                    loading = state.isLoading,
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(colors.bg)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = VexSpace.s6, vertical = VexSpace.s5)
                .padding(bottom = VexSpace.s8),
        ) {
            Text(
                text = stringResource(R.string.settings_disable_account_title),
                style = MaterialTheme.typography.titleMedium,
                color = colors.text,
            )

            Spacer(modifier = Modifier.height(VexSpace.s2))

            Text(
                text = stringResource(R.string.settings_disable_account_desc),
                style = MaterialTheme.typography.bodySmall,
                color = colors.textMuted,
            )

            Spacer(modifier = Modifier.height(VexSpace.s5))

            VexPasswordField(
                value = state.kataSandi,
                onValueChange = onKataSandiChange,
                label = stringResource(R.string.settings_current_password_label),
            )

            Spacer(modifier = Modifier.height(VexSpace.s4))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSetujuChange(!state.setujuKetentuan) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = state.setujuKetentuan,
                    onCheckedChange = onSetujuChange,
                    colors = CheckboxDefaults.colors(checkedColor = colors.primary),
                )
                Spacer(modifier = Modifier.width(VexSpace.s2))
                Text(
                    text = stringResource(R.string.settings_deactivate_terms),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.text,
                )
            }

            state.errorMessage?.let {
                Spacer(modifier = Modifier.height(VexSpace.s3))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.danger,
                )
            }
        }
    }
}

@Composable
private fun DeactivationConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = VexTheme.colors
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(VexRadius.xl),
        containerColor = colors.surface,
        title = {
            Text(
                text = stringResource(R.string.settings_deactivate_confirm_title),
                style = MaterialTheme.typography.titleLarge,
                color = colors.text,
            )
        },
        text = {
            Text(
                text = stringResource(R.string.settings_deactivate_confirm_msg),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
            )
        },
        confirmButton = {
            VexButton(
                text = stringResource(R.string.settings_deactivate_confirm_yes),
                onClick = onConfirm,
                variant = VexButtonVariant.Primary,
            )
        },
        dismissButton = {
            VexButton(
                text = stringResource(R.string.settings_deactivate_confirm_no),
                onClick = onDismiss,
                variant = VexButtonVariant.Outline,
            )
        },
    )
}

/**
 * Menghubungkan [AccountStatusScreen] dengan [AccountStatusViewModel].
 * Setelah sukses, memicu [onDeactivated] (hapus sesi + arahkan ke login).
 */
@Composable
fun AccountStatusRoute(
    onBackClick: () -> Unit,
    onDeactivated: () -> Unit,
    viewModel: AccountStatusViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var isDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onDeactivated()
        }
    }

    AccountStatusScreen(
        state = state,
        isConfirmDialogOpen = isDialogOpen,
        onKataSandiChange = viewModel::onKataSandiChange,
        onSetujuChange = viewModel::onSetujuChange,
        onBackClick = onBackClick,
        onDeactivateClick = { isDialogOpen = true },
        onConfirmDeactivate = {
            isDialogOpen = false
            viewModel.deaktivasi()
        },
        onDismissDialog = {
            isDialogOpen = false
            viewModel.reset()
        },
    )
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

@Preview(name = "AccountStatus - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun AccountStatusDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        AccountStatusScreen(
            state = AccountStatusUiState(),
            isConfirmDialogOpen = false,
            onKataSandiChange = {},
            onSetujuChange = {},
            onBackClick = {},
            onDeactivateClick = {},
            onConfirmDeactivate = {},
            onDismissDialog = {},
        )
    }
}

@Preview(name = "AccountStatus - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AccountStatusLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        AccountStatusScreen(
            state = AccountStatusUiState(),
            isConfirmDialogOpen = false,
            onKataSandiChange = {},
            onSetujuChange = {},
            onBackClick = {},
            onDeactivateClick = {},
            onConfirmDeactivate = {},
            onDismissDialog = {},
        )
    }
}
