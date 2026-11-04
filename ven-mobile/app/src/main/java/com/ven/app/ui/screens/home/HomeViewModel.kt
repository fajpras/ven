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

    fun muatUlang() {
        _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
        muatDataBeranda(isRefresh = true)
    }

    private fun muatDataBeranda(isRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pameranDeferred = async { apiHome.getRekomendasiPameran(page = 1) }
                val karyaDeferred = async { apiHome.getRekomendasiKarya(page = 1) }

                val pameranRes = pameranDeferred.await()
                val karyaRes = karyaDeferred.await()

                val events = if (pameranRes.isSuccessful) {
                    pameranRes.body()?.data?.data?.map { it.toComingSoonEvent() } ?: emptyList()
                } else {
                    emptyList()
                }

                val paginatedKarya = karyaRes.body()?.data
                val posts = if (karyaRes.isSuccessful) {
                    paginatedKarya?.data?.map { it.toPostItem() } ?: emptyList()
                } else {
                    emptyList()
                }

                val canMore = paginatedKarya != null && paginatedKarya.currentPage < paginatedKarya.lastPage && posts.isNotEmpty()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        canLoadMore = canMore,
                        currentPage = 1,
                        comingSoonEvents = events,
                        posts = posts,
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

    /**
     * Memuat chunk halaman berikutnya (infinite scroll) secara ringan dan efisien.
     */
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
                        it.copy(
                            isLoadingMore = false,
                            canLoadMore = hasMore,
                            currentPage = nextPage,
                            posts = merged,
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
