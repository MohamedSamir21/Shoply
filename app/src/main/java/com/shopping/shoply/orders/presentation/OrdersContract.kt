package com.shopping.shoply.orders.presentation

import com.shopping.shoply.orders.domain.model.Order

// State, intents, events and destinations of the Orders tab and Order Details.

/** What the Orders tab shows. [orders] is the selected tab's list; the counts drive the tab badges. */
data class OrdersState(
    val tab: OrdersTab = OrdersTab.ACTIVE,
    val loading: Boolean = true,
    val orders: List<Order> = emptyList(),
    val activeCount: Int = 0,
    val pastCount: Int = 0,
)

enum class OrdersTab { ACTIVE, PAST }

sealed interface OrdersIntent {
    data class SelectTab(val tab: OrdersTab) : OrdersIntent

    data class Reorder(val orderId: String) : OrdersIntent
}

/** What Order Details shows. [reordering] disables Reorder while it runs. */
data class OrderDetailsState(
    val body: OrderDetailsBody = OrderDetailsBody.Loading,
    val reordering: Boolean = false,
)

sealed interface OrderDetailsBody {
    data object Loading : OrderDetailsBody

    data object NotFound : OrderDetailsBody

    data class Content(val order: Order) : OrderDetailsBody
}

sealed interface OrderDetailsIntent {
    data object Reorder : OrderDetailsIntent
}

/** Events of both screens. */
sealed interface OrdersEvent {
    /** Show [message] in a snackbar. */
    data class ShowMessage(val message: OrdersMessage) : OrdersEvent

    /** Reorder worked: go to the cart. */
    data object OpenCart : OrdersEvent
}

/** Snackbar messages. The screens map each to its text. */
enum class OrdersMessage {
    REORDERED,
    REORDER_FAILED,
}

/** Where the Orders tab and Order Details can send the user. */
sealed interface OrdersDestination {
    data class OrderDetails(val id: String) : OrdersDestination

    data class Product(val id: String) : OrdersDestination

    data object Cart : OrdersDestination

    data object StartShopping : OrdersDestination

    data object Back : OrdersDestination
}
