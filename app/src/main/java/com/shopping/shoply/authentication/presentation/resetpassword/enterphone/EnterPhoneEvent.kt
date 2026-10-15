package com.shopping.shoply.authentication.presentation.resetpassword.enterphone

sealed interface EnterPhoneEvent {
    // Carries the full E.164 number as digits (no '+') so the OTP screen can send the code.
    data class Success(
        val countryCode: String,
        val phoneNumber: String
    ) : EnterPhoneEvent
}
