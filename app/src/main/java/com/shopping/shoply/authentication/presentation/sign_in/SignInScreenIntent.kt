package com.shopping.shoply.authentication.presentation.sign_in

sealed class SignInScreenIntent {
    data object ContinueWithFacebook: SignInScreenIntent()
    data object ContinueWithGoogle: SignInScreenIntent()
    data object SignIn: SignInScreenIntent()
    sealed class Change: SignInScreenIntent() {
        data class Email(val email: String): Change()
        data class Password(val password: String): Change()
        data class PasswordVisibility(val show: Boolean): Change()
    }
    sealed class Clear: SignInScreenIntent() {
        data object Email: Clear()
        data object Password: Clear()
    }
}