package com.shopping.shoply.store.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.cart.domain.usecase.QuickAddToCartUseCase
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.time.NowProvider
import com.shopping.shoply.store.domain.rules.OpeningStatus
import com.shopping.shoply.store.domain.rules.StoreTab
import com.shopping.shoply.testutil.MainDispatcherExtension
import com.shopping.shoply.testutil.aProduct
import com.shopping.shoply.testutil.aStore
import com.shopping.shoply.testutil.fakes.FakeCartRepository
import com.shopping.shoply.testutil.fakes.FakeCatalogRepository
import com.shopping.shoply.testutil.fakes.FakeFavoriteStoresRepository
import com.shopping.shoply.testutil.fakes.FakeWishlistRepository
import com.shopping.shoply.wishlist.domain.usecase.ToggleWishlistUseCase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.Calendar

@DisplayName("StoreViewModelTest")
class StoreViewModelTest {
    val store = aStore(id = "atelier", opensAtMinutes = 10 * 60, closesAtMinutes = 21 * 60)
    val plain = aProduct(id = "p1", storeId = "atelier")
    val discounted = aProduct(id = "p2", storeId = "atelier", priceCents = 900, oldPriceCents = 1200)
    val otherStore = aProduct(id = "p3", storeId = "volt")

    val fakeCatalogRepository = FakeCatalogRepository().apply {
        stores = listOf(store)
        products = listOf(plain, discounted, otherStore)
    }
    val fakeFavoriteStoresRepository = FakeFavoriteStoresRepository()
    val fakeWishlistRepository = FakeWishlistRepository()
    val fakeCartRepository = FakeCartRepository()

    // Today at 15:00.
    val now = NowProvider {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 15)
            set(Calendar.MINUTE, 0)
        }.timeInMillis
    }

    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    private fun createSut() = StoreViewModel(
        savedStateHandle = SavedStateHandle(mapOf(STORE_ID_ARG to "atelier")),
        catalogRepository = fakeCatalogRepository,
        favoriteStoresRepository = fakeFavoriteStoresRepository,
        toggleWishlist = ToggleWishlistUseCase(fakeWishlistRepository),
        quickAddToCart = QuickAddToCartUseCase(fakeCartRepository),
        wishlistRepository = fakeWishlistRepository,
        nowProvider = now,
    )

    @Test
    @DisplayName("loads the store with only its own products, and the opening status for now")
    fun `loads the store and its products`() = runTest(main.dispatcher) {
        val state = createSut().state.value

        assertThat(state.body).isEqualTo(StoreBody.Content(store, listOf(plain, discounted)))
        assertThat(state.opening).isEqualTo(OpeningStatus.OpenUntil(21 * 60))
    }

    @Test
    @DisplayName("SelectTab shows that tab's products")
    fun `SelectTab filters the products`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.onIntent(StoreIntent.SelectTab(StoreTab.OFFERS))

        assertThat(sut.state.value.tab).isEqualTo(StoreTab.OFFERS)
        assertThat(sut.state.value.body).isEqualTo(StoreBody.Content(store, listOf(discounted)))
    }

    @Test
    @DisplayName("either load failing is Error, and Retry loads both again")
    fun `a failed load is Error and Retry reloads`() = runTest(main.dispatcher) {
        fakeCatalogRepository.storeProductsError = DataError.Network
        val sut = createSut()
        assertThat(sut.state.value.body).isEqualTo(StoreBody.Error)

        fakeCatalogRepository.storeProductsError = null
        sut.onIntent(StoreIntent.Retry)

        assertThat(sut.state.value.body).isEqualTo(StoreBody.Content(store, listOf(plain, discounted)))
    }

    @Test
    @DisplayName("ToggleFavorite flips the heart at once and keeps it after the save")
    fun `ToggleFavorite flips at once`() = runTest(main.dispatcher) {
        val sut = createSut()
        fakeFavoriteStoresRepository.gate.hold()

        sut.onIntent(StoreIntent.ToggleFavorite)
        assertThat(sut.state.value.favorite).isTrue()

        fakeFavoriteStoresRepository.gate.release()
        assertThat(sut.state.value.favorite).isTrue()
        assertThat(fakeFavoriteStoresRepository.ids.value).containsExactly("atelier")
    }

    @Test
    @DisplayName("ToggleFavorite failing flips the heart back and says FAVORITE_FAILED")
    fun `ToggleFavorite failure flips back`() = runTest(main.dispatcher) {
        fakeFavoriteStoresRepository.result = Result.Error(DataError.Unknown)
        val sut = createSut()

        sut.events.test {
            sut.onIntent(StoreIntent.ToggleFavorite)
            assertThat(awaitItem()).isEqualTo(StoreEvent.ShowMessage(StoreMessage.FAVORITE_FAILED))
        }
        assertThat(sut.state.value.favorite).isFalse()
    }
}
