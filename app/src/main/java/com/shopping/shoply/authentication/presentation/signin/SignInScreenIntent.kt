package com.shopping.shoply.authentication.presentation.signin

sealed class SignInScreenIntent {
    // No SDK objects here — the data layer injects the Facebook/Google collaborators it needs.
    data object ContinueWithFacebook : SignInScreenIntent()
    data object ContinueWithGoogle : SignInScreenIntent()
    data object SignIn : SignInScreenIntent()
    sealed class Change : SignInScreenIntent() {
        data class Email(val email: String) : Change()
        data class Password(val password: String) : Change()
        data object PasswordVisibility: Change()
    }

    sealed class Clear : SignInScreenIntent() {
        data object Email : Clear()
        data object Password : Clear()
    }
}