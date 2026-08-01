package com.shopping.shoply.authentication.presentation.resetpassword.enteremail

import com.shopping.shoply.authentication.presentation.utility.UiText

data class EnterEmailState(
    val email: String = "",
    val error: UiText? = null,
    val isProcessing: Boolean = false
)