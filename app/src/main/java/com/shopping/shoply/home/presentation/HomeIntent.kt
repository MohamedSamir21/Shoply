package com.shopping.shoply.home.presentation

import com.shopping.shoply.catalog.domain.model.Product

/**
 * User actions that need logic. Plain navigation taps (search, a store, a product, see all…) don't
 * go through the ViewModel: the screen calls its navigation callbacks directly.
 */
sealed interface HomeIntent {
    /** Reload the feed from the Offline state. */
    data object Retry : HomeIntent

    /** Heart tapped on a product card. */
    data class ToggleWishlist(val productId: String) : HomeIntent

    /** "+" tapped on a product card. Carries the whole product, which the cart needs. */
    data class QuickAdd(val product: Product) : HomeIntent
}
