package com.happylife2080.norebang

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.happylife2080.norebang.data.CatalogDb
import com.happylife2080.norebang.data.FavoritesDb
import com.happylife2080.norebang.data.Song
import com.happylife2080.norebang.data.UpdateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UiState(
    val query: String = "",
    val brandFilter: String? = null, // null = 전체, "TJ", "KY"
    val results: List<Song> = emptyList(),
    val favorites: List<Song> = emptyList(),
    val favoriteKeys: Set<String> = emptySet(),
    val dbVersion: String = "",
    val songCount: Int = 0,
    val searching: Boolean = false,
)

class SearchViewModel(app: Application) : AndroidViewModel(app) {

    private val catalog = CatalogDb(app)
    private val favoritesDb = FavoritesDb(app)
    private val updateManager = UpdateManager(app, catalog)

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val version = catalog.version()
            val count = catalog.songCount()
            val favs = favoritesDb.all()
            val keys = favoritesDb.keys()
            _state.value = _state.value.copy(
                dbVersion = version, songCount = count, favorites = favs, favoriteKeys = keys
            )
            if (updateManager.checkAndUpdate()) refreshMeta()
        }
        viewModelScope.launch { collectQueries() }
    }

    @OptIn(FlowPreview::class)
    private suspend fun collectQueries() {
        queryFlow.debounce(120).collect { q ->
            val brand = _state.value.brandFilter
            val results = withContext(Dispatchers.IO) { catalog.search(q, brand) }
            // 타이핑이 더 진행됐으면 이 결과는 버린다(늦게 도착한 이전 쿼리).
            val current = _state.value
            if (current.query == q && current.brandFilter == brand) {
                _state.value = current.copy(results = results, searching = false)
            }
        }
    }

    fun onQueryChange(q: String) {
        _state.value = _state.value.copy(query = q, searching = q.isNotBlank())
        queryFlow.value = q
    }

    fun onBrandFilter(brand: String?) {
        _state.value = _state.value.copy(brandFilter = brand, searching = true)
        viewModelScope.launch(Dispatchers.IO) {
            val q = _state.value.query
            val results = catalog.search(q, brand)
            val current = _state.value
            if (current.brandFilter == brand && current.query == q) {
                _state.value = current.copy(results = results, searching = false)
            }
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch(Dispatchers.IO) {
            val key = "${song.brand}:${song.number}"
            if (key in _state.value.favoriteKeys) favoritesDb.remove(song) else favoritesDb.add(song)
            _state.value = _state.value.copy(
                favorites = favoritesDb.all(), favoriteKeys = favoritesDb.keys()
            )
        }
    }

    private suspend fun refreshMeta() {
        val version = catalog.version()
        val count = catalog.songCount()
        _state.value = _state.value.copy(dbVersion = version, songCount = count)
    }
}
