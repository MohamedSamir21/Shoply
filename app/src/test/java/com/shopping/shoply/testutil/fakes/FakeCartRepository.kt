package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.cart.domain.CartRepository
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.model.Variant
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import kotlinx.coroutines.flow.MutableStateFlow

/** Set [totalQuantity] directly to drive the cart badge. */
class FakeCartRepository(
    var result: Result<Unit, DataError> = Result.Success(Unit),
) : CartRepository {
    val totalQuantity = MutableStateFlow(0)
    val addCalls = mutableListOf<Triple<Product, Variant, Int>>()

    override fun observeTotalQuantity() = totalQuantity

    override suspend fun add(product: Product, variant: Variant, quantity: Int) =
        result.also { addCalls += Triple(product, variant, quantity) }
}
