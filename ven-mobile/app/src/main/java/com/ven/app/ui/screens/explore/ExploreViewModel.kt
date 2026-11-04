package com.ven.app.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ven.app.data.api.ApiClient
import com.ven.app.data.api.ApiPencarian
import com.ven.app.data.api.models.KaryaItemModel
import com.ven.app.ui.components.feature.MediaGridItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ExploreViewModel(
    private val apiPencarian: ApiPencarian = ApiClient.createService(ApiPencarian::class.java),
) : ViewModel() {

    private val _uiState = MutableStateFlow(dummyExploreUiState)
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        muatDiscoveryMedia()
    }

    /**
     * Memuat karya awal dari database untuk ditampilkan pada grid Discovery dan Feed detail.
     */
    fun muatDiscoveryMedia() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = apiPencarian.searchKarya(query = null, tipe = "karya", page = 1)
                if (res.isSuccessful) {
                    val paginated = res.body()?.data
                    val rawKarya = paginated?.data ?: emptyList()
                    val items = rawKarya.map { k ->
                        MediaGridItem(
                            id = k.idKarya,
                            title = k.judul,
                            imageUrl = k.fileUrl ?: k.file,
                        )
                    }
                    val feedList = rawKarya.map { it.toFeedItem() }
                    val hasMore = paginated != null && paginated.currentPage < paginated.lastPage

                    if (items.isNotEmpty()) {
                        _uiState.update {
                            it.copy(
                                discoveryMedia = items,
                                resultPosts = items,
                                feedPosts = feedList,
                                currentPostsPage = 1,
                                canLoadMorePosts = hasMore,
                            )
                        }
                    }
                }
            } catch (_: Exception) {
                // Gunakan fallback data jika offline
            }
        }
    }

    fun setSearchActive(active: Boolean) {
        _uiState.update {
            it.copy(
                isSearchActive = active,
                searchQuery = if (!active) "" else it.searchQuery,
            )
        }
        if (active && _uiState.value.searchQuery.isNotBlank()) {
            val q = _uiState.value.searchQuery
            searchJob?.cancel()
            searchJob = viewModelScope.launch(Dispatchers.IO) {
                lakukanPencarian(q, _uiState.value.selectedTab)
            }
        }
    }

    fun setFilterSheetOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isFilterSheetOpen = isOpen) }
    }

    fun toggleCategory(category: String) {
        _uiState.update { state ->
            val updated = state.selectedCategories.toMutableSet().apply {
                if (!add(category)) remove(category)
            }
            state.copy(selectedCategories = updated)
        }
        val query = _uiState.value.searchQuery
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            lakukanPencarian(query, _uiState.value.selectedTab)
        }
    }

    fun resetFilter() {
        _uiState.update { it.copy(selectedCategories = emptySet()) }
        val query = _uiState.value.searchQuery
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            lakukanPencarian(query, _uiState.value.selectedTab)
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query, isSearchActive = true) }
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.update {
                it.copy(
                    resultAccounts = emptyList(),
                    resultPosts = it.discoveryMedia,
                    resultExhibits = emptyList(),
                )
            }
            return
        }

        searchJob = viewModelScope.launch(Dispatchers.IO) {
            delay(350) // Debounce pengetikan
            lakukanPencarian(query, _uiState.value.selectedTab)
        }
    }

    fun onTabChange(tab: ExploreSearchTab) {
        _uiState.update { it.copy(selectedTab = tab) }
        val query = _uiState.value.searchQuery
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            lakukanPencarian(query, tab)
        }
    }

    private suspend fun lakukanPencarian(query: String, tab: ExploreSearchTab) {
        val qParam = query.trim().ifBlank { null }
        try {
            _uiState.update { it.copy(isLoading = true) }
            when (tab) {
                ExploreSearchTab.ForYou -> {
                    val response = apiPencarian.searchSemua(qParam, "semua")
                    if (response.isSuccessful) {
                        val body = response.body()?.data
                        val accounts = body?.pengguna?.map { u ->
                            ExploreAccountItem(
                                id = u.idPengguna ?: "",
                                username = u.namaPanggilan ?: "",
                                fullName = u.nama ?: "",
                            )
                        } ?: emptyList()

                        val rawKarya = body?.karya ?: emptyList()
                        val posts = rawKarya.map { k ->
                            MediaGridItem(
                                id = k.idKarya,
                                title = k.judul,
                                imageUrl = k.fileUrl ?: k.file,
                            )
                        }
                        val feedList = rawKarya.map { it.toFeedItem() }

                        val exhibits = body?.pameran?.map { p ->
                            ExhibitItem(
                                id = p.idPameran.toString(),
                                title = p.judul,
                                bannerUrl = p.banner,
                            )
                        } ?: emptyList()

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSearchActive = true,
                                resultAccounts = accounts,
                                resultPosts = if (posts.isNotEmpty()) posts else it.discoveryMedia,
                                resultExhibits = exhibits,
                                feedPosts = if (feedList.isNotEmpty()) feedList else it.feedPosts,
                                currentPostsPage = 1,
                            )
                        }
                    }
                }
                ExploreSearchTab.Accounts -> {
                    val res = apiPencarian.searchPengguna(qParam)
                    if (res.isSuccessful) {
                        val accounts = res.body()?.data?.data?.map { u ->
                            ExploreAccountItem(
                                id = u.idPengguna ?: "",
                                username = u.namaPanggilan ?: "",
                                fullName = u.nama ?: "",
                            )
                        } ?: emptyList()

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSearchActive = true,
                                resultAccounts = accounts,
                            )
                        }
                    }
                }
                ExploreSearchTab.Posts -> {
                    val res = apiPencarian.searchKarya(qParam, page = 1)
                    if (res.isSuccessful) {
                        val paginated = res.body()?.data
                        val rawKarya = paginated?.data ?: emptyList()
                        val posts = rawKarya.map { k ->
                            MediaGridItem(
                                id = k.idKarya,
                                title = k.judul,
                                imageUrl = k.fileUrl ?: k.file,
                            )
                        }
                        val feedList = rawKarya.map { it.toFeedItem() }
                        val hasMore = paginated != null && paginated.currentPage < paginated.lastPage

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSearchActive = true,
                                resultPosts = if (posts.isNotEmpty()) posts else it.discoveryMedia,
                                feedPosts = if (feedList.isNotEmpty()) feedList else it.feedPosts,
                                currentPostsPage = 1,
                                canLoadMorePosts = hasMore,
                            )
                        }
                    }
                }
                ExploreSearchTab.Exhibits -> {
                    val res = apiPencarian.searchPameran(qParam)
                    if (res.isSuccessful) {
                        val exhibits = res.body()?.data?.data?.map { p ->
                            ExhibitItem(
                                id = p.idPameran.toString(),
                                title = p.judul,
                                bannerUrl = p.banner,
                            )
                        } ?: emptyList()

                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSearchActive = true,
                                resultExhibits = exhibits,
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Memuat chunk halaman karya berikutnya pada Explore.
     */
    fun loadMorePostsChunk() {
        val currentState = _uiState.value
        if (currentState.isLoading || currentState.isLoadingMorePosts || !currentState.canLoadMorePosts) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isLoadingMorePosts = true) }
            val nextPage = currentState.currentPostsPage + 1
            val qParam = currentState.searchQuery.trim().ifBlank { null }
            try {
                val res = apiPencarian.searchKarya(query = qParam, tipe = "karya", page = nextPage)
                if (res.isSuccessful) {
                    val paginated = res.body()?.data
                    val rawKarya = paginated?.data ?: emptyList()
                    val newMedia = rawKarya.map { k ->
                        MediaGridItem(
                            id = k.idKarya,
                            title = k.judul,
                            imageUrl = k.fileUrl ?: k.file,
                        )
                    }
                    val newFeed = rawKarya.map { it.toFeedItem() }
                    val hasMore = paginated != null && paginated.currentPage < paginated.lastPage && rawKarya.isNotEmpty()

                    _uiState.update {
                        it.copy(
                            isLoadingMorePosts = false,
                            canLoadMorePosts = hasMore,
                            currentPostsPage = nextPage,
                            resultPosts = (it.resultPosts + newMedia).distinctBy { item -> item.id },
                            discoveryMedia = (it.discoveryMedia + newMedia).distinctBy { item -> item.id },
                            feedPosts = (it.feedPosts + newFeed).distinctBy { item -> item.id },
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoadingMorePosts = false, canLoadMorePosts = false) }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoadingMorePosts = false) }
            }
        }
    }

    private fun KaryaItemModel.toFeedItem(): PostsFeedItem {
        val author = pengguna?.namaPanggilan ?: pengguna?.nama ?: "kreator"
        return PostsFeedItem(
            id = idKarya,
            username = if (author.startsWith("@")) author else "@$author",
            title = judul,
            description = deskripsi ?: "",
            imageUrl = fileUrl ?: file,
            pameranId = idPameran?.toString(),
        )
    }
}
