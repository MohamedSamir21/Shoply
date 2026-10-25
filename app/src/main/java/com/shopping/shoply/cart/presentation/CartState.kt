package com.shopping.shoply.cart.presentation

import com.shopping.shoply.cart.domain.model.CartItem
import com.shopping.shoply.cart.domain.rules.CartTotals

/**
 * What the Cart screen shows. [promoInput] is the promo field's text; [promoInvalid] marks it red;
 * [applyingPromo] disables Apply while the code is checked.
 */
data class CartState(
    val body: CartBody = CartBody.Loading,
    val totals: CartTotals = CartTotals(),
    val promo: AppliedPromo? = null,
    val promoInput: String = "",
    val promoInvalid: Boolean = false,
    val applyingPromo: Boolean = false,
)

sealed interface CartBody {
    data object Loading : CartBody

    data object Empty : CartBody

    /** [items] oldest added first. */
    data class Content(val items: List<CartItem>) : CartBody
}

/** A promo code that was accepted, e.g. SHOPLY10 at 10% off. */
data class AppliedPromo(val code: String, val percentOff: Int)
