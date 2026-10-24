package com.shopping.shoply.notifications.presentation

sealed interface NotificationsEvent {
    /** Show [message] in a snackbar. */
    data class ShowMessage(val message: NotificationsMessage) : NotificationsEvent
}

/** Snackbar messages. The screen maps each to its text. */
enum class NotificationsMessage {
    MARK_ALL_FAILED,
}
