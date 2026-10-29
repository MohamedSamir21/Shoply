package com.shopping.shoply.products.presentation

import com.shopping.shoply.catalog.domain.model.Category
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.rules.ProductFilters
import com.shopping.shoply.catalog.domain.rules.ProductSort

// State, intents, events and destinations of the Product Listing screen.

/**
 * What Product Listing shows.
 *
 * @property category for the title and item count; null until loaded.
 * @property subcategory the selected chip; null is "All".
 * @property matches the selected subcategory's products before filters (the Filters sheet counts from it).
 */
data class ProductListingState(
    val body: ListingBody = ListingBody.Loading,
    val category: Category? = null,
    val subcategories: List<String> = emptyList(),
    val subcategory: String? = null,
    val matches: List<Product> = emptyList(),
    val results: List<Product> = emptyList(),
    val freeDeliveryStoreIds: Set<String> = emptySet(),
    val filters: ProductFilters = ProductFilters(),
    val sort: ProductSort = ProductSort.RELEVANCE,
    val sheet: ListingSheet = ListingSheet.NONE,
    val wishlist: Set<String> = emptySet(),
)

enum class ListingBody { Loading, Error, Empty, Content }

enum class ListingSheet { NONE, FILTERS, SORT }

sealed interface ProductListingIntent {
    data class SelectSubcategory(val subcategory: String?) : ProductListingIntent
    data class OpenSheet(val sheet: ListingSheet) : ProductListingIntent
    data object CloseSheet : ProductListingIntent
    data class ApplyFilters(val filters: ProductFilters) : ProductListingIntent
    data object ResetFilters : ProductListingIntent
    data class SelectSort(val sort: ProductSort) : ProductListingIntent
    data class ToggleWishlist(val productId: String) : ProductListingIntent
    data class QuickAdd(val product: Product) : ProductListingIntent
    data object Retry : ProductListingIntent
}

sealed interface ProductListingEvent {
    data class ShowMessage(val message: ListingMessage) : ProductListingEvent
}

enum class ListingMessage {
    SAVED_TO_WISHLIST,
    REMOVED_FROM_WISHLIST,
    WISHLIST_FAILED,
    ADDED_TO_CART,
    CART_FAILED,
}

sealed interface ProductListingDestination {
    data object Back : ProductListingDestination
    data object Search : ProductListingDestination
    data class Product(val productId: String) : ProductListingDestination
    data object Categories : ProductListingDestination
    data object Wishlist : ProductListingDestination
    data object Cart : ProductListingDestination
}
