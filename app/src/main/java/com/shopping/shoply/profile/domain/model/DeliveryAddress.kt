package com.shopping.shoply.profile.domain.model

/**
 * A saved delivery address. [label] is e.g. "Home"; [line1] is e.g. "24 Elm Street, Apt 5B" and
 * [line2] e.g. "Brooklyn, NY 11201". [primary] marks the default one.
 */
data class DeliveryAddress(
    val label: String,
    val line1: String,
    val id: String = "",
    val recipientName: String = "",
    val line2: String = "",
    val phone: String = "",
    val primary: Boolean = false,
)
