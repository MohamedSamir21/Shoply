package com.shopping.shoply.checkout.presentation

import com.shopping.shoply.cart.domain.model.CartItem
import com.shopping.shoply.cart.domain.rules.CartTotals
import com.shopping.shoply.cart.presentation.AppliedPromo
import com.shopping.shoply.checkout.domain.model.PaymentCard
import com.shopping.shoply.checkout.domain.model.PaymentChoice
import com.shopping.shoply.checkout.domain.rules.DeliveryDay
import com.shopping.shoply.profile.domain.model.DeliveryAddress

/** The 4 checkout steps, in order. */
const val STEP_ADDRESS = 0
const val STEP_DELIVERY = 1
const val STEP_PAYMENT = 2
const val STEP_REVIEW = 3

/**
 * What the Checkout screen shows. [loading] is true until the cart arrives. [slotHour] is the
 * picked Standard window on [days][[dayIndex]]. [placing] disables Place Order while it runs.
 */
data class CheckoutState(
    val step: Int = STEP_ADDRESS,
    val loading: Boolean = true,
    val items: List<CartItem> = emptyList(),
    val addresses: List<DeliveryAddress> = emptyList(),
    val selectedAddressId: String? = null,
    val speed: DeliverySpeed = DeliverySpeed.STANDARD,
    val days: List<DeliveryDay> = emptyList(),
    val dayIndex: Int = 0,
    val slotHour: Int? = null,
    val cards: List<PaymentCard> = emptyList(),
    val payment: PaymentChoice? = null,
    val totals: CartTotals = CartTotals(),
    val promo: AppliedPromo? = null,
    val placing: Boolean = false,
) {
    val selectedAddress: DeliveryAddress? get() = addresses.firstOrNull { it.id == selectedAddressId }

    /** Whether the current step has what it needs to move on. */
    val canContinue: Boolean
        get() = when (step) {
            STEP_ADDRESS -> selectedAddress != null
            STEP_DELIVERY -> speed == DeliverySpeed.EXPRESS || slotHour != null
            STEP_PAYMENT -> payment != null
            else -> false
        }
}

enum class DeliverySpeed { STANDARD, EXPRESS }
