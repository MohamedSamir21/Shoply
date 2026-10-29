package com.shopping.shoply.store.presentation

import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.model.Store
import com.shopping.shoply.store.domain.rules.OpeningStatus
import com.shopping.shoply.store.domain.rules.StoreTab

// State, intents, events and destinations of the Store screen.

/**
 * What the Store screen shows.
 *
 * @property opening for the hours line; null until the store loads.
 * @property favorite whether the header heart is filled, including a tap still being saved.
 * @property wishlist ids of products whose heart is filled, including taps still being saved.
 */
data class StoreState(
    val body: StoreBody = StoreBody.Loading,
    val tab: StoreTab = StoreTab.ALL,
    val opening: OpeningStatus? = null,
    val favorite: Boolean = false,
    val wishlist: Set<String> = emptySet(),
)

sealed interface StoreBody {
    data object Loading : StoreBody

    /** The store or its products failed to load. */
    data object Error : StoreBody

    /** [products] is the selected tab's list (see [com.shopping.shoply.store.domain.rules.storeProducts]). */
    data class Content(val store: Store, val products: List<Product>) : StoreBody
}

sealed interface StoreIntent {
    data class SelectTab(val tab: StoreTab) : StoreIntent

    /** Header heart tapped. */
    data object ToggleFavorite : StoreIntent

    /** Heart tapped on a product card. */
    data class ToggleWishlist(val productId: String) : StoreIntent

    /** "+" tapped on a product card. */
    data class QuickAdd(val product: Product) : StoreIntent

    data object Retry : StoreIntent
}

sealed interface StoreEvent {
    data class ShowMessage(val message: StoreMessage) : StoreEvent
}

/** Snackbar messages; the screen picks the text. */
enum class StoreMessage {
    SAVED_TO_WISHLIST,
    REMOVED_FROM_WISHLIST,
    WISHLIST_FAILED,
    ADDED_TO_CART,
    CART_FAILED,
    FAVORITE_FAILED,
}

/** Where Store can send the user; MainShell maps it to a route. */
sealed interface StoreDestination {
    data object Back : StoreDestination
    data class Product(val productId: String) : StoreDestination
    data object Search : StoreDestination
    data object Home : StoreDestination

    /** The snackbar's "View" after saving to the wishlist / adding to the cart. */
    data object Wishlist : StoreDestination
    data object Cart : StoreDestination
}
