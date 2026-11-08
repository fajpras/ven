package com.ven.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ven.app.data.api.ApiClient
import com.ven.app.data.api.ApiHome
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val apiHome: ApiHome = ApiClient.createService(ApiHome::class.java),
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        muatDataBeranda()
    }

    fun muatUlang(filterSeen: Boolean = true) {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        muatDataBeranda(isRefresh = true, filterSeen = filterSeen)
    }

    private fun muatDataBeranda(isRefresh: Boolean = false, filterSeen: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentSeen = if (filterSeen) {
                    _uiState.value.seenPostIds + _uiState.value.posts.map { it.id }.toSet()
                } else {
                    _uiState.value.seenPostIds
                }

                val excludeParam = if (filterSeen && currentSeen.isNotEmpty()) {
                    currentSeen.take(30).joinToString(",")
                } else null

                val pameranDeferred = async { apiHome.getRekomendasiPameran(page = 1) }
                val karyaDeferred = async {
                    apiHome.getRekomendasiKarya(
                        page = 1,
                        exclude = excludeParam,
                        refresh = if (isRefresh) true else null,
                    )
                }

                val pameranRes = pameranDeferred.await()
                val karyaRes = karyaDeferred.await()

                val events = if (pameranRes.isSuccessful) {
                    pameranRes.body()?.data?.data?.map { it.toComingSoonEvent() } ?: emptyList()
                } else {
                    emptyList()
                }

                val paginatedKarya = karyaRes.body()?.data
                var rawPosts = if (karyaRes.isSuccessful) {
                    paginatedKarya?.data?.map { it.toPostItem() } ?: emptyList()
                } else {
                    emptyList()
                }

                var currentPage = 1
                var lastPage = paginatedKarya?.lastPage ?: 1
                var filteredPosts = if (filterSeen && currentSeen.isNotEmpty()) {
                    rawPosts.filterNot { it.id in currentSeen }
                } else {
                    rawPosts
                }

                while (filteredPosts.isEmpty() && currentPage < lastPage) {
                    currentPage++
                    val nextPageRes = apiHome.getRekomendasiKarya(
                        page = currentPage,
                        exclude = excludeParam,
                        refresh = if (isRefresh) true else null,
                    )
                    if (nextPageRes.isSuccessful) {
                        val nextData = nextPageRes.body()?.data
                        val nextBatch = nextData?.data?.map { it.toPostItem() } ?: emptyList()
                        lastPage = nextData?.lastPage ?: lastPage
                        val fresh = if (filterSeen && currentSeen.isNotEmpty()) {
                            nextBatch.filterNot { it.id in currentSeen }
                        } else {
                            nextBatch
                        }
                        filteredPosts = fresh
                    } else {
                        break
                    }
                }

                // Jika filteredPosts kosong (seluruh karya sudah pernah dilihat):
                // Acak urutan karya yang ada agar pengguna disajikan hal baru yang segar
                var finalPosts = when {
                    filteredPosts.isNotEmpty() -> filteredPosts
                    rawPosts.isNotEmpty() -> {
                        val currentFirstId = _uiState.value.posts.firstOrNull()?.id
                        val shuffled = rawPosts.shuffled()
                        if (shuffled.firstOrNull()?.id == currentFirstId && shuffled.size > 1) {
                            shuffled.drop(1) + shuffled.take(1)
                        } else {
                            shuffled
                        }
                    }
                    else -> emptyList()
                }

                // Fallback: Jika finalPosts kosong karena filterSeen atau seluruh karya di DB sudah pernah dilihat,
                // ambil ulang rekomendasi segar tanpa exclude agar beranda selalu menyajikan karya dan tidak pernah kosong
                if (finalPosts.isEmpty() && filterSeen) {
                    val fallbackRes = apiHome.getRekomendasiKarya(page = 1, refresh = true)
                    if (fallbackRes.isSuccessful) {
                        val fallbackBatch = fallbackRes.body()?.data?.data?.map { it.toPostItem() } ?: emptyList()
                        if (fallbackBatch.isNotEmpty()) {
                            finalPosts = fallbackBatch.shuffled()
                        }
                    }
                }

                // Pertahankan postingan dan event sebelumnya jika respons server baru kosong/gagal
                val safePosts = if (finalPosts.isNotEmpty()) finalPosts else _uiState.value.posts
                val safeEvents = if (events.isNotEmpty()) events else _uiState.value.comingSoonEvents

                // Jika kita merotasi ulang karena semua sudah dilihat, reset cache seen ke batch baru ini
                val updatedSeen = if (filteredPosts.isNotEmpty()) {
                    currentSeen + safePosts.map { it.id }.toSet()
                } else {
                    safePosts.map { it.id }.toSet()
                }
                val canMore = currentPage < lastPage && safePosts.isNotEmpty()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        canLoadMore = canMore,
                        currentPage = currentPage,
                        comingSoonEvents = safeEvents,
                        posts = safePosts,
                        seenPostIds = updatedSeen,
                        errorMessage = null,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        errorMessage = e.localizedMessage ?: "Gagal memuat feed beranda.",
                    )
                }
            }
        }
    }

    fun loadNextChunk() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isLoadingMore || !currentState.canLoadMore) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoadingMore = true) }
            val nextPage = currentState.currentPage + 1
            try {
                val res = apiHome.getRekomendasiKarya(page = nextPage)
                if (res.isSuccessful) {
                    val paginated = res.body()?.data
                    val newPosts = paginated?.data?.map { it.toPostItem() } ?: emptyList()
                    val hasMore = paginated != null && paginated.currentPage < paginated.lastPage && newPosts.isNotEmpty()

                    _uiState.update {
                        val merged = (it.posts + newPosts).distinctBy { item -> item.id }
                        val updatedSeen = it.seenPostIds + newPosts.map { post -> post.id }.toSet()
                        it.copy(
                            isLoadingMore = false,
                            canLoadMore = hasMore,
                            currentPage = nextPage,
                            posts = merged,
                            seenPostIds = updatedSeen,
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoadingMore = false, canLoadMore = false) }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }
}
