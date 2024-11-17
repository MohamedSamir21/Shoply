package com.shopping.shoply.authentication.domain.model

data class User(
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val confirmedPassword: String = ""
)
