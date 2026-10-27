package com.shopping.shoply.checkout.domain.model

import com.shopping.shoply.cart.domain.model.CartItem
import com.shopping.shoply.cart.domain.rules.CartTotals
import com.shopping.shoply.profile.domain.model.DeliveryAddress

sealed interface DeliveryOption {
    /** A free 2-hour window starting at [slotStartHour] on the day starting at [dayStartMillis]. */
    data class Standard(val dayStartMillis: Long, val slotStartHour: Int) : DeliveryOption

    /** Within the hour, for [com.shopping.shoply.cart.domain.rules.EXPRESS_FEE_CENTS]. */
    data object Express : DeliveryOption
}

sealed interface PaymentChoice {
    data class Card(val card: PaymentCard) : PaymentChoice

    /** [months] is 3 or 6. */
    data class Installments(val card: PaymentCard, val months: Int) : PaymentChoice

    data object CashOnDelivery : PaymentChoice
}

/** Everything needed to place an order. [promoCode] is the applied code, if any. */
data class OrderRequest(
    val items: List<CartItem>,
    val address: DeliveryAddress,
    val delivery: DeliveryOption,
    val payment: PaymentChoice,
    val promoCode: String?,
    val totals: CartTotals,
)
