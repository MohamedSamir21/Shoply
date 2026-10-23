package com.shopping.shoply.wishlist.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.cart.domain.usecase.QuickAddToCartUseCase
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.testutil.MainDispatcherExtension
import com.shopping.shoply.testutil.products
import com.shopping.shoply.testutil.fakes.FakeCartRepository
import com.shopping.shoply.testutil.fakes.FakeCatalogRepository
import com.shopping.shoply.testutil.fakes.FakeWishlistRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@DisplayName("WishlistViewModelTest")
class WishlistViewModelTest {
    val fakeWishlistRepository = FakeWishlistRepository()
    val fakeCatalogRepository = FakeCatalogRepository()
    val fakeCartRepository = FakeCartRepository()

    // p0, p1, p2 in the catalog.
    val catalog = products(3)

    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    private fun createSut(): WishlistViewModel {
        fakeCatalogRepository.products = catalog
        return WishlistViewModel(
            wishlistRepository = fakeWishlistRepository,
            catalogRepository = fakeCatalogRepository,
            quickAddToCart = QuickAddToCartUseCase(fakeCartRepository),
        )
    }

    @Test
    @DisplayName("state is Loading while the products load")
    fun `state is Loading while the products load`() = runTest(main.dispatcher) {
        fakeWishlistRepository.ids.value = setOf("p0")
        fakeCatalogRepository.gate.hold()
        val sut = createSut()

        assertThat(sut.state.value.body).isEqualTo(WishlistBody.Loading)

        fakeCatalogRepository.gate.release()
        assertThat(sut.state.value.body).isEqualTo(WishlistBody.Content(listOf(catalog[0])))
    }

    @Test
    @DisplayName("state is Empty when nothing is saved")
    fun `state is Empty when nothing is saved`() = runTest(main.dispatcher) {
        val sut = createSut()

        assertThat(sut.state.value.body).isEqualTo(WishlistBody.Empty)
        assertThat(fakeCatalogRepository.getProductsCalls).isEmpty()
    }

    @Test
    @DisplayName("state is Empty when the only saved product no longer exists")
    fun `state is Empty when the only saved product no longer exists`() = runTest(main.dispatcher) {
        fakeWishlistRepository.ids.value = setOf("deleted")
        val sut = createSut()

        assertThat(sut.state.value.body).isEqualTo(WishlistBody.Empty)
    }

    @Test
    @DisplayName("state shows the saved products in the order of the saved ids")
    fun `state shows the saved products in the order of the saved ids`() = runTest(main.dispatcher) {
        fakeWishlistRepository.ids.value = linkedSetOf("p2", "p0")
        val sut = createSut()

        assertThat(sut.state.value.body).isEqualTo(WishlistBody.Content(listOf(catalog[2], catalog[0])))
    }

    @Test
    @DisplayName("a product saved elsewhere appears and only it is fetched")
    fun `a product saved elsewhere appears and only it is fetched`() = runTest(main.dispatcher) {
        fakeWishlistRepository.ids.value = linkedSetOf("p0", "p1")
        val sut = createSut()

        fakeWishlistRepository.ids.value = linkedSetOf("p0", "p1", "p2")

        assertThat(sut.state.value.body).isEqualTo(WishlistBody.Content(catalog))
        assertThat(fakeCatalogRepository.getProductsCalls).containsExactly(setOf("p0", "p1"), setOf("p2")).inOrder()
    }

    @Test
    @DisplayName("a product un-saved elsewhere disappears without a refetch")
    fun `a product un-saved elsewhere disappears without a refetch`() = runTest(main.dispatcher) {
        fakeWishlistRepository.ids.value = linkedSetOf("p0", "p1")
        val sut = createSut()

        fakeWishlistRepository.ids.value = setOf("p1")

        assertThat(sut.state.value.body).isEqualTo(WishlistBody.Content(listOf(catalog[1])))
        assertThat(fakeCatalogRepository.getProductsCalls).hasSize(1)
    }

