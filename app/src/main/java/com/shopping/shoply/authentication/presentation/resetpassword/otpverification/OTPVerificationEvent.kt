package com.shopping.shoply.authentication.presentation.resetpassword.otpverification

import com.shopping.shoply.authentication.presentation.utility.UiText

sealed interface OTPVerificationEvent {
    data object Success : OTPVerificationEvent
    data class Error(val message: UiText) : OTPVerificationEvent
}
