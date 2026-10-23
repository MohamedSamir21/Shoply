package com.shopping.shoply.wishlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.cart.domain.usecase.QuickAddToCartUseCase
import com.shopping.shoply.catalog.domain.CatalogRepository
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.wishlist.domain.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the Wishlist screen: follows the saved ids live and loads only the products it hasn't
 * loaded yet, so saving or un-saving elsewhere updates the list without reloading it.
 *
 * - Remove hides the item at once; it comes back if the remove fails.
 * - Move to Cart adds 1 to the cart first; only then is the item un-saved.
 *
 * [state] starts eagerly, so its value is always current, even before anything collects it.
 */
@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val wishlistRepository: WishlistRepository,
    private val catalogRepository: CatalogRepository,
    private val quickAddToCart: QuickAddToCartUseCase,
) : ViewModel() {

    // Null until the first ids arrive.
    private val ids = MutableStateFlow<Set<String>?>(null)

    // Every id fetched so far; null means it has no product (deleted from the catalog).
    private val loaded = MutableStateFlow<Map<String, Product?>>(emptyMap())
    private val fetchFailed = MutableStateFlow(false)

    // Ids whose remove is still running: hidden until it finishes.
    private val removing = MutableStateFlow<Set<String>>(emptySet())

    val state: StateFlow<WishlistState> = combine(ids, loaded, fetchFailed, removing) { ids, loaded, failed, removing ->
        WishlistState(body = bodyOf(ids, loaded, failed, removing))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, WishlistState())

    private val _events = Channel<WishlistEvent>(Channel.BUFFERED)
    val events: Flow<WishlistEvent> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            wishlistRepository.observeIds().collect { saved ->
                ids.value = saved
                fetchMissing(saved)
            }
        }
    }

    fun onIntent(intent: WishlistIntent) {
        when (intent) {
            WishlistIntent.Retry -> {
                fetchFailed.value = false
                ids.value?.let(::fetchMissing)
            }
            is WishlistIntent.Remove -> remove(intent.productId)
            is WishlistIntent.MoveToCart -> moveToCart(intent.product)
        }
    }

    private fun fetchMissing(saved: Set<String>) {
        val missing = saved - loaded.value.keys
        if (missing.isEmpty()) return
        viewModelScope.launch {
            when (val result = catalogRepository.getProducts(missing)) {
                is Result.Success -> {
                    val byId = result.data.associateBy { it.id }
                    loaded.update { it + missing.associateWith { id -> byId[id] } }
                    fetchFailed.value = false
                }
                is Result.Error -> fetchFailed.value = true
            }
        }
    }

    private fun remove(productId: String) {
        if (productId in removing.value) return
        removing.update { it + productId }
        viewModelScope.launch {
            val message = when (unsave(productId)) {
                is Result.Success -> WishlistMessage.REMOVED
                is Result.Error -> WishlistMessage.REMOVE_FAILED
            }
            _events.send(WishlistEvent.ShowMessage(message))
        }
    }

    private fun moveToCart(product: Product) {
        if (product.id in removing.value) return
        viewModelScope.launch {
            val message = when (quickAddToCart(product)) {
                is Result.Error -> WishlistMessage.CART_FAILED
                is Result.Success -> {
                    removing.update { it + product.id }
                    // It's in the cart either way; a failed un-save just leaves it saved too.
                    unsave(product.id)
                    WishlistMessage.MOVED_TO_CART
                }
            }
            _events.send(WishlistEvent.ShowMessage(message))
        }
    }

    // Un-saves the product; it shows again afterwards only if it's still saved (the remove failed).
    private suspend fun unsave(productId: String) =
        wishlistRepository.remove(productId).also { removing.update { it - productId } }

    private fun bodyOf(
        ids: Set<String>?,
        loaded: Map<String, Product?>,
        failed: Boolean,
        removing: Set<String>,
    ): WishlistBody {
        if (ids == null) return WishlistBody.Loading
        val visible = ids - removing
        val products = visible.mapNotNull { loaded[it] }
        val allKnown = visible.all { it in loaded }
        return when {
            products.isNotEmpty() -> WishlistBody.Content(products)
            allKnown -> WishlistBody.Empty
            failed -> WishlistBody.Offline
            else -> WishlistBody.Loading
        }
    }
}
