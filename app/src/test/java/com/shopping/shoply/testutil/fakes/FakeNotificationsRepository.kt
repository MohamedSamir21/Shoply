package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.notifications.domain.NotificationsRepository
import com.shopping.shoply.notifications.domain.model.Notification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

/**
 * Set [unreadCount] or [notifications] directly to simulate live changes. [notifications] starts
 * null: nothing emitted yet (still loading).
 */
class FakeNotificationsRepository(
    var result: Result<Unit, DataError> = Result.Success(Unit),
) : NotificationsRepository {
    val unreadCount = MutableStateFlow(0)
    val notifications = MutableStateFlow<List<Notification>?>(null)
    val markReadCalls = mutableListOf<String>()
    var markAllReadCalls = 0

    override fun observeUnreadCount() = unreadCount

    override fun observeNotifications() = notifications.filterNotNull()

    override suspend fun markRead(id: String): Result<Unit, DataError> {
        markReadCalls += id
        return result
    }

    override suspend fun markAllRead(): Result<Unit, DataError> {
        markAllReadCalls++
        return result
    }
}
