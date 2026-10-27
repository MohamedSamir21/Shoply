package com.shopping.shoply.checkout.domain.model

/** A saved card, for display only (no real card data). [expiry] is "MM/YY". */
data class PaymentCard(
    val id: String,
    val brand: CardBrand,
    val last4: String,
    val expiry: String,
    val primary: Boolean,
)

enum class CardBrand { VISA, MASTERCARD, OTHER }
