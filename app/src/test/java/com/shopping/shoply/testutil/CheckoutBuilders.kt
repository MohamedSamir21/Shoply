package com.shopping.shoply.testutil

import com.shopping.shoply.checkout.domain.model.CardBrand
import com.shopping.shoply.checkout.domain.model.PaymentCard
import com.shopping.shoply.profile.domain.model.DeliveryAddress

// Test data builders for checkout: every field has a valid default.
//   anAddress(id = "work", primary = false)

fun anAddress(
    id: String = "home",
    label: String = "Home",
    primary: Boolean = true,
) = DeliveryAddress(
    label = label,
    line1 = "24 Elm Street",
    id = id,
    recipientName = "Mohamed Samir",
    line2 = "Brooklyn, NY 11201",
    phone = "+1 555 010 4290",
    primary = primary,
)

fun aPaymentCard(
    id: String = "visa",
    brand: CardBrand = CardBrand.VISA,
    primary: Boolean = true,
) = PaymentCard(id, brand, last4 = "4821", expiry = "08/28", primary = primary)
