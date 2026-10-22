package com.shopping.shoply.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.home.domain.rules.nameParts
import com.shopping.shoply.notifications.domain.NotificationsRepository
import com.shopping.shoply.profile.domain.ProfileRepository
import com.shopping.shoply.profile.domain.SessionRepository
import com.shopping.shoply.profile.domain.model.Account
import com.shopping.shoply.wishlist.domain.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the Profile tab: the live account, wishlist count and unread count, plus the log-out
 * confirmation. Confirming signs out and sends [ProfileEvent.SignedOut].
 *
 * [state] starts eagerly, so its value is always current, even before anything collects it.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    profileRepository: ProfileRepository,
    wishlistRepository: WishlistRepository,
    notificationsRepository: NotificationsRepository,
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val showLogoutDialog = MutableStateFlow(false)

    // combine waits for every flow's first value; the onStart defaults stop a silent live listener
    // (e.g. permission denied) from holding the whole screen on its initial state.
    val state: StateFlow<ProfileState> = combine(
        profileRepository.observeAccount().onStart { emit(Account(name = "", email = "", phone = null)) },
        wishlistRepository.observeIds().onStart { emit(emptySet()) },
        notificationsRepository.observeUnreadCount().onStart { emit(0) },
        showLogoutDialog,
    ) { account, wishlist, unread, dialog ->
        ProfileState(
            name = account.name,
            initials = nameParts(account.name).second,
            email = account.email,
            phone = account.phone,
            wishlistCount = wishlist.size,
            unreadNotificationCount = unread,
            showLogoutDialog = dialog,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ProfileState())

    private val _events = Channel<ProfileEvent>(Channel.BUFFERED)
    val events: Flow<ProfileEvent> = _events.receiveAsFlow()

    fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.LogOutClick -> showLogoutDialog.value = true
            ProfileIntent.LogOutDismiss -> showLogoutDialog.value = false
            ProfileIntent.LogOutConfirm -> {
                showLogoutDialog.value = false
                sessionRepository.signOut()
                viewModelScope.launch { _events.send(ProfileEvent.SignedOut) }
            }
        }
    }
}
