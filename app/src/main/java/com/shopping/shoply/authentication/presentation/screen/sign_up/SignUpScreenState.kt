package com.shopping.shoply.authentication.presentation.screen.sign_up

import com.shopping.shoply.authentication.domain.model.User
import com.shopping.shoply.authentication.presentation.utility.UiText

data class SignUpScreenState(
    val user: User = User(),
    val userError: UserError = UserError(),
    val showPassword: Boolean = false,
    val enableSignUp: Boolean = false,
    val isSigningUp: Boolean = false,
    val signUpError: UiText? = null
)

data class UserError(
    val usernameError: String? = null,
    val emailError: String? = null,
    val phoneNumberError: String? = null,
    val passwordError: String? = null,
    val passwordConfirmedError: String? = null
)