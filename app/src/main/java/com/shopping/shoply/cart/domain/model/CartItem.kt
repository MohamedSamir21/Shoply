package com.shopping.shoply.cart.domain.model

import com.shopping.shoply.catalog.domain.model.CatalogIcon

/**
 * One product variant in the cart. Name, icon and price are copies taken when it was added, so the
 * cart shows without reading the catalog.
 *
 * @property id the cart item's own id (product + variant), not the product id.
 * @property color null when the product has no colours; same for [size].
 */
data class CartItem(
    val id: String,
    val productId: String,
    val name: String,
    val icon: CatalogIcon,
    val storeId: String,
    val priceCents: Long,
    val color: String?,
    val size: String?,
    val quantity: Int,
)
