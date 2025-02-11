package com.shopping.shoply.authentication.presentation.screen.sign_up

import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.shopping.shoply.authentication.presentation.screen.sign_up.model.ActivityContext
import com.shopping.shoply.authentication.presentation.utility.UiText

sealed class SignUpScreenIntent {
    sealed class Change : SignUpScreenIntent() {
        data class Username(val username: String) : Change()
        data class Email(val email: String) : Change()
        data class PhoneNumber(val phoneNumber: String) : Change()
        data class Password(val password: String) : Change()
        data class ConfirmedPassword(val confirmedPassword: String) : Change()
        data class PasswordVisibility(val show: Boolean) : Change()
    }

    sealed class Clear : SignUpScreenIntent() {
        data object Username : Clear()
        data object Email : Clear()
        data object PhoneNumber : Clear()
        data object Password : Clear()
        data object ConfirmedPassword : Clear()
    }

    sealed class SignUp : SignUpScreenIntent() {
        data object SubmitForm : SignUp()
        data class Google(val activityContext: ActivityContext) : SignUp()
        data class Facebook(
            val loginManager: LoginManager,
            val callbackManager: CallbackManager
        ) : SignUp()
    }

    data object SignIn: SignUpScreenIntent()
}