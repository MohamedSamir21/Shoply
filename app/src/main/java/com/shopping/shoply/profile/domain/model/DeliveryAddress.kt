package com.shopping.shoply.profile.domain.model

/** A saved delivery address. [label] is e.g. "Home"; [line1] is e.g. "24 Elm Street, Apt 5B". */
data class DeliveryAddress(val label: String, val line1: String)
