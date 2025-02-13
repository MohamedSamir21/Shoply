package com.shopping.shoply.authentication.presentation.sign_in

data class SignInScreenState(
    val email: String,
    val password: String,
    val errorMessage: String? = null,
    val showPassword: Boolean = false
)
