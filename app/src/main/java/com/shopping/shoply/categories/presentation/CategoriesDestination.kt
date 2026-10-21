package com.shopping.shoply.categories.presentation

/**
 * Where Categories can send the user. Categories only names the destination;
 * [com.shopping.shoply.common.presentation.navigation.MainShell] maps it to a route.
 */
sealed interface CategoriesDestination {
    data object Search : CategoriesDestination
    data object Addresses : CategoriesDestination
    data class ProductListing(val categoryId: String) : CategoriesDestination
}
