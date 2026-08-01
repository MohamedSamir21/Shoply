package com.shopping.shoply.authentication.presentation.resetpassword.sendresetlink

import com.shopping.shoply.authentication.presentation.utility.UiText

data class SendResetLinkState(
    val email: String? = null,
    val countDownTime: String = "01:00",
    val countDownFinished: Boolean = true,
    val sendEmailLinkSuccessMessage: UiText? = null,
    val sendEmailLinkError: UiText? = null
)