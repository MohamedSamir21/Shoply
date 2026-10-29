package com.shopping.shoply.search.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.cart.domain.usecase.QuickAddToCartUseCase
import com.shopping.shoply.catalog.domain.CatalogRepository
import com.shopping.shoply.catalog.domain.model.Category
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.model.Store
import com.shopping.shoply.catalog.domain.rules.PriceRange
import com.shopping.shoply.catalog.domain.rules.ProductFilters
import com.shopping.shoply.catalog.domain.rules.ProductSort
import com.shopping.shoply.catalog.domain.rules.applyFilters
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.search.domain.RecentSearchesRepository
import com.shopping.shoply.search.domain.rules.matchProducts
import com.shopping.shoply.search.domain.rules.matchStores
import com.shopping.shoply.search.domain.rules.withRecent
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

/** true opens Search browsing every product with the Filters sheet up (Home's filter button). */
const val SEARCH_OPEN_FILTERS_ARG = "openFilters"

private const val IDLE_STORES = 4
private const val MAX_SUGGESTIONS = 6

/**
 * Backs Search. The whole catalog (products, stores, categories) loads once, and every query is
 * matched in memory: the catalog is small and Firestore has no text search.
 *
 * - Submit saves the query to the recent searches and shows its results.
 * - Filters and sort apply to the results; editing the query hides them until the next Submit.
 * - Wishlist and quick add behave like Home's.
 */
