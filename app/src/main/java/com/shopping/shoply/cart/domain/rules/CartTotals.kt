package com.shopping.shoply.cart.domain.rules

import com.shopping.shoply.cart.domain.model.CartItem

/** Delivery is free once the subtotal reaches this ($35). */
const val FREE_DELIVERY_FROM_CENTS = 3_500L

/** The price summary of a cart. All money in cents. [itemCount] sums the quantities. */
data class CartTotals(
    val itemCount: Int = 0,
    val subtotalCents: Long = 0,
    val deliveryCents: Long = 0,
    val discountCents: Long = 0,
    val totalCents: Long = 0,
)

/**
 * Totals for [items].
 *
 * - Delivery: free from [FREE_DELIVERY_FROM_CENTS]; below it, each distinct store's fee once. A
 *   store missing from [storeFeesCents] counts as free.
 * - Discount: [percentOff] of the subtotal, rounded to the nearest cent.
 * - Total: subtotal + delivery − discount, never below 0.
 */
fun cartTotals(items: List<CartItem>, storeFeesCents: Map<String, Long>, percentOff: Int?): CartTotals {
    val subtotal = items.sumOf { it.priceCents * it.quantity }
    val delivery = if (items.isEmpty() || subtotal >= FREE_DELIVERY_FROM_CENTS) {
        0L
    } else {
        items.map { it.storeId }.distinct().sumOf { storeFeesCents[it] ?: 0L }
    }
    val percent = percentOff?.coerceIn(0, 100) ?: 0
    val discount = (subtotal * percent + 50) / 100
    return CartTotals(
        itemCount = items.sumOf { it.quantity },
        subtotalCents = subtotal,
        deliveryCents = delivery,
        discountCents = discount,
        totalCents = (subtotal + delivery - discount).coerceAtLeast(0),
    )
}
