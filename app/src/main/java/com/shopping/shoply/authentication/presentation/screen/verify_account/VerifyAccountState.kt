package com.shopping.shoply.authentication.presentation.screen.verify_account

import com.shopping.shoply.authentication.presentation.utility.UiText

data class VerifyAccountState(
    val countDownTime: String = "01:00",
    val countDownFinished: Boolean = true,
    val sendEmailLinkSuccessMessage: UiText? = null,
    val sendEmailLinkError: UiText? = null
)
