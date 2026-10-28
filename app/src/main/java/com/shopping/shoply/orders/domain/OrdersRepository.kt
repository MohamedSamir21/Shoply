package com.shopping.shoply.orders.domain

import com.shopping.shoply.orders.domain.model.Order
import kotlinx.coroutines.flow.Flow

/**
 * The signed-in user's orders. Flows emit right away, then on every change. They never complete
 * and never throw: when offline they keep the last known value.
 */
interface OrdersRepository {

    /** Every order, newest first. */
    fun observeOrders(): Flow<List<Order>>

    /** One order, or null when there's no order with [id]. */
    fun observeOrder(id: String): Flow<Order?>
}
