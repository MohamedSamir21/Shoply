package com.shopping.shoply.notifications.presentation.preview

import com.shopping.shoply.notifications.domain.model.Notification
import com.shopping.shoply.notifications.domain.model.NotificationType

/** Sample notifications from the design, for previews only. */
internal object NotificationsPreviewData {
    private const val HOUR = 60 * 60 * 1000L
    private val now = System.currentTimeMillis()

    val onTheWay = Notification(
        id = "n1",
        type = NotificationType.ORDER_ON_THE_WAY,
        title = "Your order is on the way",
        body = "Order #SH-20481 left Volt Electronics and will arrive in about 25 minutes.",
        createdAtMillis = now,
        read = false,
        targetId = "SH-20481",
    )
    val promo = Notification(
        id = "n2",
        type = NotificationType.PROMO,
        title = "Free delivery this week",
        body = "Orders over $25 ship free from all stores near you until Sunday.",
        createdAtMillis = now,
        read = true,
    )
    val priceDrop = Notification(
        id = "n3",
        type = NotificationType.PRICE_DROP,
        title = "Price drop on your wishlist",
        body = "Wireless Noise-Cancelling Headphones is now $89.00.",
        createdAtMillis = now - 24 * HOUR,
        read = true,
        targetId = "p1",
    )
    val delivered = Notification(
        id = "n4",
        type = NotificationType.ORDER_DELIVERED,
        title = "Order delivered",
        body = "Order #SH-20377 was delivered. Enjoy your purchase!",
        createdAtMillis = now - 5 * 24 * HOUR,
        read = true,
        targetId = "SH-20377",
    )

    val today = listOf(onTheWay, promo)
    val earlier = listOf(priceDrop, delivered)
}
