package com.shopping.shoply.authentication.presentation.reset_password.send_rest_link

import com.shopping.shoply.authentication.presentation.utility.UiText

data class SendRestLinkState(
    val email: String? = null,
    val countDownTime: String = "01:00",
    val countDownFinished: Boolean = true,
    val sendEmailLinkSuccessMessage: UiText? = null,
    val sendEmailLinkError: UiText? = null
)