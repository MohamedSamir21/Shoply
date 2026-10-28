package com.shopping.shoply.orders.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.orders.domain.OrdersRepository
import com.shopping.shoply.orders.domain.model.Order
import com.shopping.shoply.orders.domain.rules.isActive
import com.shopping.shoply.orders.domain.usecase.ReorderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the Orders tab: the live orders split into Active and Past (newest first).
 * Reorder puts an order's items back in the cart; a tap while one runs is ignored.
 *
 * [state] starts eagerly, so its value is always current, even before anything collects it.
 */
@HiltViewModel
class OrdersViewModel @Inject constructor(
    ordersRepository: OrdersRepository,
    private val reorder: ReorderUseCase,
) : ViewModel() {

    private val tab = MutableStateFlow(OrdersTab.ACTIVE)
    private var reordering = false

    // null until the first list arrives.
    private val orders: StateFlow<List<Order>?> = ordersRepository.observeOrders()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val state: StateFlow<OrdersState> = combine(orders, tab) { orders, tab ->
        val (active, past) = orders.orEmpty().partition { it.status.isActive() }
        OrdersState(
            tab = tab,
            loading = orders == null,
            orders = if (tab == OrdersTab.ACTIVE) active else past,
            activeCount = active.size,
            pastCount = past.size,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, OrdersState())

    private val _events = Channel<OrdersEvent>(Channel.BUFFERED)
    val events: Flow<OrdersEvent> = _events.receiveAsFlow()

    fun onIntent(intent: OrdersIntent) {
        when (intent) {
            is OrdersIntent.SelectTab -> tab.value = intent.tab
            is OrdersIntent.Reorder -> startReorder(intent.orderId)
        }
    }

    private fun startReorder(orderId: String) {
        val order = orders.value?.firstOrNull { it.id == orderId } ?: return
        if (reordering) return
        reordering = true
        viewModelScope.launch {
            sendReorderResult(reorder(order), _events)
            reordering = false
        }
    }
}

/** Reorder's outcome as events: a message, then the cart on success. Shared by both screens. */
internal suspend fun sendReorderResult(result: Result<Int, *>, events: Channel<OrdersEvent>) {
    when (result) {
        is Result.Success -> {
            events.send(OrdersEvent.ShowMessage(OrdersMessage.REORDERED))
            events.send(OrdersEvent.OpenCart)
        }
        is Result.Error -> events.send(OrdersEvent.ShowMessage(OrdersMessage.REORDER_FAILED))
    }
}
