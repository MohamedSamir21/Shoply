package com.shopping.shoply.store.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.cart.domain.usecase.QuickAddToCartUseCase
import com.shopping.shoply.catalog.domain.CatalogRepository
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.time.NowProvider
import com.shopping.shoply.store.domain.FavoriteStoresRepository
import com.shopping.shoply.store.domain.rules.openingStatus
import com.shopping.shoply.store.domain.rules.storeProducts
import com.shopping.shoply.wishlist.domain.WishlistRepository
import com.shopping.shoply.wishlist.domain.usecase.ToggleWishlistUseCase
import com.shopping.shoply.wishlist.domain.usecase.WishlistChange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
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
import java.util.Calendar
import javax.inject.Inject

const val STORE_ID_ARG = "storeId"

/**
 * Backs the Store screen.
 *
 * - Loads the store and its products together; either failing shows [StoreBody.Error], and
 *   [StoreIntent.Retry] loads both again.
 * - The favorite and wishlist hearts flip in [state] at once, then the save runs. On failure they
 *   flip back and a *_FAILED message is sent. A second tap while a save is running is ignored.
 */
@HiltViewModel
class StoreViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogRepository: CatalogRepository,
    private val favoriteStoresRepository: FavoriteStoresRepository,
    private val toggleWishlist: ToggleWishlistUseCase,
    private val quickAddToCart: QuickAddToCartUseCase,
    wishlistRepository: WishlistRepository,
    private val nowProvider: NowProvider,
) : ViewModel() {

    private val storeId: String = checkNotNull(savedStateHandle[STORE_ID_ARG])

    // Content here holds every product; state shows the selected tab's.
    private val loaded = MutableStateFlow<StoreBody>(StoreBody.Loading)
    private val tab = MutableStateFlow(StoreState().tab)

    // The favorite value the user asked for while its save runs; null when nothing is pending.
    private val pendingFavorite = MutableStateFlow<Boolean?>(null)
    private val shownFavorite: Flow<Boolean> =
        combine(favoriteStoresRepository.observeIds().onStart { emit(emptySet()) }, pendingFavorite) { ids, pending ->
            pending ?: (storeId in ids)
        }

    // Taps still being saved: product id -> the saved state the user asked for.
    private val pendingWishlist = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val shownWishlist: Flow<Set<String>> =
        combine(wishlistRepository.observeIds().onStart { emit(emptySet()) }, pendingWishlist) { saved, pending ->
            saved + pending.filterValues { it }.keys - pending.filterValues { !it }.keys
        }

    val state: StateFlow<StoreState> =
        combine(loaded, tab, shownFavorite, shownWishlist) { loaded, tab, favorite, wishlist ->
            val content = loaded as? StoreBody.Content
            StoreState(
                body = content?.copy(products = storeProducts(content.products, tab)) ?: loaded,
                tab = tab,
                opening = content?.store?.let { openingStatus(it.opensAtMinutes, it.closesAtMinutes, nowMinutes()) },
                favorite = favorite,
                wishlist = wishlist,
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, StoreState())

    private val _events = Channel<StoreEvent>(Channel.BUFFERED)
    val events: Flow<StoreEvent> = _events.receiveAsFlow()

    init {
        load()
    }

    fun onIntent(intent: StoreIntent) {
        when (intent) {
            is StoreIntent.SelectTab -> tab.value = intent.tab
            StoreIntent.ToggleFavorite -> toggleFavorite()
            is StoreIntent.ToggleWishlist -> toggleProduct(intent.productId)
            is StoreIntent.QuickAdd -> quickAdd(intent.product)
            StoreIntent.Retry -> load()
        }
    }

    private fun load() {
        loaded.value = StoreBody.Loading
        viewModelScope.launch {
            val store = async { catalogRepository.getStore(storeId) }
            val products = async { catalogRepository.getProductsInStore(storeId) }
            val storeResult = store.await()
            val productsResult = products.await()
            loaded.value =
                if (storeResult is Result.Success && productsResult is Result.Success) {
                    StoreBody.Content(storeResult.data, productsResult.data)
                } else {
                    StoreBody.Error
                }
        }
    }

    private fun toggleFavorite() {
        if (pendingFavorite.value != null) return
        val favorite = !state.value.favorite
        pendingFavorite.value = favorite
        viewModelScope.launch {
            val result = favoriteStoresRepository.setFavorite(storeId, favorite)
            // Clearing the pending tap shows the repository's ids again (the old value on failure).
            pendingFavorite.value = null
            if (result is Result.Error) _events.send(StoreEvent.ShowMessage(StoreMessage.FAVORITE_FAILED))
        }
    }

    private fun toggleProduct(productId: String) {
        if (productId in pendingWishlist.value) return
        val isSaved = productId in state.value.wishlist
        pendingWishlist.update { it + (productId to !isSaved) }
        viewModelScope.launch {
            val result = toggleWishlist(productId, isSaved)
            pendingWishlist.update { it - productId }
            val message = when (result) {
                is Result.Success -> when (result.data) {
                    WishlistChange.SAVED -> StoreMessage.SAVED_TO_WISHLIST
                    WishlistChange.REMOVED -> StoreMessage.REMOVED_FROM_WISHLIST
                }
                is Result.Error -> StoreMessage.WISHLIST_FAILED
            }
            _events.send(StoreEvent.ShowMessage(message))
        }
    }

    private fun quickAdd(product: Product) {
        viewModelScope.launch {
            val message = when (quickAddToCart(product)) {
                is Result.Success -> StoreMessage.ADDED_TO_CART
                is Result.Error -> StoreMessage.CART_FAILED
            }
            _events.send(StoreEvent.ShowMessage(message))
        }
    }

    private fun nowMinutes(): Int = Calendar.getInstance().run {
        timeInMillis = nowProvider.nowMillis()
        get(Calendar.HOUR_OF_DAY) * 60 + get(Calendar.MINUTE)
    }
}
