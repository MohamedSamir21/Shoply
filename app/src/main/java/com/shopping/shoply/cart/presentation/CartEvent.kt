package com.shopping.shoply.cart.presentation

sealed interface CartEvent {
    /** Show [message] in a snackbar. */
    data class ShowMessage(val message: CartMessage) : CartEvent
}

/** Snackbar messages. The screen maps each to its text. */
enum class CartMessage {
    REMOVED,
    UPDATE_FAILED,
    PROMO_FAILED,
}