@HiltViewModel
class SearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val catalogRepository: CatalogRepository,
    private val recentSearchesRepository: RecentSearchesRepository,
    private val toggleWishlist: ToggleWishlistUseCase,
    private val quickAddToCart: QuickAddToCartUseCase,
    wishlistRepository: WishlistRepository,
) : ViewModel() {

    private val openFilters: Boolean = savedStateHandle[SEARCH_OPEN_FILTERS_ARG] ?: false

    private val catalog = MutableStateFlow<Catalog?>(null)
    private val catalogStatus = MutableStateFlow(SearchCatalog.Loading)
    private val input = MutableStateFlow(
        Input(submitted = openFilters, sheet = if (openFilters) SearchSheet.FILTERS else SearchSheet.NONE),
    )

    private val pendingWishlist = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val shownWishlist: Flow<Set<String>> =
        combine(wishlistRepository.observeIds().onStart { emit(emptySet()) }, pendingWishlist) { saved, pending ->
            saved + pending.filterValues { it }.keys - pending.filterValues { !it }.keys
        }

    private val recent = recentSearchesRepository.observe().onStart { emit(emptyList()) }

    val state: StateFlow<SearchState> =
        combine(catalog, catalogStatus, input, recent, shownWishlist) { catalog, status, input, recent, wishlist ->
            buildState(catalog, status, input, recent, wishlist)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, SearchState(submitted = openFilters))

    private val _events = Channel<SearchEvent>(Channel.BUFFERED)
    val events: Flow<SearchEvent> = _events.receiveAsFlow()

    init {
        load()
    }

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.QueryChanged -> input.update { it.copy(query = intent.query, submitted = false) }
            is SearchIntent.Submit -> submit(intent.query)
            SearchIntent.ClearQuery -> input.update { it.copy(query = "", submitted = false) }
            is SearchIntent.RemoveRecent -> saveRecent { list -> list.filterNot { it == intent.query } }
            SearchIntent.ClearRecent -> saveRecent { emptyList() }
            is SearchIntent.OpenSheet -> input.update { it.copy(sheet = intent.sheet) }
            SearchIntent.CloseSheet -> input.update { it.copy(sheet = SearchSheet.NONE) }
            is SearchIntent.ApplyFilters -> input.update { it.copy(filters = intent.filters, sheet = SearchSheet.NONE) }
            SearchIntent.ResetFilters -> input.update { it.copy(filters = ProductFilters()) }
            is SearchIntent.SelectSort -> input.update { it.copy(sort = intent.sort, sheet = SearchSheet.NONE) }
            is SearchIntent.ToggleQuickFilter -> input.update { it.copy(filters = it.filters.toggle(intent.filter)) }
            is SearchIntent.ToggleWishlist -> toggleProduct(intent.productId)
            is SearchIntent.QuickAdd -> quickAdd(intent.product)
            SearchIntent.Retry -> load()
        }
    }

    private fun load() {
        catalogStatus.value = SearchCatalog.Loading
        viewModelScope.launch {
            val products = async { catalogRepository.getAllProducts() }
            val stores = async { catalogRepository.getAllStores() }
            val categories = async { catalogRepository.getCategories() }
            val p = products.await()
            val s = stores.await()
            // Categories only feed the Filters sheet, so the screen works without them.
            val c = (categories.await() as? Result.Success)?.data.orEmpty()
            if (p is Result.Success && s is Result.Success) {
                catalog.value = Catalog(p.data, s.data, c)
                catalogStatus.value = SearchCatalog.Ready
            } else {
                catalogStatus.value = SearchCatalog.Error
            }
        }
    }

    private fun submit(query: String) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return
        input.update { it.copy(query = trimmed, submitted = true) }
        saveRecent { withRecent(it, trimmed) }
    }

    private fun saveRecent(change: (List<String>) -> List<String>) {
        viewModelScope.launch {
            recentSearchesRepository.save(change(state.value.recent))
        }
    }

    private fun buildState(
        catalog: Catalog?,
        status: SearchCatalog,
        input: Input,
        recent: List<String>,
        wishlist: Set<String>,
    ): SearchState {
        val products = catalog?.products.orEmpty()
        val stores = catalog?.stores.orEmpty()
        val storeNames = stores.associate { it.id to it.name }
        val freeDelivery = stores.filter { it.deliveryFeeCents == 0L }.map { it.id }.toSet()
        val typing = input.query.isNotBlank() && !input.submitted
        val matches = when {
            !input.submitted -> emptyList()
            input.query.isBlank() -> products
            else -> matchProducts(products, storeNames, input.query)
        }
        return SearchState(
            query = input.query,
            submitted = input.submitted,
            catalog = status,
            recent = recent,
            stores = stores.take(IDLE_STORES),
            categories = catalog?.categories.orEmpty(),
            suggestedStores = if (typing) matchStores(stores, input.query) else emptyList(),
            suggestions = if (typing) matchProducts(products, storeNames, input.query).take(MAX_SUGGESTIONS) else emptyList(),
            matches = matches,
            results = applyFilters(matches, input.filters, input.sort, freeDelivery),
            freeDeliveryStoreIds = freeDelivery,
            filters = input.filters,
            sort = input.sort,
            sheet = input.sheet,
            wishlist = wishlist,
        )
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
                    WishlistChange.SAVED -> SearchMessage.SAVED_TO_WISHLIST
                    WishlistChange.REMOVED -> SearchMessage.REMOVED_FROM_WISHLIST
                }
                is Result.Error -> SearchMessage.WISHLIST_FAILED
            }
            _events.send(SearchEvent.ShowMessage(message))
        }
    }

    private fun quickAdd(product: Product) {
        viewModelScope.launch {
            val message = when (quickAddToCart(product)) {
                is Result.Success -> SearchMessage.ADDED_TO_CART
                is Result.Error -> SearchMessage.CART_FAILED
            }
            _events.send(SearchEvent.ShowMessage(message))
        }
    }

    private data class Catalog(val products: List<Product>, val stores: List<Store>, val categories: List<Category>)

    private data class Input(
        val query: String = "",
        val submitted: Boolean = false,
        val filters: ProductFilters = ProductFilters(),
        val sort: ProductSort = ProductSort.RELEVANCE,
        val sheet: SearchSheet = SearchSheet.NONE,
    )
}

/** Flips a quick chip. "Under $50" is the $25–$50 range, as in the design. */
internal fun ProductFilters.toggle(filter: QuickFilter): ProductFilters = when (filter) {
    QuickFilter.UNDER_50 -> copy(price = if (price == PriceRange.FROM_25_TO_50) PriceRange.ANY else PriceRange.FROM_25_TO_50)
    QuickFilter.RATING_4_5 -> copy(minRating = if (minRating == 4.5) null else 4.5)
    QuickFilter.FREE_DELIVERY -> copy(freeDeliveryOnly = !freeDeliveryOnly)
}

/** Whether a quick chip shows selected. */
internal fun ProductFilters.isOn(filter: QuickFilter): Boolean = when (filter) {
    QuickFilter.UNDER_50 -> price == PriceRange.UNDER_25 || price == PriceRange.FROM_25_TO_50
    QuickFilter.RATING_4_5 -> minRating == 4.5
    QuickFilter.FREE_DELIVERY -> freeDeliveryOnly
}
