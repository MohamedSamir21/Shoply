package com.shopping.shoply.search.presentation

import com.shopping.shoply.catalog.domain.model.Category
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.model.Store
import com.shopping.shoply.catalog.domain.rules.ProductFilters
import com.shopping.shoply.catalog.domain.rules.ProductSort

// State, intents, events and destinations of the Search screen.

/**
 * What Search shows. Which part depends on [query] and [submitted]:
 * - blank and not submitted: idle ([recent], popular searches, [stores]);
 * - typed, not submitted: [suggestedStores] and [suggestions];
 * - submitted: the toolbar and [results]. A blank submitted query browses every product.
 *
 * @property matches the submitted query's products before filters (the Filters sheet counts from it).
 * @property freeDeliveryStoreIds stores with no delivery fee, for counting with the free-delivery filter.
 */
data class SearchState(
    val query: String = "",
    val submitted: Boolean = false,
    val catalog: SearchCatalog = SearchCatalog.Loading,
    val recent: List<String> = emptyList(),
    val stores: List<Store> = emptyList(),
    val categories: List<Category> = emptyList(),
    val suggestedStores: List<Store> = emptyList(),
    val suggestions: List<Product> = emptyList(),
    val matches: List<Product> = emptyList(),
    val results: List<Product> = emptyList(),
    val freeDeliveryStoreIds: Set<String> = emptySet(),
    val filters: ProductFilters = ProductFilters(),
    val sort: ProductSort = ProductSort.RELEVANCE,
    val sheet: SearchSheet = SearchSheet.NONE,
    val wishlist: Set<String> = emptySet(),
)

enum class SearchCatalog { Loading, Error, Ready }

enum class SearchSheet { NONE, FILTERS, SORT }

/** The one-tap chips next to Filters and Sort. */
enum class QuickFilter { UNDER_50, RATING_4_5, FREE_DELIVERY }

sealed interface SearchIntent {
    data class QueryChanged(val query: String) : SearchIntent

    /** Search for [query] (typed, a recent one, a popular one or a suggestion). */
    data class Submit(val query: String) : SearchIntent

    data object ClearQuery : SearchIntent

    data class RemoveRecent(val query: String) : SearchIntent

    data object ClearRecent : SearchIntent

    data class OpenSheet(val sheet: SearchSheet) : SearchIntent

    data object CloseSheet : SearchIntent

    data class ApplyFilters(val filters: ProductFilters) : SearchIntent

    data object ResetFilters : SearchIntent

    data class SelectSort(val sort: ProductSort) : SearchIntent

    data class ToggleQuickFilter(val filter: QuickFilter) : SearchIntent

    data class ToggleWishlist(val productId: String) : SearchIntent

    data class QuickAdd(val product: Product) : SearchIntent

    data object Retry : SearchIntent
}

sealed interface SearchEvent {
    data class ShowMessage(val message: SearchMessage) : SearchEvent
}

enum class SearchMessage {
    SAVED_TO_WISHLIST,
    REMOVED_FROM_WISHLIST,
    WISHLIST_FAILED,
    ADDED_TO_CART,
    CART_FAILED,
}

sealed interface SearchDestination {
    data object Back : SearchDestination
    data class Product(val productId: String) : SearchDestination
    data class Store(val storeId: String) : SearchDestination

    /** The snackbar's "View" after saving to the wishlist / adding to the cart. */
    data object Wishlist : SearchDestination
    data object Cart : SearchDestination
}
