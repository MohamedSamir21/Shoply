package com.shopping.shoply.home.presentation

/**
 * Where Home can send the user. Home only names the destination; [com.shopping.shoply.common.presentation.navigation.MainShell]
 * maps it to a route. Destinations that aren't built yet open a "Coming soon" placeholder.
 */
sealed interface HomeDestination {
    data class Search(val openFilters: Boolean) : HomeDestination
    data object Notifications : HomeDestination
    data object Wishlist : HomeDestination
    data object Addresses : HomeDestination
    data class ProductListing(val categoryId: String) : HomeDestination
    data object AllCategories : HomeDestination
    data object AllStores : HomeDestination
    data class Store(val storeId: String) : HomeDestination
    data class ProductDetails(val productId: String) : HomeDestination
    data object Cart : HomeDestination
    data object Help : HomeDestination
}
