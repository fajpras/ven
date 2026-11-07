package com.ven.app.ui.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ven.app.data.api.ApiClient
import com.ven.app.data.api.ApiPencarian
import com.ven.app.data.api.models.KaryaItemModel
import com.ven.app.ui.components.feature.MediaGridItem
import com.ven.app.util.DateFormatter
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
     * Memuat ulang data Explore secara reaktif (pull-to-refresh & double tap).
     */
    fun muatUlang(filterSeen: Boolean = false) {
        _uiState.update { it.copy(isRefreshing = true) }
        if (_uiState.value.isShowingResults) {
            val q = _uiState.value.searchQuery
            val tab = _uiState.value.selectedTab
            searchJob?.cancel()
            searchJob = viewModelScope.launch(Dispatchers.IO) {
                lakukanPencarian(q, tab)
                _uiState.update { it.copy(isRefreshing = false) }
            }
        } else {
            muatDiscoveryMedia(isRefresh = true, filterSeen = filterSeen)
        }
    }

    /**
     * Memuat karya awal dari database untuk ditampilkan pada grid Discovery dan Feed detail.
     * Mendukung filterSeen untuk menampilkan hal baru yang belum pernah dilihat saat refresh.
     */
    fun muatDiscoveryMedia(isRefresh: Boolean = false, filterSeen: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentSeen = if (filterSeen) {
                    _uiState.value.seenMediaIds + _uiState.value.discoveryMedia.map { it.id }.toSet()
                } else {
                    _uiState.value.seenMediaIds
                }

                // Muat daftar karya
                val res = apiPencarian.searchKarya(query = null, tipe = "karya", page = 1)
                if (res.isSuccessful) {
                    val paginated = res.body()?.data
                    val rawKarya = paginated?.data ?: emptyList()
                    var currentPage = 1
                    var lastPage = paginated?.lastPage ?: 1

                    var filteredKarya = if (filterSeen && currentSeen.isNotEmpty()) {
                        rawKarya.filterNot { it.idKarya in currentSeen }
                    } else {
                        rawKarya
                    }

                    // Jika difilter dan halaman pertama sudah pernah dilihat semua, cari halaman berikutnya
                    while (filteredKarya.isEmpty() && currentPage < lastPage) {
                        currentPage++
                        val nextRes = apiPencarian.searchKarya(query = null, tipe = "karya", page = currentPage)
                        if (nextRes.isSuccessful) {
                            val nextPaginated = nextRes.body()?.data
                            val nextData = nextPaginated?.data ?: emptyList()
                            lastPage = nextPaginated?.lastPage ?: lastPage
                            val fresh = if (filterSeen && currentSeen.isNotEmpty()) {
                                nextData.filterNot { it.idKarya in currentSeen }
                            } else {
                                nextData
                            }
                            filteredKarya = fresh
                        } else {
                            break
                        }
                    }

                    val finalKarya = if (filteredKarya.isEmpty()) {
                        rawKarya
                    } else {
                        filteredKarya
                    }

                    val items = finalKarya.map { k ->
                        MediaGridItem(
                            id = k.idKarya,
                            title = k.judul,
                            imageUrl = k.fileUrl ?: k.file,
                        )
                    }
                    val feedList = finalKarya.map { it.toFeedItem() }
                    val hasMore = currentPage < lastPage && items.isNotEmpty()
                    val updatedSeen = currentSeen + items.map { it.id }.toSet()

                    if (items.isNotEmpty()) {
                        _uiState.update {
                            it.copy(
                                isRefreshing = false,
                                discoveryMedia = items,
                                resultPosts = items,
                                feedPosts = feedList,
                                currentPostsPage = currentPage,
                                canLoadMorePosts = hasMore,
                                seenMediaIds = updatedSeen,
                            )
                        }
                    } else {
                        _uiState.update { it.copy(isRefreshing = false) }
                    }
                } else {
                    _uiState.update { it.copy(isRefreshing = false) }
                }

                // Muat data pengguna nyata untuk riwayat pencarian awal (Recents)
                val userRes = apiPencarian.searchPengguna(null)
                if (userRes.isSuccessful) {
                    val users = userRes.body()?.data?.data ?: emptyList()
                    val realRecents = users.take(6).map { u ->
                        RecentSearchItem.Account(
                            id = u.idPengguna ?: "",
                            username = "@" + (u.namaPanggilan ?: "user"),
                            fullName = u.nama ?: "",
                            avatarUrl = u.fotoProfil,
                        )
                    }
                    if (realRecents.isNotEmpty()) {
                        _uiState.update { it.copy(recentSearches = realRecents) }
                    }
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isRefreshing = false) }
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
                                avatarUrl = u.fotoProfil,
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
                                category = p.tipe ?: "3D",
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
                                avatarUrl = u.fotoProfil,
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
                                category = p.tipe ?: "3D",
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

    fun onAccountClicked(account: ExploreAccountItem) {
        _uiState.update { state ->
            val newEntry = RecentSearchItem.Account(
                id = account.id,
                username = if (account.username.startsWith("@")) account.username else "@${account.username}",
                fullName = account.fullName,
                avatarUrl = account.avatarUrl,
            )
            val updated = (listOf(newEntry) + state.recentSearches.filterNot { it.id == account.id }).take(10)
            state.copy(recentSearches = updated)
        }
    }

    fun removeRecentSearch(item: RecentSearchItem) {
        _uiState.update { state ->
            state.copy(recentSearches = state.recentSearches.filterNot { it.id == item.id })
        }
    }

    private fun KaryaItemModel.toFeedItem(): PostsFeedItem {
        val author = pengguna?.namaPanggilan ?: pengguna?.nama ?: "kreator"
        val relativeTime = DateFormatter.formatRelativeTime(createdAt)
        return PostsFeedItem(
            id = idKarya,
            username = if (author.startsWith("@")) author else "@$author",
            userId = pengguna?.idPengguna,
            userAvatarUrl = pengguna?.fotoProfil,
            title = judul,
            category = kategori?.nama,
            timestampText = relativeTime,
            description = deskripsi ?: "",
            imageUrl = fileUrl ?: file,
            pameranId = idPameran?.toString(),
        )
    }
}
