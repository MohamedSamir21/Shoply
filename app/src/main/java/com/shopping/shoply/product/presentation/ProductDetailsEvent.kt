package com.shopping.shoply.product.presentation

sealed interface ProductDetailsEvent {
    /** Show [message] in a snackbar. */
    data class ShowMessage(val message: ProductDetailsMessage) : ProductDetailsEvent
}

/** Snackbar messages. The screen maps each to its text (and "View" for [ADDED_TO_CART]). */
enum class ProductDetailsMessage {
    ADDED_TO_CART,
    CART_FAILED,
    SAVED,
    REMOVED,
    WISHLIST_FAILED,
}
