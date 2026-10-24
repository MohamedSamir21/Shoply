package com.shopping.shoply.product.presentation

import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.model.Store

/**
 * What the Product Details screen shows. [color] and [size] are the picked variant (null when the
 * product has none); [addingToCart] disables Add to Cart while it runs.
 */
data class ProductDetailsState(
    val body: ProductDetailsBody = ProductDetailsBody.Loading,
    val color: String? = null,
    val size: String? = null,
    val quantity: Int = 1,
    val isSaved: Boolean = false,
    val addingToCart: Boolean = false,
)

sealed interface ProductDetailsBody {
    data object Loading : ProductDetailsBody

    /** The product couldn't be loaded. */
    data object Offline : ProductDetailsBody

    /** [store] is null and [similar] empty when they couldn't be loaded. */
    data class Content(
        val product: Product,
        val store: Store?,
        val similar: List<Product>,
    ) : ProductDetailsBody
}
