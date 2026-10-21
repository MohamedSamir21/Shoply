package com.shopping.shoply.wishlist.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.testutil.fakes.FakeWishlistRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("ToggleWishlistUseCaseTest")
class ToggleWishlistUseCaseTest {

    @Test
    @DisplayName("reports SAVED when the user tells that the product isn't saved")
    fun `reports SAVED when the user tells that the product isn't saved`() = runTest {
        val repository = FakeWishlistRepository()
        val toggleWishlistUseCase = ToggleWishlistUseCase(repository)

        assertThat(
            toggleWishlistUseCase(productId = "product1", isSaved = false)
        ).isEqualTo(Result.Success<WishlistChange, DataError>(WishlistChange.SAVED))
        assertThat(
            repository.addCalls
        ).hasSize(1)
        assertThat(
            repository.removeCalls
        ).hasSize(0)
    }

    @Test
    @DisplayName("reports REMOVED when the user tells that the product is saved")
    fun `reports REMOVED when the user tells that the product is saved`() = runTest {
        val repository = FakeWishlistRepository()
        val toggleWishlistUseCase = ToggleWishlistUseCase(repository)

        assertThat(
            toggleWishlistUseCase(productId = "product1", isSaved = true)
        ).isEqualTo(Result.Success<WishlistChange, DataError>(WishlistChange.REMOVED))
        assertThat(
            repository.removeCalls
        ).hasSize(1)
        assertThat(
            repository.addCalls
        ).hasSize(0)
    }

    @Test
    @DisplayName("reports error when failure happens while toggling")
    fun `reports error when failure happens while toggling`() = runTest {
        val repository = FakeWishlistRepository(Result.Error<Unit, DataError>(DataError.Unknown))
        val toggleWishlistUseCase = ToggleWishlistUseCase(repository)

        assertThat(
            toggleWishlistUseCase(productId = "product1", isSaved = true)
        ).isEqualTo(Result.Error<WishlistChange, DataError>(DataError.Unknown))
        assertThat(
            repository.removeCalls
        ).hasSize(1)
    }
}