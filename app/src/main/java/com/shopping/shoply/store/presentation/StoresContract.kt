package com.shopping.shoply.store.presentation

import com.shopping.shoply.catalog.domain.model.Store
import com.shopping.shoply.store.domain.rules.StoreSort

// State, intents, events and destinations of All Stores and Favorite Stores (one screen, two modes).

/**
 * @property favoritesOnly true for Favorite Stores: [stores] is just the favorites, with no search,
 *   chips or sort.
 * @property totalCount every store (All Stores) or every favorite, for the top bar subtitle.
 * @property favorites ids whose heart is filled, including taps still being saved.
 */
data class StoresState(
    val favoritesOnly: Boolean = false,
    val body: StoresBody = StoresBody.Loading,
    val stores: List<Store> = emptyList(),
    val totalCount: Int = 0,
    val categories: List<String> = emptyList(),
    val category: String? = null,
    val query: String = "",
    val sort: StoreSort = StoreSort.NEAREST,
    val sortSheetOpen: Boolean = false,
    val favorites: Set<String> = emptySet(),
)

/** [Empty]: no stores at all (All Stores) or no favorites yet (Favorite Stores). */
enum class StoresBody { Loading, Error, Empty, Content }

sealed interface StoresIntent {
    data class QueryChanged(val query: String) : StoresIntent
    data class SelectCategory(val category: String?) : StoresIntent
    data object ClearFilters : StoresIntent
    data object OpenSort : StoresIntent
    data object CloseSort : StoresIntent
    data class SelectSort(val sort: StoreSort) : StoresIntent
    data class ToggleFavorite(val storeId: String) : StoresIntent
    data object Retry : StoresIntent
}

sealed interface StoresEvent {
    /** Saving a favorite failed; the heart already flipped back. */
    data object FavoriteFailed : StoresEvent
}

sealed interface StoresDestination {
    data object Back : StoresDestination
    data class Store(val storeId: String) : StoresDestination
    data object AllStores : StoresDestination
    data object Addresses : StoresDestination
}
