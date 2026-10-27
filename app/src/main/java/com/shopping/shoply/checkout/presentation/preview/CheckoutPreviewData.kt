package com.shopping.shoply.checkout.presentation.preview

import com.shopping.shoply.cart.domain.rules.cartTotals
import com.shopping.shoply.cart.presentation.preview.CartPreviewData
import com.shopping.shoply.checkout.domain.model.CardBrand
import com.shopping.shoply.checkout.domain.model.PaymentCard
import com.shopping.shoply.checkout.domain.model.PaymentChoice
import com.shopping.shoply.checkout.domain.rules.deliveryDays
import com.shopping.shoply.checkout.presentation.CheckoutState
import com.shopping.shoply.checkout.presentation.STEP_PAYMENT
import com.shopping.shoply.profile.domain.model.DeliveryAddress
import java.util.Calendar

/** Sample checkout data for previews: the test user's addresses and cards, at 11:30 on Oct 27. */
internal object CheckoutPreviewData {
    val home = DeliveryAddress(
        label = "Home",
        line1 = "24 Elm Street, Apt 5B",
        id = "home",
        recipientName = "Mohamed Samir",
        line2 = "Brooklyn, NY 11201",
        phone = "+1 555 010 4290",
        primary = true,
    )
    val work = home.copy(id = "work", label = "Work", line1 = "300 Park Avenue, Floor 12", line2 = "New York, NY 10022", primary = false)
    val visa = PaymentCard("visa", CardBrand.VISA, "4821", "08/28", primary = true)
    val mastercard = PaymentCard("mc", CardBrand.MASTERCARD, "0937", "11/26", primary = false)

    private val now = Calendar.getInstance().apply {
        clear()
        set(2026, Calendar.OCTOBER, 27, 11, 30)
    }.timeInMillis

    val state = CheckoutState(
        loading = false,
        items = CartPreviewData.items,
        addresses = listOf(home, work),
        selectedAddressId = "home",
        days = deliveryDays(now),
        slotHour = 14,
        cards = listOf(visa, mastercard),
        payment = PaymentChoice.Card(visa),
        totals = cartTotals(CartPreviewData.items, emptyMap(), CartPreviewData.promo.percentOff),
        promo = CartPreviewData.promo,
    )

    val installments = state.copy(step = STEP_PAYMENT, payment = PaymentChoice.Installments(visa, months = 3))
}
