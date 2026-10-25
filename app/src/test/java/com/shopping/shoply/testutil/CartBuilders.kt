package com.shopping.shoply.testutil

import com.shopping.shoply.cart.domain.model.CartItem
import com.shopping.shoply.catalog.domain.model.CatalogIcon

// Test data builder for cart items: every field has a valid default.
//   aCartItem(priceCents = 1000, quantity = 2)

fun aCartItem(
    id: String = "c1",
    productId: String = "p1",
    name: String = "Product $productId",
    icon: CatalogIcon = CatalogIcon.HEADPHONES,
    storeId: String = "store1",
    priceCents: Long = 1000,
    color: String? = "Sand",
    size: String? = "M",
    quantity: Int = 1,
) = CartItem(id, productId, name, icon, storeId, priceCents, color, size, quantity)
