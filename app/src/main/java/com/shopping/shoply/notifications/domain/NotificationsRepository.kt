package com.shopping.shoply.notifications.domain

import kotlinx.coroutines.flow.Flow

/** The signed-in user's notifications. */
interface NotificationsRepository {

    /**
     * Number of unread notifications. Emits right away, then on every change. Never completes and
     * never throws: when offline it keeps the last known value.
     */
    fun observeUnreadCount(): Flow<Int>
}