    @Test
    @DisplayName("a failed load with nothing on screen is Offline, and Retry loads again")
    fun `a failed load with nothing on screen is Offline, and Retry loads again`() = runTest(main.dispatcher) {
        fakeWishlistRepository.ids.value = setOf("p0")
        fakeCatalogRepository.productsError = DataError.Network
        val sut = createSut()

        assertThat(sut.state.value.body).isEqualTo(WishlistBody.Offline)

        fakeCatalogRepository.productsError = null
        fakeCatalogRepository.gate.hold()
        sut.onIntent(WishlistIntent.Retry)
        assertThat(sut.state.value.body).isEqualTo(WishlistBody.Loading)

        fakeCatalogRepository.gate.release()
        assertThat(sut.state.value.body).isEqualTo(WishlistBody.Content(listOf(catalog[0])))
        assertThat(fakeCatalogRepository.getProductsCalls).hasSize(2)
    }

    @Test
    @DisplayName("Remove hides the item at once and shows REMOVED")
    fun `Remove hides the item at once and shows REMOVED`() = runTest(main.dispatcher) {
        fakeWishlistRepository.ids.value = linkedSetOf("p0", "p1")
        val sut = createSut()

        sut.events.test {
            fakeWishlistRepository.gate.hold()
            sut.onIntent(WishlistIntent.Remove("p0"))
            assertThat(sut.state.value.body).isEqualTo(WishlistBody.Content(listOf(catalog[1])))

            fakeWishlistRepository.gate.release()
            assertThat(awaitItem()).isEqualTo(WishlistEvent.ShowMessage(WishlistMessage.REMOVED))
            assertThat(sut.state.value.body).isEqualTo(WishlistBody.Content(listOf(catalog[1])))
            assertThat(fakeWishlistRepository.removeCalls).containsExactly("p0")
        }
    }

    @Test
    @DisplayName("a failed Remove brings the item back and shows REMOVE_FAILED")
    fun `a failed Remove brings the item back and shows REMOVE_FAILED`() = runTest(main.dispatcher) {
        fakeWishlistRepository.ids.value = linkedSetOf("p0", "p1")
        fakeWishlistRepository.result = Result.Error(DataError.Network)
        val sut = createSut()

        sut.events.test {
            sut.onIntent(WishlistIntent.Remove("p0"))

            assertThat(awaitItem()).isEqualTo(WishlistEvent.ShowMessage(WishlistMessage.REMOVE_FAILED))
            assertThat(sut.state.value.body).isEqualTo(WishlistBody.Content(listOf(catalog[0], catalog[1])))
        }
    }

    @Test
    @DisplayName("MoveToCart adds to the cart, un-saves it and shows MOVED_TO_CART")
    fun `MoveToCart adds to the cart, un-saves it and shows MOVED_TO_CART`() = runTest(main.dispatcher) {
        fakeWishlistRepository.ids.value = linkedSetOf("p0", "p1")
        val sut = createSut()

        sut.events.test {
            sut.onIntent(WishlistIntent.MoveToCart(catalog[0]))

            assertThat(awaitItem()).isEqualTo(WishlistEvent.ShowMessage(WishlistMessage.MOVED_TO_CART))
            assertThat(fakeCartRepository.addCalls.map { it.first }).containsExactly(catalog[0])
            assertThat(fakeWishlistRepository.removeCalls).containsExactly("p0")
            assertThat(sut.state.value.body).isEqualTo(WishlistBody.Content(listOf(catalog[1])))
        }
    }

    @Test
    @DisplayName("a failed MoveToCart keeps the item saved and shows CART_FAILED")
    fun `a failed MoveToCart keeps the item saved and shows CART_FAILED`() = runTest(main.dispatcher) {
        fakeWishlistRepository.ids.value = linkedSetOf("p0", "p1")
        fakeCartRepository.result = Result.Error(DataError.Network)
        val sut = createSut()

        sut.events.test {
            sut.onIntent(WishlistIntent.MoveToCart(catalog[0]))

            assertThat(awaitItem()).isEqualTo(WishlistEvent.ShowMessage(WishlistMessage.CART_FAILED))
            assertThat(fakeWishlistRepository.removeCalls).isEmpty()
            assertThat(sut.state.value.body).isEqualTo(WishlistBody.Content(listOf(catalog[0], catalog[1])))
        }
    }
}
