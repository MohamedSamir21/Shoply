package com.shopping.shoply.authentication.presentation.resetpassword.otpverification

sealed interface OTPVerificationIntent {
    // The whole code as a single string (driven by one text field).
    data class ChangeCode(val value: String) : OTPVerificationIntent
    // Re-sends the code to the number this screen was opened with; the ViewModel owns it.
    data object Resend : OTPVerificationIntent
}
