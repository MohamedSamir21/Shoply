package com.shopping.shoply.orders.domain.model

import com.shopping.shoply.cart.domain.rules.CartTotals
import com.shopping.shoply.catalog.domain.model.CatalogIcon
import com.shopping.shoply.checkout.domain.model.CardBrand
import com.shopping.shoply.checkout.domain.model.DeliveryOption
import com.shopping.shoply.profile.domain.model.DeliveryAddress

/**
 * A placed order, as saved at checkout. [address] and [payment] are copies, so later edits to the
 * user's addresses or cards don't change it. [etaAtMillis] is set once a courier has the order.
 * [id] doubles as the order number shown to the user ("SH-20481").
 */
data class Order(
    val id: String,
    val items: List<OrderItem>,
    val address: DeliveryAddress,
    val delivery: DeliveryOption,
    val payment: OrderPayment,
    val promoCode: String?,
    val totals: CartTotals,
    val status: OrderStatus,
    val timeline: List<TimelineEntry>,
    val etaAtMillis: Long?,
    val createdAtMillis: Long,
)

/** One line of an order: the product as it was when ordered. */
data class OrderItem(
    val productId: String,
    val name: String,
    val icon: CatalogIcon,
    val storeId: String,
    val priceCents: Long,
    val color: String?,
    val size: String?,
    val quantity: Int,
)

/** In the order an order moves through, then [CANCELLED]. */
enum class OrderStatus { PLACED, CONFIRMED, PACKED, ON_THE_WAY, DELIVERED, CANCELLED }

sealed interface OrderPayment {
    data class Card(val brand: CardBrand, val last4: String) : OrderPayment

    data class Installments(val brand: CardBrand, val last4: String, val months: Int) : OrderPayment

    data object CashOnDelivery : OrderPayment
}

/** When the order reached [status]. [note] is an optional detail, e.g. the courier's name. */
data class TimelineEntry(val status: OrderStatus, val atMillis: Long, val note: String?)
