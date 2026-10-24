package com.shopping.shoply.notifications.presentation

import com.shopping.shoply.notifications.domain.model.Notification

sealed interface NotificationsIntent {
    /** Tapped a row: marks it read. The screen navigates to its [destination]. */
    data class Open(val notification: Notification) : NotificationsIntent

    data object MarkAllRead : NotificationsIntent
}
