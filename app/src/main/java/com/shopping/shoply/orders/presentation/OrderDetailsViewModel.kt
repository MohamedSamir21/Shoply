package com.shopping.shoply.orders.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.orders.domain.OrdersRepository
import com.shopping.shoply.orders.domain.usecase.ReorderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Nav argument holding the order's id. */
const val ORDER_DETAILS_ID_ARG = "orderId"

/**
 * Backs Order Details: the order from the nav id, live (NotFound when it doesn't exist).
 * Reorder puts its items back in the cart; a tap while one runs is ignored.
 *
 * [state] starts eagerly, so its value is always current, even before anything collects it.
 */
@HiltViewModel
class OrderDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    ordersRepository: OrdersRepository,
    private val reorder: ReorderUseCase,
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle[ORDER_DETAILS_ID_ARG])
    private val reordering = MutableStateFlow(false)

    private val body: StateFlow<OrderDetailsBody> = ordersRepository.observeOrder(orderId)
        .map { order -> if (order == null) OrderDetailsBody.NotFound else OrderDetailsBody.Content(order) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, OrderDetailsBody.Loading)

    val state: StateFlow<OrderDetailsState> = combine(body, reordering, ::OrderDetailsState)
        .stateIn(viewModelScope, SharingStarted.Eagerly, OrderDetailsState())

    private val _events = Channel<OrdersEvent>(Channel.BUFFERED)
    val events: Flow<OrdersEvent> = _events.receiveAsFlow()

    fun onIntent(intent: OrderDetailsIntent) {
        when (intent) {
            OrderDetailsIntent.Reorder -> startReorder()
        }
    }

    private fun startReorder() {
        val order = (body.value as? OrderDetailsBody.Content)?.order ?: return
        if (reordering.value) return
        reordering.value = true
        viewModelScope.launch {
            sendReorderResult(reorder(order), _events)
            reordering.value = false
        }
    }
}
