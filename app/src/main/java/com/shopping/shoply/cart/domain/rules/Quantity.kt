package com.shopping.shoply.cart.domain.rules

/** The most of one item the quantity stepper allows. */
const val MAX_QUANTITY = 20

/** [quantity] kept within 1..[MAX_QUANTITY]. */
fun clampQuantity(quantity: Int): Int = quantity.coerceIn(1, MAX_QUANTITY)
