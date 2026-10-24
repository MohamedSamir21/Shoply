package com.shopping.shoply.notifications.presentation

import com.shopping.shoply.notifications.domain.model.Notification
import com.shopping.shoply.notifications.domain.model.NotificationType

/** Where tapping a notification can send the user. */
sealed interface NotificationDestination {
    data class Order(val id: String) : NotificationDestination

    data class Product(val id: String) : NotificationDestination

    /** A promo: back to the Home feed. */
    data object Home : NotificationDestination

    data object PaymentMethods : NotificationDestination
}

/** Null when the notification leads nowhere (general, or missing its order/product id). */
val Notification.destination: NotificationDestination?
    get() = when (type) {
        NotificationType.ORDER_CONFIRMED,
        NotificationType.ORDER_ON_THE_WAY,
        NotificationType.ORDER_DELIVERED -> targetId?.let(NotificationDestination::Order)
        NotificationType.PRICE_DROP -> targetId?.let(NotificationDestination::Product)
        NotificationType.PROMO -> NotificationDestination.Home
        NotificationType.PAYMENT_METHOD -> NotificationDestination.PaymentMethods
        NotificationType.GENERAL -> null
    }
