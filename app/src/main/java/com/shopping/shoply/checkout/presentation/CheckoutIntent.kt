package com.shopping.shoply.checkout.presentation

import com.shopping.shoply.checkout.domain.model.PaymentChoice

sealed interface CheckoutIntent {
    data class SelectAddress(val id: String) : CheckoutIntent

    data class SelectSpeed(val speed: DeliverySpeed) : CheckoutIntent

    /** Picks the day at [index] and its first free slot. */
    data class SelectDay(val index: Int) : CheckoutIntent

    /** Ignored for a full slot. */
    data class SelectSlot(val startHour: Int) : CheckoutIntent

    data class SelectPayment(val choice: PaymentChoice) : CheckoutIntent

    /** A tap on a done step in the stepper (or "Change" on Review); only goes back. */
    data class GoToStep(val step: Int) : CheckoutIntent

    data object Continue : CheckoutIntent

    /** Top-bar or system back: the previous step, or leave from the first one. */
    data object Back : CheckoutIntent

    data object PlaceOrder : CheckoutIntent
}
