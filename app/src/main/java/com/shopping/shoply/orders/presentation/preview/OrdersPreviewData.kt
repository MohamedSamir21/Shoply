package com.shopping.shoply.orders.presentation.preview

import com.shopping.shoply.cart.domain.rules.CartTotals
import com.shopping.shoply.catalog.domain.model.CatalogIcon
import com.shopping.shoply.checkout.domain.model.CardBrand
import com.shopping.shoply.checkout.domain.model.DeliveryOption
import com.shopping.shoply.checkout.presentation.preview.CheckoutPreviewData
import com.shopping.shoply.orders.domain.model.Order
import com.shopping.shoply.orders.domain.model.OrderItem
import com.shopping.shoply.orders.domain.model.OrderPayment
import com.shopping.shoply.orders.domain.model.OrderStatus
import com.shopping.shoply.orders.domain.model.TimelineEntry
import java.util.Calendar

/** Sample orders for previews, like the test user's seeded ones. */
internal object OrdersPreviewData {
    private fun at(day: Int, hour: Int, minute: Int) = Calendar.getInstance().apply {
        clear()
        set(2026, Calendar.OCTOBER, day, hour, minute)
    }.timeInMillis

    private val headphones = OrderItem("p1", "Wireless Over-Ear Headphones", CatalogIcon.HEADPHONES, "volt", 8900, "Sand", null, 1)
    private val speaker = OrderItem("p10", "Portable Bluetooth Speaker", CatalogIcon.SPEAKER, "volt", 7200, "Stone", null, 1)
    private val shirt = OrderItem("p2", "Relaxed Linen Shirt", CatalogIcon.SHIRT, "thread", 4200, "Navy", "L", 1)
    private val mat = OrderItem("p12", "Cork Yoga Mat", CatalogIcon.MAT, "stride", 3600, null, null, 1)

    val onTheWay = Order(
        id = "SH-20481",
        items = listOf(headphones, speaker),
        address = CheckoutPreviewData.home,
        delivery = DeliveryOption.Express,
        payment = OrderPayment.Card(CardBrand.VISA, "4821"),
        promoCode = null,
        totals = CartTotals(itemCount = 2, subtotalCents = 16100, deliveryCents = 499, totalCents = 16599),
        status = OrderStatus.ON_THE_WAY,
        timeline = listOf(
            TimelineEntry(OrderStatus.PLACED, at(18, 13, 2), null),
            TimelineEntry(OrderStatus.CONFIRMED, at(18, 13, 5), null),
            TimelineEntry(OrderStatus.PACKED, at(18, 13, 20), null),
            TimelineEntry(OrderStatus.ON_THE_WAY, at(18, 13, 34), "Daniel picked up your order and is heading to you."),
        ),
        etaAtMillis = at(18, 13, 46),
        createdAtMillis = at(18, 13, 2),
    )

    val delivered = onTheWay.copy(
        id = "SH-20412",
        items = listOf(shirt),
        address = CheckoutPreviewData.work,
        delivery = DeliveryOption.Standard(at(10, 0, 0), 14),
        payment = OrderPayment.Installments(CardBrand.VISA, "4821", 3),
        totals = CartTotals(itemCount = 1, subtotalCents = 4200, totalCents = 4200),
        status = OrderStatus.DELIVERED,
        timeline = listOf(TimelineEntry(OrderStatus.DELIVERED, at(10, 14, 52), null)),
        etaAtMillis = null,
        createdAtMillis = at(9, 16, 40),
    )

    val cancelled = onTheWay.copy(
        id = "SH-20341",
        items = listOf(mat),
        payment = OrderPayment.Card(CardBrand.MASTERCARD, "0937"),
        totals = CartTotals(itemCount = 1, subtotalCents = 3600, totalCents = 3600),
        status = OrderStatus.CANCELLED,
        timeline = emptyList(),
        etaAtMillis = null,
        createdAtMillis = at(24, 15, 0),
    )

    val all = listOf(onTheWay, delivered, cancelled)
}
