package com.shopping.shoply.authentication.presentation.verifyaccount

import com.shopping.shoply.authentication.presentation.utility.UiText

data class VerifyAccountState(
    val email: String? = null,
    val countDownTime: String = "01:00",
    val countDownFinished: Boolean = true,
    val sendEmailLinkSuccessMessage: UiText? = null,
    val sendEmailLinkError: UiText? = null,
    // Status re-check (Option A): set true once Firebase reports the email as verified,
    // which the screen observes to navigate forward.
    val isCheckingVerification: Boolean = false,
    val isEmailVerified: Boolean = false,
    val verificationError: UiText? = null
)
