package com.shopping.shoply.product.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.catalog.domain.model.ColorOption
import com.shopping.shoply.catalog.domain.model.Variant
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.testutil.MainDispatcherExtension
import com.shopping.shoply.testutil.aProduct
import com.shopping.shoply.testutil.aStore
import com.shopping.shoply.testutil.fakes.FakeCartRepository
import com.shopping.shoply.testutil.fakes.FakeCatalogRepository
import com.shopping.shoply.testutil.fakes.FakeWishlistRepository
import com.shopping.shoply.wishlist.domain.usecase.ToggleWishlistUseCase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@DisplayName("ProductDetailsViewModelTest")
class ProductDetailsViewModelTest {
    val fakeCatalogRepository = FakeCatalogRepository()
    val fakeCartRepository = FakeCartRepository()
    val fakeWishlistRepository = FakeWishlistRepository()

    // The opened product, with 2 colours and 3 sizes, in store "volt".
    val product = aProduct(
        id = "p1",
        storeId = "volt",
        colors = listOf(ColorOption("Sand", "#D8CAB6"), ColorOption("Charcoal", "#3A3735")),
        sizes = listOf("S", "M", "L"),
    )
    val store = aStore(id = "volt")

    // Same category: 5 others, so the rail is capped at 4.
    val others = List(5) { aProduct(id = "s$it") }

    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    private fun createSut(): ProductDetailsViewModel {
        fakeCatalogRepository.products = listOf(product) + others
        fakeCatalogRepository.stores = listOf(store)
        return ProductDetailsViewModel(
            savedStateHandle = SavedStateHandle(mapOf(PRODUCT_ID_ARG to "p1")),
            catalogRepository = fakeCatalogRepository,
            cartRepository = fakeCartRepository,
            toggleWishlist = ToggleWishlistUseCase(fakeWishlistRepository),
            wishlistRepository = fakeWishlistRepository,
        )
    }

    @Test
    @DisplayName("state is Loading while the product loads, then Content")
    fun `state is Loading while the product loads, then Content`() = runTest(main.dispatcher) {
        fakeCatalogRepository.gate.hold()
        val sut = createSut()

        assertThat(sut.state.value.body).isEqualTo(ProductDetailsBody.Loading)

        fakeCatalogRepository.gate.release()
        assertThat(sut.state.value.body)
            .isEqualTo(ProductDetailsBody.Content(product = product, store = store, similar = others.take(4)))
    }

    @Test
    @DisplayName("a failed load is Offline, and Retry loads again")
    fun `a failed load is Offline, and Retry loads again`() = runTest(main.dispatcher) {
        fakeCatalogRepository.productError = DataError.Network
        val sut = createSut()

        assertThat(sut.state.value.body).isEqualTo(ProductDetailsBody.Offline)

        fakeCatalogRepository.productError = null
        sut.onIntent(ProductDetailsIntent.Retry)

        assertThat(sut.state.value.body).isInstanceOf(ProductDetailsBody.Content::class.java)
    }

    @Test
    @DisplayName("the default colour and size are preselected with quantity 1")
    fun `the default colour and size are preselected with quantity 1`() = runTest(main.dispatcher) {
        val sut = createSut()

        with(sut.state.value) {
            assertThat(color).isEqualTo("Sand")
            assertThat(size).isEqualTo("M")
            assertThat(quantity).isEqualTo(1)
        }
    }

    @Test
    @DisplayName("a failed store or similar load still shows the product")
    fun `a failed store or similar load still shows the product`() = runTest(main.dispatcher) {
        fakeCatalogRepository.storeError = DataError.Network
        fakeCatalogRepository.categoryError = DataError.Network
        val sut = createSut()

        assertThat(sut.state.value.body)
            .isEqualTo(ProductDetailsBody.Content(product = product, store = null, similar = emptyList()))
    }

