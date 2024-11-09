package com.shopping.shoply.authentication.presentation.screen.sign_up

sealed class SignUpScreenEvent {
    data object Change: SignUpScreenEvent() {
        data class Username(val username: String) : SignUpScreenEvent()
        data class Email(val email: String) : SignUpScreenEvent()
        data class PhoneNumber(val phoneNumber: String) : SignUpScreenEvent()
        data class Password(val password: String) : SignUpScreenEvent()
        data class ConfirmedPassword(val confirmedPassword: String) : SignUpScreenEvent()
        data class PasswordVisibility(val show: Boolean): SignUpScreenEvent()
    }
    data object Clear: SignUpScreenEvent() {
        data object Username : SignUpScreenEvent()
        data object Email : SignUpScreenEvent()
        data object PhoneNumber : SignUpScreenEvent()
        data object Password : SignUpScreenEvent()
        data object ConfirmedPassword : SignUpScreenEvent()
    }
    data object SignUp: SignUpScreenEvent()
}