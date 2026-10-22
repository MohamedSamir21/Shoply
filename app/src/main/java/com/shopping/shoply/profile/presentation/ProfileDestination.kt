package com.shopping.shoply.profile.presentation

/** Where the Profile tab can send the user. */
sealed interface ProfileDestination {
    data object EditProfile : ProfileDestination
    data object Settings : ProfileDestination
    data object Orders : ProfileDestination
    data object Wishlist : ProfileDestination
    data object FavoriteStores : ProfileDestination
    data object Addresses : ProfileDestination
    data object Payments : ProfileDestination
    data object Notifications : ProfileDestination
    data object Help : ProfileDestination
}
