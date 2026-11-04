package com.ven.app.ui.screens.settings

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ven.app.R
import com.ven.app.data.api.SessionManager
import com.ven.app.ui.theme.VexTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/**
 * Menyiapkan data profil dari sesi dan menghubungkannya ke [ProfileSettingsScreen].
 */
@Composable
fun ProfileSettingsRoute(
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    onSessionExpired: () -> Unit,
    viewModel: ProfileSettingsViewModel = viewModel(),
) {
    val context = LocalContext.current
    val sesi = remember { SessionManager.getInstance(context) }
    val penggunaAwal = remember { sesi.getUser() }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pengguna = state.pengguna ?: penggunaAwal

    var fotoBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(state.pengguna) {
        state.pengguna?.let(sesi::updateUser)
    }

    LaunchedEffect(state.fotoProfilUrl ?: pengguna?.fotoProfil) {
        val url = state.fotoProfilUrl ?: pengguna?.fotoProfil
        if (url != null && fotoBitmap == null) {
            fotoBitmap = withContext(Dispatchers.IO) {
                runCatching {
                    val koneksi = URL(url).openConnection() as HttpURLConnection
                    koneksi.connectTimeout = 5000
                    koneksi.readTimeout = 5000
                    koneksi.inputStream.use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
                }.getOrNull()
            }
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            val mimeType = (context.contentResolver.getType(uri) ?: "image/jpeg").lowercase()
            val mimeDiizinkan = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")

            if (mimeType !in mimeDiizinkan) {
                viewModel.tampilkanGagal(context.getString(R.string.settings_photo_invalid))
            } else {
                val ekstensi = when (mimeType) {
                    "image/png" -> "png"
                    "image/webp" -> "webp"
                    else -> "jpg"
                }
                val file = File(
                    context.cacheDir,
                    "foto_profil_${System.currentTimeMillis()}.$ekstensi",
                )
                context.contentResolver.openInputStream(uri)?.use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }

                if (file.length() > 5L * 1024 * 1024) {
                    file.delete()
                    viewModel.tampilkanGagal(context.getString(R.string.settings_photo_too_large))
                } else {
                    fotoBitmap = BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                    viewModel.uploadFoto(file, mimeType)
                }
            }
        }
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onSaved()
        }
    }

    LaunchedEffect(state.isSessionExpired) {
        if (state.isSessionExpired) {
            sesi.clearSession()
            onSessionExpired()
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

    val initialData = remember(state.selectedIds, pengguna) {
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
        isUploadingPhoto = state.isUploadingPhoto,
        errorMessage = state.errorMessage,
        onRetryClick = viewModel::muat,
        fotoProfilUrl = state.fotoProfilUrl ?: pengguna?.fotoProfil,
        fotoBitmap = fotoBitmap,
        onPhotoClick = {
            photoPicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        },
    )
}
