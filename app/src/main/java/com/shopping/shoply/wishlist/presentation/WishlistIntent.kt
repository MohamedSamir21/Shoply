package com.shopping.shoply.wishlist.presentation

import com.shopping.shoply.catalog.domain.model.Product

sealed interface WishlistIntent {
    data object Retry : WishlistIntent

    /** Filled ♥: un-save it. */
    data class Remove(val productId: String) : WishlistIntent

    /** Add 1 to the cart, then un-save it. */
    data class MoveToCart(val product: Product) : WishlistIntent
}
