package com.shopping.shoply.home.presentation

/** One-shot things that happen once, delivered through a Channel (not kept in state). */
sealed interface HomeEvent {
    data class ShowMessage(val message: HomeMessage) : HomeEvent
}

/**
 * Snackbar messages. Typed instead of strings so tests compare enums, and the screen picks the
 * text (and the "View" action for [SAVED_TO_WISHLIST] and [ADDED_TO_CART]).
 */
enum class HomeMessage {
    SAVED_TO_WISHLIST,
    REMOVED_FROM_WISHLIST,
    WISHLIST_FAILED,
    ADDED_TO_CART,
    CART_FAILED,
}
