package com.shopping.shoply.store.domain.rules

import com.shopping.shoply.catalog.domain.model.Product

enum class StoreTab { ALL, TOP_RATED, OFFERS }

/**
 * The products shown under [tab]:
 * - [StoreTab.ALL]: all of them, in the given order.
 * - [StoreTab.TOP_RATED]: all of them, highest rating first (ties keep the given order).
 * - [StoreTab.OFFERS]: only the discounted ones (old price above the price), in the given order.
 */
fun storeProducts(products: List<Product>, tab: StoreTab): List<Product> = when (tab) {
    StoreTab.ALL -> products
    StoreTab.TOP_RATED -> products.sortedByDescending { it.rating }
    StoreTab.OFFERS -> products.filter { product -> product.oldPriceCents?.let { it > product.priceCents } == true }
}

/** Whether a store is open now. Times are minutes since midnight. */
sealed interface OpeningStatus {
    /** "Open until 9 PM". */
    data class OpenUntil(val closesAtMinutes: Int) : OpeningStatus

    /** "Closed · Opens at 10 AM". */
    data class ClosedOpensAt(val opensAtMinutes: Int) : OpeningStatus
}

/** Open from [opensAtMinutes] up to, but not including, [closesAtMinutes]. */
fun openingStatus(
    opensAtMinutes: Int,
    closesAtMinutes: Int,
    nowMinutes: Int
): OpeningStatus =
    if (nowMinutes in opensAtMinutes until closesAtMinutes) OpeningStatus.OpenUntil(closesAtMinutes)
    else OpeningStatus.ClosedOpensAt(opensAtMinutes)
