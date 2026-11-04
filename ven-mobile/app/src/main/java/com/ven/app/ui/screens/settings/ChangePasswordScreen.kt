package com.ven.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ven.app.R
import com.ven.app.ui.components.layout.VexBottomActionBar
import com.ven.app.ui.components.layout.VexTopBar
import com.ven.app.ui.components.ui.VexButton
import com.ven.app.ui.components.ui.VexButtonVariant
import com.ven.app.ui.components.ui.VexPasswordField
import com.ven.app.ui.theme.ThemeMode
import com.ven.app.ui.theme.VexSpace
import com.ven.app.ui.theme.VexTheme

/**
 * Halaman Change Password.
 * Sesuai wireframe `Wireframe - Setting Password.png` dan DESIGN.md §8.10:
 * - Field: Current password, New password, Confirm password.
 * - Sticky bottom CTA: [VexBottomActionBar].
 */
@Composable
fun ChangePasswordScreen(
    state: ChangePasswordUiState,
    onCurrentPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = VexTheme.colors

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.bg,
        topBar = {
            VexTopBar.Back(
                title = stringResource(R.string.settings_item_change_password),
                onBackClick = onBackClick,
            )
        },
        bottomBar = {
            VexBottomActionBar {
                VexButton(
                    text = stringResource(R.string.settings_save_changes),
                    onClick = onSubmitClick,
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
            VexPasswordField(
                value = state.kataSandiSaatIni,
                onValueChange = onCurrentPasswordChange,
                label = stringResource(R.string.settings_current_password_label),
            )

            Spacer(modifier = Modifier.height(VexSpace.s4))

            VexPasswordField(
                value = state.kataSandi,
                onValueChange = onNewPasswordChange,
                label = stringResource(R.string.settings_new_password_label),
            )

            Spacer(modifier = Modifier.height(VexSpace.s4))

            VexPasswordField(
                value = state.konfirmasiKataSandi,
                onValueChange = onConfirmPasswordChange,
                label = stringResource(R.string.settings_confirm_password_label),
            )

            state.errorMessage?.let {
                Spacer(modifier = Modifier.height(VexSpace.s3))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.danger,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/**
 * Menghubungkan [ChangePasswordScreen] dengan [ChangePasswordViewModel].
 */
@Composable
fun ChangePasswordRoute(
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ChangePasswordViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onSaved()
        }
    }

    ChangePasswordScreen(
        state = state,
        onCurrentPasswordChange = viewModel::onKataSandiSaatIniChange,
        onNewPasswordChange = viewModel::onKataSandiChange,
        onConfirmPasswordChange = viewModel::onKonfirmasiChange,
        onBackClick = onBackClick,
        onSubmitClick = viewModel::simpan,
    )
}

// ---------------------------------------------------------
// Previews
// ---------------------------------------------------------

@Preview(name = "ChangePassword - Dark Mode", showBackground = true, backgroundColor = 0xFF1F1F1F)
@Composable
private fun ChangePasswordDarkPreview() {
    VexTheme(mode = ThemeMode.Dark) {
        ChangePasswordScreen(
            state = ChangePasswordUiState(),
            onCurrentPasswordChange = {},
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onBackClick = {},
            onSubmitClick = {},
        )
    }
}

@Preview(name = "ChangePassword - Light Mode", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ChangePasswordLightPreview() {
    VexTheme(mode = ThemeMode.Light) {
        ChangePasswordScreen(
            state = ChangePasswordUiState(),
            onCurrentPasswordChange = {},
            onNewPasswordChange = {},
            onConfirmPasswordChange = {},
            onBackClick = {},
            onSubmitClick = {},
        )
    }
}
