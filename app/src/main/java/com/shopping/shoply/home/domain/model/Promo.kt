package com.shopping.shoply.home.domain.model

/**
 * A banner in the Home carousel.
 *
 * @property kicker the small uppercase line above the title, e.g. "THIS WEEK".
 * @property target where the button goes. It's data, so banners can change without an app update.
 */
data class Promo(
    val id: String,
    val kicker: String,
    val title: String,
    val ctaLabel: String,
    val art: PromoArt,
    val target: PromoTarget,
)

/** The illustration on the right of a banner. [UNKNOWN] is the fallback for new values. */
enum class PromoArt { TRUCK, SPLIT, PLANT, UNKNOWN }

/** Where tapping a banner's button navigates. */
sealed interface PromoTarget {
    data class CategoryListing(val categoryId: String) : PromoTarget
    data class Store(val storeId: String) : PromoTarget
    data object Help : PromoTarget
}
