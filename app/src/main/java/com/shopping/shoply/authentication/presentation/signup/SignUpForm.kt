package com.shopping.shoply.authentication.presentation.signup

// Presentation-only form model. Holds raw user input including password &
// confirmedPassword (pure UI/validation concerns). Mapped to the domain User
// (+ a separate password) on submit, so credentials never enter the domain entity.
data class SignUpForm(
    val username: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val confirmedPassword: String = ""
)