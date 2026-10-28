package com.shopping.shoply.orders.domain.usecase

import com.shopping.shoply.cart.domain.CartRepository
import com.shopping.shoply.catalog.domain.CatalogRepository
import com.shopping.shoply.catalog.domain.model.Variant
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.orders.domain.model.Order
import javax.inject.Inject

/**
 * Puts [Order.items] back in the cart, each with its colour, size and quantity, at today's price
 * (the current product is added, not the old copy). Products no longer sold are skipped.
 *
 * @return how many lines were added; [DataError.Unknown] when none could be, or the read's error.
 */
class ReorderUseCase @Inject constructor(
    private val catalogRepository: CatalogRepository,
    private val cartRepository: CartRepository,
) {
    suspend operator fun invoke(order: Order): Result<Int, DataError> {
        val products = when (val result = catalogRepository.getProducts(order.items.map { it.productId }.toSet())) {
            is Result.Success -> result.data.associateBy { it.id }
            is Result.Error -> return Result.Error(result.error)
        }
        val added = order.items.count { item ->
            val product = products[item.productId] ?: return@count false
            cartRepository.add(product, Variant(item.color, item.size), item.quantity) is Result.Success
        }
        return if (added > 0) Result.Success(added) else Result.Error(DataError.Unknown)
    }
}
