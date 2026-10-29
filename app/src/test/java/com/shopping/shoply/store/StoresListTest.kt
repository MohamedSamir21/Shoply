package com.shopping.shoply.store

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.store.domain.rules.StoreSort
import com.shopping.shoply.store.domain.rules.storeCategoriesOf
import com.shopping.shoply.store.domain.rules.storeList
import com.shopping.shoply.store.presentation.STORES_FAVORITES_ONLY_ARG
import com.shopping.shoply.store.presentation.StoresBody
import com.shopping.shoply.store.presentation.StoresEvent
import com.shopping.shoply.store.presentation.StoresIntent
import com.shopping.shoply.store.presentation.StoresViewModel
import com.shopping.shoply.testutil.MainDispatcherExtension
import com.shopping.shoply.testutil.aStore
import com.shopping.shoply.testutil.fakes.FakeCatalogRepository
import com.shopping.shoply.testutil.fakes.FakeFavoriteStoresRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@DisplayName("StoresListTest")
class StoresListTest {
    val volt = aStore(id = "volt", name = "Volt Electronics", category = "Electronics", rating = 4.8, etaMinutes = 25..35, distanceMiles = 1.2)
    val fern = aStore(id = "fern", name = "Fern & Co.", category = "Plants", rating = 4.9, etaMinutes = 30..45, distanceMiles = 0.8)
    val byte = aStore(id = "byte", name = "Byte Shop", category = "Electronics", rating = 4.2, etaMinutes = 15..25, distanceMiles = 3.0)
    val stores = listOf(volt, fern, byte)

    val fakeCatalogRepository = FakeCatalogRepository().apply { this.stores = this@StoresListTest.stores }
    val fakeFavoriteStoresRepository = FakeFavoriteStoresRepository()

    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    private fun createSut(favoritesOnly: Boolean = false) = StoresViewModel(
        savedStateHandle = SavedStateHandle(mapOf(STORES_FAVORITES_ONLY_ARG to favoritesOnly)),
        catalogRepository = fakeCatalogRepository,
        favoriteStoresRepository = fakeFavoriteStoresRepository,
    )

    @Test
    @DisplayName("storeList: category, query, and each sort")
    fun `storeList rules`() {
        assertThat(storeCategoriesOf(stores)).containsExactly("Electronics", "Plants").inOrder()
        assertThat(storeList(stores, "Electronics", "", StoreSort.NEAREST)).containsExactly(volt, byte).inOrder()
        assertThat(storeList(stores, null, "fern", StoreSort.NEAREST)).containsExactly(fern)
        assertThat(storeList(stores, null, "", StoreSort.TOP_RATED)).containsExactly(fern, volt, byte).inOrder()
        assertThat(storeList(stores, null, "", StoreSort.FASTEST)).containsExactly(byte, volt, fern).inOrder()
    }

    @Test
    @DisplayName("Favorite Stores lists only the favorites, live; none is Empty")
    fun `favorites only, live`() = runTest(main.dispatcher) {
        val sut = createSut(favoritesOnly = true)
        assertThat(sut.state.value.body).isEqualTo(StoresBody.Empty)

        fakeFavoriteStoresRepository.ids.value = setOf("fern")
        assertThat(sut.state.value.stores).containsExactly(fern)

        sut.onIntent(StoresIntent.ToggleFavorite("fern"))
        assertThat(sut.state.value.body).isEqualTo(StoresBody.Empty)
    }

    @Test
    @DisplayName("ToggleFavorite failing flips the heart back and sends FavoriteFailed")
    fun `favorite failure flips back`() = runTest(main.dispatcher) {
        fakeFavoriteStoresRepository.result = Result.Error(DataError.Unknown)
        val sut = createSut()

        sut.events.test {
            sut.onIntent(StoresIntent.ToggleFavorite("volt"))
            assertThat(awaitItem()).isEqualTo(StoresEvent.FavoriteFailed)
        }
        assertThat(sut.state.value.favorites).isEmpty()
    }

    @Test
    @DisplayName("a failed load is Error, and Retry loads again")
    fun `error and retry`() = runTest(main.dispatcher) {
        fakeCatalogRepository.storeError = DataError.Network
        val sut = createSut()
        assertThat(sut.state.value.body).isEqualTo(StoresBody.Error)

        fakeCatalogRepository.storeError = null
        sut.onIntent(StoresIntent.Retry)
        assertThat(sut.state.value.body).isEqualTo(StoresBody.Content)
    }
}
