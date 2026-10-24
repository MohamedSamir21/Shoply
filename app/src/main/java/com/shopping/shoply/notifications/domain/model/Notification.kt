package com.shopping.shoply.notifications.domain.model

/**
 * One notification. [createdAtMillis] is epoch millis; [targetId] is the order or product it is
 * about, when it is about one.
 */
data class Notification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val createdAtMillis: Long,
    val read: Boolean,
    val targetId: String? = null,
)

/** Picks the row's icon and where tapping it goes. */
enum class NotificationType {
    ORDER_CONFIRMED,
    ORDER_ON_THE_WAY,
    ORDER_DELIVERED,
    PROMO,
    PRICE_DROP,
    PAYMENT_METHOD,

    /** Anything else, including types this version doesn't know yet. */
    GENERAL,
}
