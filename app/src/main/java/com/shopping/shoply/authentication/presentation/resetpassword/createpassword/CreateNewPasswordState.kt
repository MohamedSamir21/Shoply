package com.shopping.shoply.authentication.presentation.resetpassword.createpassword

import com.shopping.shoply.authentication.domain.model.PasswordStrength
import com.shopping.shoply.authentication.presentation.utility.UiText

data class CreateNewPasswordState(
    val password: String = "",
    val confirmedPassword: String = "",
    val showPassword: Boolean = false,
    // Live strength feedback for the meter; NONE renders nothing.
    val passwordStrength: PasswordStrength = PasswordStrength.NONE,
    val passwordError: UiText? = null,
    val confirmedPasswordError: UiText? = null,
    // Backend (update password) failure, shown as a toast.
    val error: UiText? = null,
    val isProcessing: Boolean = false,
    // Drives the success result dialog overlay.
    val resetSucceeded: Boolean = false
)
