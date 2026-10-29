package com.shopping.shoply.store.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.catalog.domain.CatalogRepository
import com.shopping.shoply.catalog.domain.model.Store
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.store.domain.FavoriteStoresRepository
import com.shopping.shoply.store.domain.rules.StoreSort
import com.shopping.shoply.store.domain.rules.storeCategoriesOf
import com.shopping.shoply.store.domain.rules.storeList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** true opens Favorite Stores; false (default) All Stores. */
const val STORES_FAVORITES_ONLY_ARG = "favoritesOnly"

/**
 * Backs All Stores and Favorite Stores. Every store loads once; the favorites are live, so a store
 * un-favorited here (or on its page) leaves Favorite Stores at once. The heart flips immediately and
 * flips back with [StoresEvent.FavoriteFailed] if saving fails.
 */
@HiltViewModel
class StoresViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogRepository: CatalogRepository,
    private val favoriteStoresRepository: FavoriteStoresRepository,
) : ViewModel() {

    private val favoritesOnly: Boolean = savedStateHandle[STORES_FAVORITES_ONLY_ARG] ?: false

    private val all = MutableStateFlow<List<Store>?>(null)
    private val status = MutableStateFlow(StoresBody.Loading)
    private val input = MutableStateFlow(Input())

    private val pending = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val shownFavorites: Flow<Set<String>> =
        combine(favoriteStoresRepository.observeIds().onStart { emit(emptySet()) }, pending) { saved, pending ->
            saved + pending.filterValues { it }.keys - pending.filterValues { !it }.keys
        }

    // Favorite Stores lists the saved ids (not the pending ones), so a tapped heart stays visible
    // until the save lands instead of the row vanishing under the finger.
    private val savedFavorites = favoriteStoresRepository.observeIds().onStart { emit(emptySet()) }

    val state: StateFlow<StoresState> =
        combine(all, status, input, shownFavorites, savedFavorites) { all, status, input, favorites, saved ->
            val stores = all.orEmpty()
            if (favoritesOnly) {
                val list = stores.filter { it.id in saved }
                StoresState(
                    favoritesOnly = true,
                    body = if (status == StoresBody.Content && list.isEmpty()) StoresBody.Empty else status,
                    stores = list,
                    totalCount = list.size,
                    favorites = favorites,
                )
            } else {
                StoresState(
                    body = if (status == StoresBody.Content && stores.isEmpty()) StoresBody.Empty else status,
                    stores = storeList(stores, input.category, input.query, input.sort),
                    totalCount = stores.size,
                    categories = storeCategoriesOf(stores),
                    category = input.category,
                    query = input.query,
                    sort = input.sort,
                    sortSheetOpen = input.sortSheetOpen,
                    favorites = favorites,
                )
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, StoresState(favoritesOnly = favoritesOnly))

    private val _events = Channel<StoresEvent>(Channel.BUFFERED)
    val events: Flow<StoresEvent> = _events.receiveAsFlow()

    init {
        load()
    }

    fun onIntent(intent: StoresIntent) {
        when (intent) {
            is StoresIntent.QueryChanged -> input.update { it.copy(query = intent.query) }
            is StoresIntent.SelectCategory -> input.update { it.copy(category = intent.category) }
            StoresIntent.ClearFilters -> input.update { it.copy(query = "", category = null) }
            StoresIntent.OpenSort -> input.update { it.copy(sortSheetOpen = true) }
            StoresIntent.CloseSort -> input.update { it.copy(sortSheetOpen = false) }
            is StoresIntent.SelectSort -> input.update { it.copy(sort = intent.sort, sortSheetOpen = false) }
            is StoresIntent.ToggleFavorite -> toggle(intent.storeId)
            StoresIntent.Retry -> load()
        }
    }

    private fun load() {
        status.value = StoresBody.Loading
        viewModelScope.launch {
            when (val result = catalogRepository.getAllStores()) {
                is Result.Success -> {
                    all.value = result.data
                    status.value = StoresBody.Content
                }
                is Result.Error -> status.value = StoresBody.Error
            }
        }
    }

    private fun toggle(storeId: String) {
        if (storeId in pending.value) return
        val favorite = storeId !in state.value.favorites
        pending.update { it + (storeId to favorite) }
        viewModelScope.launch {
            val result = favoriteStoresRepository.setFavorite(storeId, favorite)
            pending.update { it - storeId }
            if (result is Result.Error) _events.send(StoresEvent.FavoriteFailed)
        }
    }

    private data class Input(
        val query: String = "",
        val category: String? = null,
        val sort: StoreSort = StoreSort.NEAREST,
        val sortSheetOpen: Boolean = false,
    )
}
