package com.shopping.shoply.authentication.presentation.resetpassword.forgotpassword

import com.shopping.shoply.authentication.presentation.resetpassword.forgotpassword.model.ResetMethod
import com.shopping.shoply.authentication.presentation.utility.getResetMethods

data class ForgotPasswordScreenState(
    val resetMethods: List<ResetMethod> = getResetMethods(),
    val selectedResetMethodIndex: Int = 0,
    val email: String? = null,
    val phoneNumber: String? = null,
    // Gates the phone reset path: an unverified number can't be used for recovery.
    val isPhoneVerified: Boolean = false
)


