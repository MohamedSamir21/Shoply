package com.shopping.shoply.product.presentation

sealed interface ProductDetailsIntent {
    data object Retry : ProductDetailsIntent

    data class SelectColor(val name: String) : ProductDetailsIntent

    data class SelectSize(val size: String) : ProductDetailsIntent

    data object Increase : ProductDetailsIntent

    data object Decrease : ProductDetailsIntent

    data object ToggleWishlist : ProductDetailsIntent

    /** Adds the picked variant × quantity. */
    data object AddToCart : ProductDetailsIntent
}
