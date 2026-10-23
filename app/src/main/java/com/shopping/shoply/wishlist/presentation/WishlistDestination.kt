package com.shopping.shoply.wishlist.presentation

/** Where the Wishlist screen can send the user. */
sealed interface WishlistDestination {
    data class Product(val id: String) : WishlistDestination

    /** The snackbar's "View" after Move to Cart. */
    data object Cart : WishlistDestination

    /** The empty state's button: back to Home to browse. */
    data object ExploreProducts : WishlistDestination
}
