package com.shopping.shoply.testutil.fakes

import com.shopping.shoply.cart.domain.CartRepository
import com.shopping.shoply.cart.domain.model.CartItem
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.model.Variant
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

/**
 * Set [totalQuantity] directly to drive the cart badge, and [items] to drive the cart (null = no
 * list yet). [gate] keeps an add pending (checks the ignored second tap). [result] answers add,
 * setQuantity and remove.
 */
class FakeCartRepository(
    var result: Result<Unit, DataError> = Result.Success(Unit),
) : CartRepository {
    val totalQuantity = MutableStateFlow(0)
    val items = MutableStateFlow<List<CartItem>?>(null)
    val gate = Gate()
    val addCalls = mutableListOf<Triple<Product, Variant, Int>>()
    val setQuantityCalls = mutableListOf<Pair<String, Int>>()
    val removeCalls = mutableListOf<String>()

    override fun observeTotalQuantity() = totalQuantity

    override suspend fun add(product: Product, variant: Variant, quantity: Int): Result<Unit, DataError> {
        addCalls += Triple(product, variant, quantity)
        gate.pass()
        return result
    }

    override fun observeItems(): Flow<List<CartItem>> = items.filterNotNull()

    override suspend fun setQuantity(itemId: String, quantity: Int): Result<Unit, DataError> {
        setQuantityCalls += itemId to quantity
        return result
    }

    override suspend fun remove(itemId: String): Result<Unit, DataError> {
        removeCalls += itemId
        return result
    }
}
