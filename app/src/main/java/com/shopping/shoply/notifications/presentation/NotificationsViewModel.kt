package com.shopping.shoply.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.time.StartOfDayProvider
import com.shopping.shoply.notifications.domain.NotificationsRepository
import com.shopping.shoply.notifications.domain.model.Notification
import com.shopping.shoply.notifications.domain.rules.splitByDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the Notifications screen: follows the list live and splits it into Today and Earlier.
 * Opening an unread one marks it read; the live list then updates the row.
 *
 * [state] starts eagerly, so its value is always current, even before anything collects it.
 */
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notificationsRepository: NotificationsRepository,
    private val startOfDayProvider: StartOfDayProvider,
) : ViewModel() {

    val state: StateFlow<NotificationsState> = notificationsRepository.observeNotifications()
        .map(::stateOf)
        .stateIn(viewModelScope, SharingStarted.Eagerly, NotificationsState())

    private val _events = Channel<NotificationsEvent>(Channel.BUFFERED)
    val events: Flow<NotificationsEvent> = _events.receiveAsFlow()

    fun onIntent(intent: NotificationsIntent) {
        when (intent) {
            is NotificationsIntent.Open -> open(intent.notification)
            NotificationsIntent.MarkAllRead -> markAllRead()
        }
    }

    // Fire and forget: a failed mark just leaves the row unread.
    private fun open(notification: Notification) {
        if (notification.read) return
        viewModelScope.launch { notificationsRepository.markRead(notification.id) }
    }

    private fun markAllRead() {
        viewModelScope.launch {
            if (notificationsRepository.markAllRead() is Result.Error) {
                _events.send(NotificationsEvent.ShowMessage(NotificationsMessage.MARK_ALL_FAILED))
            }
        }
    }

    private fun stateOf(notifications: List<Notification>): NotificationsState {
        val body = if (notifications.isEmpty()) {
            NotificationsBody.Empty
        } else {
            val groups = splitByDay(notifications, startOfDayProvider.startOfTodayMillis())
            NotificationsBody.Content(today = groups.today, earlier = groups.earlier)
        }
        return NotificationsState(body = body, hasUnread = notifications.any { !it.read })
    }
}
