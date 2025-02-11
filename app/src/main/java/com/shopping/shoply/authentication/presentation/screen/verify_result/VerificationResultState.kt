package com.shopping.shoply.authentication.presentation.screen.verify_result

import com.shopping.shoply.authentication.presentation.utility.UiText

data class VerificationResultState(
    val isVerifying: Boolean = true,
    val verificationHasSucceeded: Boolean = false,
    val verificationError: UiText? = null
)
