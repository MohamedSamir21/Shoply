package com.shopping.shoply.orders.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.orders.domain.model.OrderStatus
import com.shopping.shoply.orders.domain.usecase.ReorderUseCase
import com.shopping.shoply.testutil.MainDispatcherExtension
import com.shopping.shoply.testutil.aProduct
import com.shopping.shoply.testutil.anOrder
import com.shopping.shoply.testutil.fakes.FakeCartRepository
import com.shopping.shoply.testutil.fakes.FakeCatalogRepository
import com.shopping.shoply.testutil.fakes.FakeOrdersRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@DisplayName("OrdersViewModelsTest")
class OrdersViewModelsTest {
    val fakeOrdersRepository = FakeOrdersRepository()
    val fakeCatalogRepository = FakeCatalogRepository().apply { products = listOf(aProduct(id = "p1")) }
    val fakeCartRepository = FakeCartRepository()
    val reorder = ReorderUseCase(fakeCatalogRepository, fakeCartRepository)

    val onTheWay = anOrder(id = "SH-3", status = OrderStatus.ON_THE_WAY, createdAtMillis = 3)
    val placed = anOrder(id = "SH-2", status = OrderStatus.PLACED, createdAtMillis = 2)
    val delivered = anOrder(id = "SH-1", status = OrderStatus.DELIVERED, createdAtMillis = 1)

    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    private fun ordersSut() = OrdersViewModel(fakeOrdersRepository, reorder)

    private fun detailsSut(id: String = "SH-1") =
        OrderDetailsViewModel(SavedStateHandle(mapOf(ORDER_DETAILS_ID_ARG to id)), fakeOrdersRepository, reorder)

    @Test
    @DisplayName("Orders: Loading until the first list, then the Active tab with both counts")
    fun `Orders shows Active with both counts`() = runTest(main.dispatcher) {
        val sut = ordersSut()
        assertThat(sut.state.value.loading).isTrue()

        fakeOrdersRepository.orders.value = listOf(onTheWay, placed, delivered)

        val state = sut.state.value
        assertThat(state.loading).isFalse()
        assertThat(state.orders).containsExactly(onTheWay, placed).inOrder()
        assertThat(state.activeCount).isEqualTo(2)
        assertThat(state.pastCount).isEqualTo(1)
    }

    @Test
    @DisplayName("Orders: SelectTab shows the past orders, and counts follow live changes")
    fun `Orders SelectTab shows past, counts follow live changes`() = runTest(main.dispatcher) {
        fakeOrdersRepository.orders.value = listOf(onTheWay, delivered)
        val sut = ordersSut()

        sut.onIntent(OrdersIntent.SelectTab(OrdersTab.PAST))
        assertThat(sut.state.value.orders).containsExactly(delivered)

        fakeOrdersRepository.orders.value = listOf(onTheWay.copy(status = OrderStatus.DELIVERED), delivered)
        assertThat(sut.state.value.pastCount).isEqualTo(2)
        assertThat(sut.state.value.activeCount).isEqualTo(0)
    }

    @Test
    @DisplayName("Orders: Reorder says REORDERED and opens the cart; a failure says REORDER_FAILED")
    fun `Orders Reorder success and failure`() = runTest(main.dispatcher) {
        fakeOrdersRepository.orders.value = listOf(delivered)
        val sut = ordersSut()

        sut.events.test {
            sut.onIntent(OrdersIntent.Reorder("SH-1"))
            assertThat(awaitItem()).isEqualTo(OrdersEvent.ShowMessage(OrdersMessage.REORDERED))
            assertThat(awaitItem()).isEqualTo(OrdersEvent.OpenCart)

            fakeCartRepository.result = Result.Error(DataError.Unknown)
            sut.onIntent(OrdersIntent.Reorder("SH-1"))
            assertThat(awaitItem()).isEqualTo(OrdersEvent.ShowMessage(OrdersMessage.REORDER_FAILED))
        }
    }

    @Test
    @DisplayName("Details: the order by its id, live; an unknown id is NotFound")
    fun `Details shows the order by id, unknown is NotFound`() = runTest(main.dispatcher) {
        val sut = detailsSut("SH-1")
        assertThat(sut.state.value.body).isEqualTo(OrderDetailsBody.Loading)

        fakeOrdersRepository.orders.value = listOf(delivered)
        assertThat(sut.state.value.body).isEqualTo(OrderDetailsBody.Content(delivered))

        assertThat(detailsSut("nope").state.value.body).isEqualTo(OrderDetailsBody.NotFound)
    }

    @Test
    @DisplayName("Details: Reorder is ignored while one is running")
    fun `Details Reorder is ignored while one is running`() = runTest(main.dispatcher) {
        fakeOrdersRepository.orders.value = listOf(delivered)
        val sut = detailsSut()
        fakeCartRepository.gate.hold()

        sut.onIntent(OrderDetailsIntent.Reorder)
        sut.onIntent(OrderDetailsIntent.Reorder)

        assertThat(sut.state.value.reordering).isTrue()
        assertThat(fakeCartRepository.addCalls).hasSize(1)
        fakeCartRepository.gate.release()
    }
}
