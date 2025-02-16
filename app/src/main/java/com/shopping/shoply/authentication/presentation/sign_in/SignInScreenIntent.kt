package com.shopping.shoply.authentication.presentation.sign_in

import android.content.Context
import com.facebook.CallbackManager
import com.facebook.login.LoginManager

sealed class SignInScreenIntent {
    data class ContinueWithFacebook(
        val loginManager: LoginManager,
        val callbackManager: CallbackManager
    ) : SignInScreenIntent()
    data class ContinueWithGoogle(val context: Context) : SignInScreenIntent()
    data object SignIn : SignInScreenIntent()
    sealed class Change : SignInScreenIntent() {
        data class Email(val email: String) : Change()
        data class Password(val password: String) : Change()
        data class PasswordVisibility(val show: Boolean) : Change()
    }

    sealed class Clear : SignInScreenIntent() {
        data object Email : Clear()
        data object Password : Clear()
    }
}