package com.shopping.shoply.checkout.domain

import com.shopping.shoply.checkout.domain.model.OrderRequest
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result

interface OrderRepository {

    /**
     * Saves the order and empties the cart in one step (local-first, works offline).
     *
     * @return the new order's id, or [DataError.Unknown] when nobody is signed in or the write fails.
     */
    suspend fun place(request: OrderRequest): Result<String, DataError>
}
