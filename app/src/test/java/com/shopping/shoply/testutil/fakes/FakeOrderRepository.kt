package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.checkout.domain.OrderRepository
import com.shopping.shoply.checkout.domain.model.OrderRequest
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result

/** Answers with [result]. [gate] keeps a place pending; [requests] records each call. */
class FakeOrderRepository(
    var result: Result<String, DataError> = Result.Success("order1"),
) : OrderRepository {
    val gate = Gate()
    val requests = mutableListOf<OrderRequest>()

    override suspend fun place(request: OrderRequest): Result<String, DataError> {
        requests += request
        gate.pass()
        return result
    }
}
