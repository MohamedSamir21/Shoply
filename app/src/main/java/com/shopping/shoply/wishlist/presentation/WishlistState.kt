package com.shopping.shoply.wishlist.presentation

import com.shopping.shoply.catalog.domain.model.Product

/** What the Wishlist screen shows. */
data class WishlistState(
    val body: WishlistBody = WishlistBody.Loading,
)

sealed interface WishlistBody {
    data object Loading : WishlistBody

    /** Nothing saved (or every saved product is gone from the catalog). */
    data object Empty : WishlistBody

    /** The products couldn't be loaded and there is nothing to show. */
    data object Offline : WishlistBody

    /** The saved products, in the order of the saved ids. */
    data class Content(val products: List<Product>) : WishlistBody
}
