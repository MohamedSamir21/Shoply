package com.shopping.shoply.checkout.presentation

sealed interface CheckoutEvent {
    /** Show [message] in a snackbar. */
    data class ShowMessage(val message: CheckoutMessage) : CheckoutEvent

    /** Back from the first step: leave Checkout. */
    data object Back : CheckoutEvent

    data class OrderPlaced(val orderId: String) : CheckoutEvent
}

/** Snackbar messages. The screen maps each to its text. */
enum class CheckoutMessage {
    ORDER_FAILED,
}
