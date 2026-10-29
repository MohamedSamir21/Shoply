package com.shopping.shoply.search.presentation

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.cart.domain.usecase.QuickAddToCartUseCase
import com.shopping.shoply.catalog.domain.rules.PriceRange
import com.shopping.shoply.catalog.domain.rules.ProductFilters
import com.shopping.shoply.catalog.domain.rules.ProductSort
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.testutil.MainDispatcherExtension
import com.shopping.shoply.testutil.aProduct
import com.shopping.shoply.testutil.aStore
import com.shopping.shoply.testutil.fakes.FakeCartRepository
import com.shopping.shoply.testutil.fakes.FakeCatalogRepository
import com.shopping.shoply.testutil.fakes.FakeRecentSearchesRepository
import com.shopping.shoply.testutil.fakes.FakeWishlistRepository
import com.shopping.shoply.wishlist.domain.usecase.ToggleWishlistUseCase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@DisplayName("SearchViewModelTest")
class SearchViewModelTest {
    val lamp = aProduct(id = "p1", name = "Desk Lamp", storeId = "nest", priceCents = 4900, rating = 4.5)
    val lampShade = aProduct(id = "p2", name = "Lamp Shade", storeId = "nest", priceCents = 1500, rating = 4.0)
    val mug = aProduct(id = "p3", name = "Mug", storeId = "nest", priceCents = 1800)

    val fakeCatalogRepository = FakeCatalogRepository().apply {
        products = listOf(lamp, lampShade, mug)
        stores = listOf(aStore(id = "nest", name = "Nest"))
    }
    val fakeRecentSearchesRepository = FakeRecentSearchesRepository()
    val fakeWishlistRepository = FakeWishlistRepository()
    val fakeCartRepository = FakeCartRepository()

    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    private fun createSut(openFilters: Boolean = false) = SearchViewModel(
        savedStateHandle = SavedStateHandle(mapOf(SEARCH_OPEN_FILTERS_ARG to openFilters)),
        catalogRepository = fakeCatalogRepository,
        recentSearchesRepository = fakeRecentSearchesRepository,
        toggleWishlist = ToggleWishlistUseCase(fakeWishlistRepository),
        quickAddToCart = QuickAddToCartUseCase(fakeCartRepository),
        wishlistRepository = fakeWishlistRepository,
    )

    @Test
    @DisplayName("typing shows suggestions; Submit shows results and saves the recent search")
    fun `typing suggests, Submit shows results and saves recent`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.onIntent(SearchIntent.QueryChanged("lamp"))
        assertThat(sut.state.value.suggestions).containsExactly(lamp, lampShade).inOrder()
        assertThat(sut.state.value.results).isEmpty()

        sut.onIntent(SearchIntent.Submit("lamp "))
        assertThat(sut.state.value.submitted).isTrue()
        assertThat(sut.state.value.results).containsExactly(lamp, lampShade).inOrder()
        assertThat(fakeRecentSearchesRepository.queries.value).containsExactly("lamp")
    }

    @Test
    @DisplayName("filters and sort apply to the results; editing the query hides them")
    fun `filters and sort apply to results`() = runTest(main.dispatcher) {
        val sut = createSut()
        sut.onIntent(SearchIntent.Submit("lamp"))

        sut.onIntent(SearchIntent.SelectSort(ProductSort.PRICE_LOW_HIGH))
        assertThat(sut.state.value.results).containsExactly(lampShade, lamp).inOrder()

        sut.onIntent(SearchIntent.ApplyFilters(ProductFilters(price = PriceRange.FROM_25_TO_50)))
        assertThat(sut.state.value.results).containsExactly(lamp)
        assertThat(sut.state.value.matches).hasSize(2)

        sut.onIntent(SearchIntent.QueryChanged("lam"))
        assertThat(sut.state.value.results).isEmpty()
    }

    @Test
    @DisplayName("openFilters browses every product with the Filters sheet open")
    fun `openFilters browses all with the sheet open`() = runTest(main.dispatcher) {
        val state = createSut(openFilters = true).state.value

        assertThat(state.sheet).isEqualTo(SearchSheet.FILTERS)
        assertThat(state.results).containsExactly(lamp, lampShade, mug).inOrder()
    }

    @Test
    @DisplayName("a failed catalog load is Error, and Retry loads it again")
    fun `failed load is Error and Retry reloads`() = runTest(main.dispatcher) {
        fakeCatalogRepository.productsError = DataError.Network
        val sut = createSut()
        assertThat(sut.state.value.catalog).isEqualTo(SearchCatalog.Error)

        fakeCatalogRepository.productsError = null
        sut.onIntent(SearchIntent.Retry)
        assertThat(sut.state.value.catalog).isEqualTo(SearchCatalog.Ready)
    }
}
