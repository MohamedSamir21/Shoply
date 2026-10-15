package com.shopping.shoply.authentication.domain.model

// Domain entity = the user's profile/identity only. Password & confirmedPassword are
// transient credential/form concerns — they are NOT part of the entity and never persisted.
data class User(
    val username: String = "",
    val email: String? = null,
    val phoneNumber: String? = null,
    // True only once the phone was verified in an authenticated context (signup OTP link or,
    // later, from profile). Gates phone-based password recovery.
    val isPhoneVerified: Boolean = false
)
