package com.shopping.shoply.product.presentation

/** Where the Product Details screen can send the user. */
sealed interface ProductDetailsDestination {
    data class Store(val id: String) : ProductDetailsDestination

    /** A product from "You May Also Like". */
    data class Product(val id: String) : ProductDetailsDestination

    /** The snackbar's "View" after Add to Cart. */
    data object Cart : ProductDetailsDestination
}
