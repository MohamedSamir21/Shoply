package com.shopping.shoply.authentication.presentation.resetpassword.otpverification

const val OTP_CODE_LENGTH = 6

data class OTPVerificationState(
    val enteredCode: String = "",
    val countryCode: String = "",
    val phoneNumber: String = "",
    val countDownTime: String = "01:00",
    val countDownFinished: Boolean = false,
    val isLoading: Boolean = false
)
