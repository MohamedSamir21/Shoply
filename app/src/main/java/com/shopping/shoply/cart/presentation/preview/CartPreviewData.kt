package com.shopping.shoply.cart.presentation.preview

import com.shopping.shoply.cart.domain.model.CartItem
import com.shopping.shoply.cart.domain.rules.cartTotals
import com.shopping.shoply.cart.presentation.AppliedPromo
import com.shopping.shoply.cart.presentation.CartBody
import com.shopping.shoply.cart.presentation.CartState
import com.shopping.shoply.catalog.domain.model.CatalogIcon

/** Sample cart data for previews. */
internal object CartPreviewData {
    val headphones = CartItem(
        id = "p1_Sand",
        productId = "p1",
        name = "Wireless Over-Ear Headphones",
        icon = CatalogIcon.HEADPHONES,
        storeId = "volt",
        priceCents = 8900,
        color = "Sand",
        size = null,
        quantity = 2,
    )
    val shirt = CartItem(
        id = "p2_Oat_M",
        productId = "p2",
        name = "Linen Button-Down Shirt",
        icon = CatalogIcon.SHIRT,
        storeId = "thread",
        priceCents = 4900,
        color = "Oat",
        size = "M",
        quantity = 1,
    )
    val mug = CartItem(
        id = "p3",
        productId = "p3",
        name = "Stoneware Coffee Mug",
        icon = CatalogIcon.MUG,
        storeId = "nook",
        priceCents = 1600,
        color = null,
        size = null,
        quantity = 1,
    )

    val items = listOf(headphones, shirt, mug)
    val promo = AppliedPromo("SHOPLY10", 10)

    val content = CartState(
        body = CartBody.Content(items),
        totals = cartTotals(items, emptyMap(), promo.percentOff),
        promo = promo,
    )
}
