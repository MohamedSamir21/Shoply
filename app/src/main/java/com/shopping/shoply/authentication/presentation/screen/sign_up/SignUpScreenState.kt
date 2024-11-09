package com.shopping.shoply.authentication.presentation.screen.sign_up

data class SignUpScreenState(
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val showPassword: Boolean = false,
    val confirmedPassword: String = "",
    val enableSignUp: Boolean = false
)