package com.shopping.shoply.product.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.cart.domain.CartRepository
import com.shopping.shoply.cart.domain.rules.clampQuantity
import com.shopping.shoply.catalog.domain.CatalogRepository
import com.shopping.shoply.catalog.domain.model.Variant
import com.shopping.shoply.catalog.domain.rules.defaultVariant
import com.shopping.shoply.common.domain.Result
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
import javax.inject.Inject

/** Nav argument holding the product id. */
const val PRODUCT_ID_ARG = "productId"

/**
 * Backs the Product Details screen: loads the product, then its store and similar products
 * together (their failure only hides them).
 *
 * - The default colour and size are preselected; quantity stays within 1..20.
 * - Add to Cart adds the picked variant × quantity; a tap while it runs is ignored.
 * - The heart follows the saved ids live and flips at once on a tap; it flips back if saving fails.
 *
 * [state] starts eagerly, so its value is always current, even before anything collects it.
 */
@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogRepository: CatalogRepository,
    private val cartRepository: CartRepository,
    private val toggleWishlist: ToggleWishlistUseCase,
    wishlistRepository: WishlistRepository,
) : ViewModel() {

    private val productId: String = checkNotNull(savedStateHandle[PRODUCT_ID_ARG])

    private val body = MutableStateFlow<ProductDetailsBody>(ProductDetailsBody.Loading)
    private val selection = MutableStateFlow(Selection())
    private val addingToCart = MutableStateFlow(false)

    // The saved state the user tapped for, until the save finishes.
    private val pendingSaved = MutableStateFlow<Boolean?>(null)

    val state: StateFlow<ProductDetailsState> = combine(
        body,
        selection,
        addingToCart,
        wishlistRepository.observeIds().onStart { emit(emptySet()) },
        pendingSaved,
    ) { body, selection, adding, savedIds, pending ->
        ProductDetailsState(
            body = body,
            color = selection.color,
            size = selection.size,
            quantity = selection.quantity,
            isSaved = pending ?: (productId in savedIds),
            addingToCart = adding,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ProductDetailsState())

    private val _events = Channel<ProductDetailsEvent>(Channel.BUFFERED)
    val events: Flow<ProductDetailsEvent> = _events.receiveAsFlow()

    init {
        load()
    }

    fun onIntent(intent: ProductDetailsIntent) {
        when (intent) {
            ProductDetailsIntent.Retry -> load()
            is ProductDetailsIntent.SelectColor -> selection.update { it.copy(color = intent.name) }
            is ProductDetailsIntent.SelectSize -> selection.update { it.copy(size = intent.size) }
            ProductDetailsIntent.Increase -> selection.update { it.copy(quantity = clampQuantity(it.quantity + 1)) }
            ProductDetailsIntent.Decrease -> selection.update { it.copy(quantity = clampQuantity(it.quantity - 1)) }
            ProductDetailsIntent.ToggleWishlist -> toggle()
            ProductDetailsIntent.AddToCart -> addToCart()
        }
    }

    private fun load() {
        body.value = ProductDetailsBody.Loading
        viewModelScope.launch {
            val product = when (val result = catalogRepository.getProduct(productId)) {
                is Result.Success -> result.data
                is Result.Error -> {
                    body.value = ProductDetailsBody.Offline
                    return@launch
                }
            }
            val variant = defaultVariant(product)
            selection.value = Selection(color = variant.color, size = variant.size)

            val store = async { catalogRepository.getStore(product.storeId) }
            val similar = async { catalogRepository.getProductsInCategory(product.categoryId, SIMILAR_LIMIT + 1) }
            body.value = ProductDetailsBody.Content(
                product = product,
                store = (store.await() as? Result.Success)?.data,
                similar = (similar.await() as? Result.Success)?.data.orEmpty()
                    .filter { it.id != productId }
                    .take(SIMILAR_LIMIT),
            )
        }
    }

    private fun addToCart() {
        val product = (body.value as? ProductDetailsBody.Content)?.product ?: return
        if (addingToCart.value) return
        addingToCart.value = true
        val picked = selection.value
        viewModelScope.launch {
            val result = cartRepository.add(product, Variant(picked.color, picked.size), picked.quantity)
            addingToCart.value = false
            val message = when (result) {
                is Result.Success -> ProductDetailsMessage.ADDED_TO_CART
                is Result.Error -> ProductDetailsMessage.CART_FAILED
            }
            _events.send(ProductDetailsEvent.ShowMessage(message))
        }
    }

    private fun toggle() {
        if (pendingSaved.value != null) return
        val isSaved = state.value.isSaved
        pendingSaved.value = !isSaved
        viewModelScope.launch {
            val result = toggleWishlist(productId, isSaved)
            // Clearing the pending tap shows the repository's ids again: the new value on success,
            // the old one (heart flips back) on failure.
            pendingSaved.value = null
            val message = when (result) {
                is Result.Success -> when (result.data) {
                    WishlistChange.SAVED -> ProductDetailsMessage.SAVED
                    WishlistChange.REMOVED -> ProductDetailsMessage.REMOVED
                }
                is Result.Error -> ProductDetailsMessage.WISHLIST_FAILED
            }
            _events.send(ProductDetailsEvent.ShowMessage(message))
        }
    }

    private data class Selection(val color: String? = null, val size: String? = null, val quantity: Int = 1)

    private companion object {
        const val SIMILAR_LIMIT = 4
    }
}
