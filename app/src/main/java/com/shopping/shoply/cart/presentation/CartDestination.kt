package com.shopping.shoply.cart.presentation

/** Where the Cart screen can send the user. */
sealed interface CartDestination {
    data class Product(val id: String) : CartDestination

    data object Wishlist : CartDestination

    /** [promoCode] is the applied code, checked again by Checkout. */
    data class Checkout(val promoCode: String?) : CartDestination

    /** "Start Shopping" on the empty cart. */
    data object StartShopping : CartDestination
}
