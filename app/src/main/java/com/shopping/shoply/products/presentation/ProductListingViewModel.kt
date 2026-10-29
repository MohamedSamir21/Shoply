package com.shopping.shoply.products.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.cart.domain.usecase.QuickAddToCartUseCase
import com.shopping.shoply.catalog.domain.CatalogRepository
import com.shopping.shoply.catalog.domain.model.Category
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.rules.ProductFilters
import com.shopping.shoply.catalog.domain.rules.ProductSort
import com.shopping.shoply.catalog.domain.rules.applyFilters
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.products.domain.rules.inSubcategory
import com.shopping.shoply.products.domain.rules.subcategoriesOf
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

const val LISTING_CATEGORY_ID_ARG = "categoryId"

/**
 * Backs Product Listing: one category's products, narrowed by the subcategory chip, then the
 * filters, then sorted. Loads once; [ProductListingIntent.Retry] loads again.
 */
@HiltViewModel
class ProductListingViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogRepository: CatalogRepository,
    private val toggleWishlist: ToggleWishlistUseCase,
    private val quickAddToCart: QuickAddToCartUseCase,
    wishlistRepository: WishlistRepository,
) : ViewModel() {

    private val categoryId: String = checkNotNull(savedStateHandle[LISTING_CATEGORY_ID_ARG])

    private val loaded = MutableStateFlow<Loaded?>(null)
    private val status = MutableStateFlow(ListingBody.Loading)
    private val input = MutableStateFlow(Input())

    private val pendingWishlist = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val shownWishlist: Flow<Set<String>> =
        combine(wishlistRepository.observeIds().onStart { emit(emptySet()) }, pendingWishlist) { saved, pending ->
            saved + pending.filterValues { it }.keys - pending.filterValues { !it }.keys
        }

    val state: StateFlow<ProductListingState> =
        combine(loaded, status, input, shownWishlist) { loaded, status, input, wishlist ->
            val products = loaded?.products.orEmpty()
            val matches = inSubcategory(products, input.subcategory)
            ProductListingState(
                body = status,
                category = loaded?.category,
                subcategories = subcategoriesOf(products),
                subcategory = input.subcategory,
                matches = matches,
                results = applyFilters(matches, input.filters, input.sort, loaded?.freeDeliveryStoreIds.orEmpty()),
                freeDeliveryStoreIds = loaded?.freeDeliveryStoreIds.orEmpty(),
                filters = input.filters,
                sort = input.sort,
                sheet = input.sheet,
                wishlist = wishlist,
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, ProductListingState())

    private val _events = Channel<ProductListingEvent>(Channel.BUFFERED)
    val events: Flow<ProductListingEvent> = _events.receiveAsFlow()

    init {
        load()
    }

    fun onIntent(intent: ProductListingIntent) {
        when (intent) {
            is ProductListingIntent.SelectSubcategory -> input.update { it.copy(subcategory = intent.subcategory) }
            is ProductListingIntent.OpenSheet -> input.update { it.copy(sheet = intent.sheet) }
            ProductListingIntent.CloseSheet -> input.update { it.copy(sheet = ListingSheet.NONE) }
            is ProductListingIntent.ApplyFilters -> input.update { it.copy(filters = intent.filters, sheet = ListingSheet.NONE) }
            ProductListingIntent.ResetFilters -> input.update { it.copy(filters = ProductFilters()) }
            is ProductListingIntent.SelectSort -> input.update { it.copy(sort = intent.sort, sheet = ListingSheet.NONE) }
            is ProductListingIntent.ToggleWishlist -> toggleProduct(intent.productId)
            is ProductListingIntent.QuickAdd -> quickAdd(intent.product)
            ProductListingIntent.Retry -> load()
        }
    }

    private fun load() {
        status.value = ListingBody.Loading
        viewModelScope.launch {
            val products = async { catalogRepository.getProductsInCategory(categoryId, Int.MAX_VALUE) }
            val categories = async { catalogRepository.getCategories() }
            val stores = async { catalogRepository.getAllStores() }
            val productsResult = products.await()
            // The title and the free-delivery filter are extras; the list works without them.
            val category = (categories.await() as? Result.Success)?.data?.firstOrNull { it.id == categoryId }
            val freeDelivery = (stores.await() as? Result.Success)?.data
                ?.filter { it.deliveryFeeCents == 0L }?.map { it.id }?.toSet().orEmpty()
            if (productsResult is Result.Success) {
                loaded.value = Loaded(category, productsResult.data, freeDelivery)
                status.value = if (productsResult.data.isEmpty()) ListingBody.Empty else ListingBody.Content
            } else {
                status.value = ListingBody.Error
            }
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
                    WishlistChange.SAVED -> ListingMessage.SAVED_TO_WISHLIST
                    WishlistChange.REMOVED -> ListingMessage.REMOVED_FROM_WISHLIST
                }
                is Result.Error -> ListingMessage.WISHLIST_FAILED
            }
            _events.send(ProductListingEvent.ShowMessage(message))
        }
    }

    private fun quickAdd(product: Product) {
        viewModelScope.launch {
            val message = when (quickAddToCart(product)) {
                is Result.Success -> ListingMessage.ADDED_TO_CART
                is Result.Error -> ListingMessage.CART_FAILED
            }
            _events.send(ProductListingEvent.ShowMessage(message))
        }
    }

    private data class Loaded(val category: Category?, val products: List<Product>, val freeDeliveryStoreIds: Set<String>)

    private data class Input(
        val subcategory: String? = null,
        val filters: ProductFilters = ProductFilters(),
        val sort: ProductSort = ProductSort.RELEVANCE,
        val sheet: ListingSheet = ListingSheet.NONE,
    )
}
