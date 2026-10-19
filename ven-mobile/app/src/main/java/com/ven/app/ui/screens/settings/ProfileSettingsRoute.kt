package com.ven.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ven.app.data.api.SessionManager
import com.ven.app.ui.theme.VexTheme

/**
 * Menyiapkan data profil dari sesi dan menghubungkannya ke [ProfileSettingsScreen].
 */
@Composable
fun ProfileSettingsRoute(
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    viewModel: ProfileSettingsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val pengguna = remember { SessionManager.getInstance(context).getUser() }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onSaved()
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VexTheme.colors.bg),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = VexTheme.colors.primary)
        }
        return
    }

    val initialData = remember(state.selectedIds) {
        ProfileSettingsData(
            username = pengguna?.namaPanggilan.orEmpty(),
            name = pengguna?.nama.orEmpty(),
            phone = pengguna?.nomorHp.orEmpty(),
            description = pengguna?.deskripsi.orEmpty(),
            interestIds = state.selectedIds,
        )
    }

    ProfileSettingsScreen(
        initialData = initialData,
        categories = state.categories,
        onBackClick = onBackClick,
        onSaveProfile = viewModel::simpan,
        isSaving = state.isSaving,
        errorMessage = state.errorMessage,
        onRetryClick = viewModel::muat,
    )
}