    @Test
    @DisplayName("similar products leave out the open one")
    fun `similar products leave out the open one`() = runTest(main.dispatcher) {
        val sut = createSut()

        val similar = (sut.state.value.body as ProductDetailsBody.Content).similar
        assertThat(similar.map { it.id }).doesNotContain("p1")
        assertThat(similar).hasSize(4)
    }

    @Test
    @DisplayName("quantity stays within 1..20")
    fun `quantity stays within 1 to 20`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.onIntent(ProductDetailsIntent.Decrease)
        assertThat(sut.state.value.quantity).isEqualTo(1)

        repeat(25) { sut.onIntent(ProductDetailsIntent.Increase) }
        assertThat(sut.state.value.quantity).isEqualTo(20)
    }

    @Test
    @DisplayName("AddToCart adds the picked variant and quantity and shows ADDED_TO_CART")
    fun `AddToCart adds the picked variant and quantity and shows ADDED_TO_CART`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.events.test {
            sut.onIntent(ProductDetailsIntent.SelectColor("Charcoal"))
            sut.onIntent(ProductDetailsIntent.SelectSize("L"))
            sut.onIntent(ProductDetailsIntent.Increase)
            sut.onIntent(ProductDetailsIntent.AddToCart)

            assertThat(awaitItem()).isEqualTo(ProductDetailsEvent.ShowMessage(ProductDetailsMessage.ADDED_TO_CART))
            assertThat(fakeCartRepository.addCalls).containsExactly(Triple(product, Variant("Charcoal", "L"), 2))
        }
    }

    @Test
    @DisplayName("a failed AddToCart shows CART_FAILED")
    fun `a failed AddToCart shows CART_FAILED`() = runTest(main.dispatcher) {
        fakeCartRepository.result = Result.Error(DataError.Network)
        val sut = createSut()

        sut.events.test {
            sut.onIntent(ProductDetailsIntent.AddToCart)

            assertThat(awaitItem()).isEqualTo(ProductDetailsEvent.ShowMessage(ProductDetailsMessage.CART_FAILED))
            assertThat(sut.state.value.addingToCart).isFalse()
        }
    }

    @Test
    @DisplayName("a second AddToCart while adding is ignored")
    fun `a second AddToCart while adding is ignored`() = runTest(main.dispatcher) {
        val sut = createSut()
        fakeCartRepository.gate.hold()

        sut.onIntent(ProductDetailsIntent.AddToCart)
        assertThat(sut.state.value.addingToCart).isTrue()
        sut.onIntent(ProductDetailsIntent.AddToCart)

        fakeCartRepository.gate.release()
        assertThat(fakeCartRepository.addCalls).hasSize(1)
        assertThat(sut.state.value.addingToCart).isFalse()
    }

    @Test
    @DisplayName("ToggleWishlist saves the product and shows SAVED")
    fun `ToggleWishlist saves the product and shows SAVED`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.events.test {
            fakeWishlistRepository.gate.hold()
            sut.onIntent(ProductDetailsIntent.ToggleWishlist)
            assertThat(sut.state.value.isSaved).isTrue()

            fakeWishlistRepository.gate.release()
            assertThat(awaitItem()).isEqualTo(ProductDetailsEvent.ShowMessage(ProductDetailsMessage.SAVED))
            assertThat(sut.state.value.isSaved).isTrue()
            assertThat(fakeWishlistRepository.addCalls).containsExactly("p1")
        }
    }

    @Test
    @DisplayName("a failed ToggleWishlist flips the heart back and shows WISHLIST_FAILED")
    fun `a failed ToggleWishlist flips the heart back and shows WISHLIST_FAILED`() = runTest(main.dispatcher) {
        fakeWishlistRepository.ids.value = setOf("p1")
        fakeWishlistRepository.result = Result.Error(DataError.Network)
        val sut = createSut()

        sut.events.test {
            sut.onIntent(ProductDetailsIntent.ToggleWishlist)

            assertThat(awaitItem()).isEqualTo(ProductDetailsEvent.ShowMessage(ProductDetailsMessage.WISHLIST_FAILED))
            assertThat(sut.state.value.isSaved).isTrue()
        }
    }
}
