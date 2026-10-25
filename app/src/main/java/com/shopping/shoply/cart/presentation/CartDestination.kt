package com.shopping.shoply.cart.presentation

/** Where the Cart screen can send the user. */
sealed interface CartDestination {
    data class Product(val id: String) : CartDestination

    data object Wishlist : CartDestination

    data object Checkout : CartDestination

    /** "Start Shopping" on the empty cart. */
    data object StartShopping : CartDestination
}
