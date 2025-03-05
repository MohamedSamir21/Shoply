package com.shopping.shoply.authentication.presentation.reset_password.forgot_password

import com.shopping.shoply.authentication.presentation.reset_password.forgot_password.model.RestMethod
import com.shopping.shoply.authentication.presentation.utility.getRestMethods

data class ForgotPasswordScreenState(
    val restMethods: List<RestMethod> = getRestMethods(),
    val selectedRestMethodIndex: Int = 0
)


