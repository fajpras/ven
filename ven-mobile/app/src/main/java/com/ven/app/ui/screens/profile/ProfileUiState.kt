package com.ven.app.ui.screens.profile

import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ven.app.data.api.ApiClient
import com.ven.app.data.api.ApiProfil
import com.ven.app.ui.components.feature.MediaGridItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

/**
 * Tab pilihan pada halaman Profil.
 */
enum class ProfileTab {
    Posts,
    Exhibits,
}

/**
 * Data keahlian / skill pengguna.
 */
@Immutable
data class Skill(
    val label: String,
)

/**
 * Data profil pengguna.
 */
@Immutable
data class Profile(
    val username: String,
    val fullName: String,
    val postCount: Int,
    val exhibitCount: Int,
    val skills: List<Skill>,
    val bio: String,
    val email: String,
    val phone: String,
    @param:DrawableRes val avatarRes: Int? = null,
)

/**
 * UI State untuk [ProfileScreen].
 * Mengikuti arsitektur MVI/MVVM dan Unidirectional Data Flow.
 */
@Immutable
data class ProfileUiState(
    val isLoading: Boolean = false,
    val isOwnProfile: Boolean = true,
    val profile: Profile? = null,
    val selectedTab: ProfileTab = ProfileTab.Posts,
    val posts: List<MediaGridItem> = emptyList(),
    val exhibits: List<MediaGridItem> = emptyList(),
    val isSkillSheetOpen: Boolean = false,
    val avatarBitmap: ImageBitmap? = null,
    val errorMessage: String? = null,
) {
    val visibleSkills: List<Skill>
        get() = profile?.skills?.take(MAX_VISIBLE_SKILLS).orEmpty()

    val extraSkillCount: Int
        get() = ((profile?.skills?.size ?: 0) - MAX_VISIBLE_SKILLS).coerceAtLeast(0)

    val allSkills: List<Skill>
        get() = profile?.skills.orEmpty()

    companion object {
        const val MAX_VISIBLE_SKILLS = 4
    }
}

/**
 * Memuat data profil pemilik akun beserta minat dan jumlah karyanya.
 */
class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    fun muat() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = it.profile == null, errorMessage = null)
            }

            val profil = ApiProfil.profil()
            if (profil.isFailure) {
                tampilkanGagal(profil.exceptionOrNull()?.message ?: "Gagal memuat profil")
                return@launch
            }

            val minat = ApiProfil.minat()
            if (minat.isFailure) {
                tampilkanGagal(minat.exceptionOrNull()?.message ?: "Gagal memuat minat")
                return@launch
            }

            val pengguna = profil.getOrThrow()
            val profile = Profile(
                username = pengguna.namaPanggilan.orEmpty(),
                fullName = pengguna.nama.orEmpty(),
                postCount = ApiProfil.jumlahKarya().getOrDefault(0),
                exhibitCount = ApiProfil.jumlahPameran().getOrDefault(0),
                skills = minat.getOrThrow().map { Skill(it.nama) },
                bio = pengguna.deskripsi.orEmpty(),
                email = pengguna.email.orEmpty(),
                phone = pengguna.nomorHp.orEmpty(),
            )

            _uiState.update {
                it.copy(
                    isLoading = false,
                    profile = profile,
                    posts = emptyList(),
                    exhibits = emptyList(),
                    errorMessage = null,
                )
            }

            muatAvatar(pengguna.fotoProfil)
        }
    }

    fun pilihTab(tab: ProfileTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun aturSkillSheet(buka: Boolean) {
        _uiState.update { it.copy(isSkillSheetOpen = buka) }
    }

    private fun tampilkanGagal(pesan: String) {
        _uiState.update {
            if (it.profile != null) {
                it.copy(isLoading = false, errorMessage = null)
            } else {
                it.copy(isLoading = false, errorMessage = pesan)
            }
        }
    }

    private suspend fun muatAvatar(url: String?) {
        if (url.isNullOrBlank()) return

        val bitmap = withContext(Dispatchers.IO) {
            runCatching {
                val koneksi = URL(normalisasiUrl(url)).openConnection() as HttpURLConnection
                koneksi.connectTimeout = 5000
                koneksi.readTimeout = 5000
                koneksi.inputStream.use {
                    BitmapFactory.decodeStream(it)?.asImageBitmap()
                }
            }.getOrNull()
        }

        _uiState.update { it.copy(avatarBitmap = bitmap) }
    }

    /**
     * URL foto bisa dibuat dari host request (mis. 127.0.0.1 saat diunggah
     * lewat curl). Dari emulator/perangkat, host tersebut harus mengikuti
     * alamat API yang sedang dipakai aplikasi.
     */
    private fun normalisasiUrl(url: String): String {
        val asal = runCatching { URI(url) }.getOrNull() ?: return url
        if (asal.host != "127.0.0.1" && asal.host != "localhost") return url

        val dasar = runCatching { URI(ApiClient.getBaseUrl()) }.getOrNull() ?: return url
        val host = dasar.host ?: return url

        return runCatching {
            URI(
                asal.scheme ?: dasar.scheme,
                null,
                host,
                if (dasar.port > 0) dasar.port else -1,
                asal.path,
                asal.query,
                asal.fragment,
            ).toString()
        }.getOrDefault(url)
    }
}
