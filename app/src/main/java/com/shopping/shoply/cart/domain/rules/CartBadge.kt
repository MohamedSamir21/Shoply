package com.shopping.shoply.cart.domain.rules

/**
 * The text on the cart tab's badge for the total number of items in the cart.
 *
 * @return null to hide the badge when [totalQuantity] is 0 or less, the number itself up to 99,
 *   and "99+" above 99.
 */
fun cartBadge(totalQuantity: Int): String? = when {
    totalQuantity <= 0 -> null
    totalQuantity > 99 -> "99+"
    else -> totalQuantity.toString()
}
