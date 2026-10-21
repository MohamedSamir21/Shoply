package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.notifications.domain.NotificationsRepository
import kotlinx.coroutines.flow.MutableStateFlow

/** Set [unreadCount] directly to simulate live changes. */
class FakeNotificationsRepository : NotificationsRepository {
    val unreadCount = MutableStateFlow(0)

    override fun observeUnreadCount() = unreadCount
}
