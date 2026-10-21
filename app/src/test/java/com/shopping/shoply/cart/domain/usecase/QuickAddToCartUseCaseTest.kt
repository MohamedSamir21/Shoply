package com.shopping.shoply.cart.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.catalog.domain.model.Variant
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.testutil.aProduct
import com.shopping.shoply.testutil.fakes.FakeCartRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("QuickAddToCartUseCaseTest")
class QuickAddToCartUseCaseTest {
    @Test
    @DisplayName("reports success when the product is added successfully")
    fun `reports success when the product is added successfully`() = runTest {
        val repository = FakeCartRepository()
        val quickAddToCartUseCase = QuickAddToCartUseCase(repository)

        assertThat(quickAddToCartUseCase(aProduct())).isEqualTo(
            Result.Success<Unit, DataError>(Unit)
        )
        assertThat(
            repository.addCalls
        ).containsExactly(
            Triple(aProduct(), Variant("Sand", "M"), 1)
        )
        assertThat(repository.addCalls).hasSize(1)
    }

    @Test
    @DisplayName("reports error when a failure occurs during adding the product")
    fun `reports error when a failure occurs during adding the product`() = runTest {
        val repository = FakeCartRepository(Result.Error<Unit, DataError>(DataError.Unknown))
        val quickAddToCartUseCase = QuickAddToCartUseCase(repository)

        assertThat(quickAddToCartUseCase(aProduct())).isEqualTo(
            Result.Error<Unit, DataError>(DataError.Unknown)
        )
        assertThat(repository.addCalls).hasSize(1)
    }
}