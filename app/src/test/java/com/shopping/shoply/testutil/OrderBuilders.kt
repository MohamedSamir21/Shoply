package com.shopping.shoply.testutil

import com.shopping.shoply.cart.domain.rules.CartTotals
import com.shopping.shoply.catalog.domain.model.CatalogIcon
import com.shopping.shoply.checkout.domain.model.CardBrand
import com.shopping.shoply.checkout.domain.model.DeliveryOption
import com.shopping.shoply.orders.domain.model.Order
import com.shopping.shoply.orders.domain.model.OrderItem
import com.shopping.shoply.orders.domain.model.OrderPayment
import com.shopping.shoply.orders.domain.model.OrderStatus

// Test data builders for orders: every field has a valid default.
//   anOrder(id = "SH-2", status = OrderStatus.DELIVERED, createdAtMillis = 2)

fun anOrderItem(
    productId: String = "p1",
    color: String? = "Sand",
    size: String? = "M",
    quantity: Int = 1,
) = OrderItem(productId, "Product $productId", CatalogIcon.HEADPHONES, "store1", 1000, color, size, quantity)

fun anOrder(
    id: String = "SH-1",
    status: OrderStatus = OrderStatus.PLACED,
    items: List<OrderItem> = listOf(anOrderItem()),
    createdAtMillis: Long = 1,
) = Order(
    id = id,
    items = items,
    address = anAddress(),
    delivery = DeliveryOption.Express,
    payment = OrderPayment.Card(CardBrand.VISA, "4821"),
    promoCode = null,
    totals = CartTotals(itemCount = 1, subtotalCents = 1000, totalCents = 1000),
    status = status,
    timeline = emptyList(),
    etaAtMillis = null,
    createdAtMillis = createdAtMillis,
)
