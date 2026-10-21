package com.shopping.shoply.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.cart.domain.usecase.QuickAddToCartUseCase
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.time.CurrentHourProvider
import com.shopping.shoply.home.domain.rules.greetingFor
import com.shopping.shoply.home.domain.rules.nameParts
import com.shopping.shoply.home.domain.usecase.GetHomeFeedUseCase
import com.shopping.shoply.notifications.domain.NotificationsRepository
import com.shopping.shoply.profile.domain.ProfileRepository
import com.shopping.shoply.profile.domain.model.DeliveryAddress
import com.shopping.shoply.wishlist.domain.WishlistRepository
import com.shopping.shoply.wishlist.domain.usecase.ToggleWishlistUseCase
import com.shopping.shoply.wishlist.domain.usecase.WishlistChange
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the Home screen.
 *
 * - Loads the feed once when created; [HomeIntent.Retry] loads it again.
 * - Combines the feed with live data (wishlist, name, address, unread count) into one [state].
 *   A live update never reloads the feed.
 * - Wishlist: the heart flips in [state] immediately, then the save runs. On failure it flips back
 *   and [HomeMessage.WISHLIST_FAILED] is sent. A second tap on the same product while its save is
 *   still running is ignored.
 *
 * [state] starts eagerly, so its value is always current, even before anything collects it.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeFeed: GetHomeFeedUseCase,
    private val toggleWishlist: ToggleWishlistUseCase,
    private val quickAddToCart: QuickAddToCartUseCase,
    wishlistRepository: WishlistRepository,
    profileRepository: ProfileRepository,
    notificationsRepository: NotificationsRepository,
    currentHourProvider: CurrentHourProvider,
) : ViewModel() {

    private val body = MutableStateFlow<HomeBody>(HomeBody.Loading)

    // Taps still being saved: product id -> the saved state the user asked for.
    private val pendingWishlist = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    private val shownWishlist: Flow<Set<String>> =
        combine(wishlistRepository.observeIds().onStart { emit(emptySet()) }, pendingWishlist) { saved, pending ->
            saved + pending.filterValues { it }.keys - pending.filterValues { !it }.keys
        }

    private val greeting = greetingFor(currentHourProvider.currentHour())

    // combine waits for every flow's first value; the onStart defaults stop a silent live listener
    // (e.g. permission denied) from holding the whole screen on its initial state.
    val state: StateFlow<HomeState> = combine(
        body,
        profileRepository.observeDisplayName().onStart { emit("") },
        profileRepository.observePrimaryAddress().onStart { emit(null) },
        notificationsRepository.observeUnreadCount().onStart { emit(0) },
        shownWishlist,
    ) { body, name, address, unread, wishlist ->
        val (firstName, initials) = nameParts(name)
        HomeState(
            greeting = greeting,
            firstName = firstName,
            initials = initials,
            hasUnread = unread > 0,
            addressLine = address?.toLine(),
            body = body,
            wishlist = wishlist,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeState())

    private val _events = Channel<HomeEvent>(Channel.BUFFERED)
    val events: Flow<HomeEvent> = _events.receiveAsFlow()

    init {
        loadFeed()
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.Retry -> loadFeed()
            is HomeIntent.ToggleWishlist -> toggle(intent.productId)
            is HomeIntent.QuickAdd -> quickAdd(intent.product)
        }
    }

    private fun loadFeed() {
        body.value = HomeBody.Loading
        viewModelScope.launch {
            body.value = when (val result = getHomeFeed()) {
                is Result.Success ->
                    if (result.data.featuredStores.isEmpty()) HomeBody.Empty
                    else HomeBody.Content(result.data)
                is Result.Error -> HomeBody.Offline
            }
        }
    }

    private fun toggle(productId: String) {
        if (productId in pendingWishlist.value) return
        val isSaved = productId in state.value.wishlist
        pendingWishlist.update { it + (productId to !isSaved) }
        viewModelScope.launch {
            val result = toggleWishlist(productId, isSaved)
            // Clearing the pending tap shows the repository's ids again: the new value on success,
            // the old one (heart flips back) on failure.
            pendingWishlist.update { it - productId }
            val message = when (result) {
                is Result.Success -> when (result.data) {
                    WishlistChange.SAVED -> HomeMessage.SAVED_TO_WISHLIST
                    WishlistChange.REMOVED -> HomeMessage.REMOVED_FROM_WISHLIST
                }
                is Result.Error -> HomeMessage.WISHLIST_FAILED
            }
            _events.send(HomeEvent.ShowMessage(message))
        }
    }

    private fun quickAdd(product: Product) {
        viewModelScope.launch {
            val message = when (quickAddToCart(product)) {
                is Result.Success -> HomeMessage.ADDED_TO_CART
                is Result.Error -> HomeMessage.CART_FAILED
            }
            _events.send(HomeEvent.ShowMessage(message))
        }
    }
}

/** "Home · 24 Elm Street": the label, then line1 up to its first comma. */
private fun DeliveryAddress.toLine(): String = "$label · ${line1.substringBefore(",").trim()}"
