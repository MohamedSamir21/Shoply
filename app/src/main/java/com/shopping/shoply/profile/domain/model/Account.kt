package com.shopping.shoply.profile.domain.model

/** The signed-in user as the Profile tab shows them. [phone] is null when none was added. */
data class Account(
    val name: String,
    val email: String,
    val phone: String?,
)
