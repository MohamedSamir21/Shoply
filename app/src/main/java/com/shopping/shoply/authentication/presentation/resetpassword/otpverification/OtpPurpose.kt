package com.shopping.shoply.authentication.presentation.resetpassword.otpverification

// Why the user is on the OTP screen: resetting a password (verify -> sign in) or finishing
// sign-up (verify -> link the phone onto the just-created email account).
enum class OtpPurpose {
    RESET,
    SIGNUP_LINK
}
