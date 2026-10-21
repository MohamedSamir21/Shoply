package com.shopping.shoply.cart.domain.usecase

import com.shopping.shoply.cart.domain.CartRepository
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.rules.defaultVariant
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import javax.inject.Inject

/**
 * Quick-add (+) from a product card: adds 1 of [product] in its [defaultVariant].
 *
 * @return success, or the repository's error unchanged.
 */
class QuickAddToCartUseCase @Inject constructor(
    private val cartRepository: CartRepository,
) {
    suspend operator fun invoke(product: Product): Result<Unit, DataError> =
        cartRepository.add(product, defaultVariant(product), quantity = 1)
}
