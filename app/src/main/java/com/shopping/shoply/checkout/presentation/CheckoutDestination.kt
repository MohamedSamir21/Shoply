package com.shopping.shoply.checkout.presentation

/** Where the Checkout screen can send the user. */
sealed interface CheckoutDestination {
    data object Back : CheckoutDestination

    data class OrderPlaced(val orderId: String) : CheckoutDestination

    data object AddAddress : CheckoutDestination

    data object AddCard : CheckoutDestination
}
