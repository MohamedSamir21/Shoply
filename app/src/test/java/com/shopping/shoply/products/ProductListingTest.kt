package com.shopping.shoply.products

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.cart.domain.usecase.QuickAddToCartUseCase
import com.shopping.shoply.catalog.domain.rules.ProductFilters
import com.shopping.shoply.catalog.domain.rules.ProductSort
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.products.domain.rules.inSubcategory
import com.shopping.shoply.products.domain.rules.subcategoriesOf
import com.shopping.shoply.products.presentation.LISTING_CATEGORY_ID_ARG
import com.shopping.shoply.products.presentation.ListingBody
import com.shopping.shoply.products.presentation.ProductListingIntent
import com.shopping.shoply.products.presentation.ProductListingViewModel
import com.shopping.shoply.testutil.MainDispatcherExtension
import com.shopping.shoply.testutil.aCategory
import com.shopping.shoply.testutil.aProduct
import com.shopping.shoply.testutil.fakes.FakeCartRepository
import com.shopping.shoply.testutil.fakes.FakeCatalogRepository
import com.shopping.shoply.testutil.fakes.FakeWishlistRepository
import com.shopping.shoply.wishlist.domain.usecase.ToggleWishlistUseCase
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@DisplayName("ProductListingTest")
class ProductListingTest {
    val headphones = aProduct(id = "p1", subcategory = "Audio", priceCents = 8900, rating = 4.6)
    val speaker = aProduct(id = "p2", subcategory = "Audio", priceCents = 4500, rating = 4.8)
    val watch = aProduct(id = "p3", subcategory = "Wearables", priceCents = 12000)
    val shirt = aProduct(id = "p4", categoryId = "fashion")

    val fakeCatalogRepository = FakeCatalogRepository(result = Result.Success(listOf(aCategory(id = "electronics")))).apply {
        products = listOf(headphones, speaker, watch, shirt)
    }
    val fakeWishlistRepository = FakeWishlistRepository()

    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    private fun createSut(categoryId: String = "electronics") = ProductListingViewModel(
        savedStateHandle = SavedStateHandle(mapOf(LISTING_CATEGORY_ID_ARG to categoryId)),
        catalogRepository = fakeCatalogRepository,
        toggleWishlist = ToggleWishlistUseCase(fakeWishlistRepository),
        quickAddToCart = QuickAddToCartUseCase(FakeCartRepository()),
        wishlistRepository = fakeWishlistRepository,
    )

    @Test
    @DisplayName("subcategoriesOf: distinct, first-seen order, blanks dropped; inSubcategory(null) keeps all")
    fun `subcategory rules`() {
        val untagged = aProduct(id = "p9")
        assertThat(subcategoriesOf(listOf(headphones, watch, speaker, untagged))).containsExactly("Audio", "Wearables").inOrder()
        assertThat(inSubcategory(listOf(headphones, watch), null)).containsExactly(headphones, watch)
        assertThat(inSubcategory(listOf(headphones, watch), "Wearables")).containsExactly(watch)
    }

    @Test
    @DisplayName("loads only the category's products with its chips and title")
    fun `loads the category`() = runTest(main.dispatcher) {
        val state = createSut().state.value

        assertThat(state.body).isEqualTo(ListingBody.Content)
        assertThat(state.category?.id).isEqualTo("electronics")
        assertThat(state.subcategories).containsExactly("Audio", "Wearables").inOrder()
        assertThat(state.results).containsExactly(headphones, speaker, watch).inOrder()
    }

    @Test
    @DisplayName("subcategory, then filters, then sort")
    fun `subcategory filters sort`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.onIntent(ProductListingIntent.SelectSubcategory("Audio"))
        sut.onIntent(ProductListingIntent.SelectSort(ProductSort.PRICE_LOW_HIGH))
        assertThat(sut.state.value.results).containsExactly(speaker, headphones).inOrder()

        sut.onIntent(ProductListingIntent.ApplyFilters(ProductFilters(minRating = 4.7)))
        assertThat(sut.state.value.results).containsExactly(speaker)
        assertThat(sut.state.value.matches).hasSize(2)
    }

    @Test
    @DisplayName("no products is Empty; a failure is Error and Retry reloads")
    fun `empty and error`() = runTest(main.dispatcher) {
        assertThat(createSut(categoryId = "books").state.value.body).isEqualTo(ListingBody.Empty)

        fakeCatalogRepository.categoryError = DataError.Network
        val sut = createSut()
        assertThat(sut.state.value.body).isEqualTo(ListingBody.Error)

        fakeCatalogRepository.categoryError = null
        sut.onIntent(ProductListingIntent.Retry)
        assertThat(sut.state.value.body).isEqualTo(ListingBody.Content)
    }
}
