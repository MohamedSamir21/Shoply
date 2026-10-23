package com.shopping.shoply.wishlist.presentation

sealed interface WishlistEvent {
    /** Show [message] in a snackbar. */
    data class ShowMessage(val message: WishlistMessage) : WishlistEvent
}

/** Snackbar messages. The screen maps each to its text (and "View" for [MOVED_TO_CART]). */
enum class WishlistMessage {
    REMOVED,
    REMOVE_FAILED,
    MOVED_TO_CART,
    CART_FAILED,
}
