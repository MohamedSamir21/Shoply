package com.shopping.shoply.orders.domain.rules

import com.shopping.shoply.orders.domain.model.OrderStatus

/** Still on its way to the user (the Active tab). Delivered and cancelled orders are past. */
fun OrderStatus.isActive(): Boolean = this != OrderStatus.DELIVERED && this != OrderStatus.CANCELLED
