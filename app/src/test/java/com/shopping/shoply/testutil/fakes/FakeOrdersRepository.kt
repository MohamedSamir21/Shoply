package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.orders.domain.OrdersRepository
import com.shopping.shoply.orders.domain.model.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

/** Set [orders] directly to simulate live changes (null = nothing received yet). */
class FakeOrdersRepository : OrdersRepository {
    val orders = MutableStateFlow<List<Order>?>(null)

    override fun observeOrders(): Flow<List<Order>> = orders.filterNotNull()

    override fun observeOrder(id: String): Flow<Order?> = orders.filterNotNull().map { list -> list.firstOrNull { it.id == id } }
}
