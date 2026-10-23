package com.shopping.shoply.notifications.presentation

import com.shopping.shoply.notifications.domain.model.Notification

/** What the Notifications screen shows. [hasUnread] shows "Mark all read". */
data class NotificationsState(
    val body: NotificationsBody = NotificationsBody.Loading,
    val hasUnread: Boolean = false,
)

sealed interface NotificationsBody {
    data object Loading : NotificationsBody

    data object Empty : NotificationsBody

    /** Newest first under each header; one of the two can be empty. */
    data class Content(
        val today: List<Notification>,
        val earlier: List<Notification>,
    ) : NotificationsBody
}
