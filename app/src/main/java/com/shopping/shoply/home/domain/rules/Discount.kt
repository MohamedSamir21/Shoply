package com.shopping.shoply.home.domain.rules

import kotlin.math.roundToInt

fun discountPercent(priceCents: Long, oldPriceCents: Long?): Int?  {
    if (oldPriceCents == null || oldPriceCents <= priceCents) return null
    return ((oldPriceCents - priceCents) * 100f / oldPriceCents).roundToInt()
}