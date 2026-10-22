package com.shopping.shoply.home.presentation

import com.shopping.shoply.home.domain.model.HomeFeed
import com.shopping.shoply.home.domain.rules.Greeting

/**
 * Everything the Home screen shows. The header fields are always present, even while the body
 * is loading or offline.
 *
 * @property firstName from [com.shopping.shoply.home.domain.rules.nameParts]; "there" when the name is blank.
 * @property initials for the avatar; null when the name is blank.
 * @property addressLine "Home · 24 Elm Street" (label · part of line1 before the first comma);
 *   null when the user has no primary address.
 * @property wishlist ids of products whose heart is filled, including taps still being saved.
 */
data class HomeState(
    val greeting: Greeting = Greeting.MORNING,
    val firstName: String = "",
    val initials: String? = null,
    val hasUnreadNotifications: Boolean = false,
    val addressLine: String? = null,
    val body: HomeBody = HomeBody.Loading,
    val wishlist: Set<String> = emptySet(),
)

/** The part of the screen below the search bar. */
sealed interface HomeBody {
    data object Loading : HomeBody

    /** Loaded, but there are no featured stores near the user. */
    data object Empty : HomeBody

    /** Any load error (network or other). */
    data object Offline : HomeBody

    data class Content(val feed: HomeFeed) : HomeBody
}
