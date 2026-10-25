package com.shopping.shoply.cart.presentation

sealed interface CartIntent {
    data class Increase(val itemId: String) : CartIntent

    /** Does nothing at quantity 1: the stepper shows remove instead. */
    data class Decrease(val itemId: String) : CartIntent

    data class Remove(val itemId: String) : CartIntent

    data class PromoInputChanged(val text: String) : CartIntent

    data object ApplyPromo : CartIntent

    data object RemovePromo : CartIntent
}
