package com.shopping.shoply.orders.domain

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.catalog.domain.model.Variant
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.orders.domain.model.OrderStatus
import com.shopping.shoply.orders.domain.rules.isActive
import com.shopping.shoply.orders.domain.usecase.ReorderUseCase
import com.shopping.shoply.testutil.aProduct
import com.shopping.shoply.testutil.anOrder
import com.shopping.shoply.testutil.anOrderItem
import com.shopping.shoply.testutil.fakes.FakeCartRepository
import com.shopping.shoply.testutil.fakes.FakeCatalogRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("OrderRulesAndReorderTest")
class OrderRulesAndReorderTest {
    val fakeCatalogRepository = FakeCatalogRepository()
    val fakeCartRepository = FakeCartRepository()
    val sut = ReorderUseCase(fakeCatalogRepository, fakeCartRepository)

    @ParameterizedTest
    @DisplayName("placed, confirmed, packed and on the way are active; delivered and cancelled are past")
    @CsvSource(
        "PLACED, true",
        "CONFIRMED, true",
        "PACKED, true",
        "ON_THE_WAY, true",
        "DELIVERED, false",
        "CANCELLED, false",
    )
    fun `active statuses`(status: OrderStatus, active: Boolean) {
        assertThat(status.isActive()).isEqualTo(active)
    }

    @Test
    @DisplayName("Reorder adds each item with its variant and quantity, skipping products no longer sold")
    fun `Reorder adds each item, skipping products no longer sold`() = runTest {
        val shirt = aProduct(id = "p2")
        fakeCatalogRepository.products = listOf(shirt)
        val order = anOrder(
            items = listOf(
                anOrderItem(productId = "p2", color = "Navy", size = "L", quantity = 3),
                anOrderItem(productId = "gone"),
            ),
        )

        val result = sut(order)

        assertThat(result).isEqualTo(Result.Success<Int, DataError>(1))
        assertThat(fakeCartRepository.addCalls).containsExactly(
            Triple(
                shirt,
                Variant("Navy", "L"),
                3
            )
        )
    }

    @Test
    @DisplayName("Reorder fails when nothing could be added, or when the products can't be read")
    fun `Reorder fails when nothing could be added`() = runTest {
        val order = anOrder(items = listOf(anOrderItem(productId = "gone")))

        assertThat(sut(order)).isEqualTo(Result.Error<Int, DataError>(DataError.Unknown))

        fakeCatalogRepository.productsError = DataError.Network
        assertThat(sut(order)).isEqualTo(Result.Error<Int, DataError>(DataError.Network))
    }
}
