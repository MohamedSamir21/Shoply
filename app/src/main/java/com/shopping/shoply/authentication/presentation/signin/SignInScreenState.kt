package com.shopping.shoply.authentication.presentation.signin

import com.shopping.shoply.authentication.presentation.utility.UiText

data class SignInScreenState(
    val email: String = "",
    val password: String = "",
    val emailValidationError: UiText? = null,
    val signInError: UiText? = null,
    val showPassword: Boolean = false,
    val enableSignIn: Boolean = false,
    val isSigningIn: Boolean = false
)
