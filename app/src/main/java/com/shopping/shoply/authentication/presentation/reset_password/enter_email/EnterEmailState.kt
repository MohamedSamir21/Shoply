package com.shopping.shoply.authentication.presentation.reset_password.enter_email

import com.shopping.shoply.authentication.presentation.utility.UiText

data class EnterEmailState(
    val email: String = "",
    val error: UiText? = null,
    val isProcessing: Boolean = false
)