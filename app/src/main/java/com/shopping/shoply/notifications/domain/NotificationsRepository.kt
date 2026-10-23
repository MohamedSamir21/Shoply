package com.shopping.shoply.notifications.domain

import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.notifications.domain.model.Notification
import kotlinx.coroutines.flow.Flow

/** The signed-in user's notifications. */
interface NotificationsRepository {

    /**
     * Number of unread notifications. Emits right away, then on every change. Never completes and
     * never throws: when offline it keeps the last known value.
     */
    fun observeUnreadCount(): Flow<Int>

    /** All notifications, newest first. Live like [observeUnreadCount], with the same guarantees. */
    fun observeNotifications(): Flow<List<Notification>>

    suspend fun markRead(id: String): Result<Unit, DataError>

    suspend fun markAllRead(): Result<Unit, DataError>
}
