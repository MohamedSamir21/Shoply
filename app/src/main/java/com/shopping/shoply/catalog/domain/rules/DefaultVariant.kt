package com.shopping.shoply.catalog.domain.rules

import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.model.Variant

/**
 * The colour and size used when the user taps quick-add (+) without opening the product.
 *
 * - colour: the first colour, or null when the product has none.
 * - size: the second size when there are 2 or more (usually "M", the most common pick),
 *   the only size when there is 1, or null when there are none.
 */
fun defaultVariant(product: Product): Variant = Variant(
    color = product.colors.firstOrNull()?.name,
    size = product.sizes.getOrNull(1) ?: product.sizes.firstOrNull(),
)
