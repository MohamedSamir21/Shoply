package com.shopping.shoply.notifications.domain.rules

import com.shopping.shoply.notifications.domain.model.Notification

/** The notifications under the "Today" and "Earlier" headers. */
data class NotificationGroups(
    val today: List<Notification>,
    val earlier: List<Notification>,
)

/**
 * Splits [notifications] into today (created at or after [startOfTodayMillis]) and earlier.
 * Each group keeps the input order.
 */
fun splitByDay(notifications: List<Notification>, startOfTodayMillis: Long): NotificationGroups {
    val (today, earlier) = notifications.partition { it.createdAtMillis >= startOfTodayMillis }
    return NotificationGroups(today = today, earlier = earlier)
}
