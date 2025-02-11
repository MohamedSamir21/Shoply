package com.shopping.shoply.authentication.presentation.screen.sign_up

import com.shopping.shoply.authentication.presentation.screen.sign_up.SignUpScreenIntent.SignUp
import com.shopping.shoply.authentication.presentation.utility.UiText

sealed interface SignUpScreenEvent {
    sealed class SignUpResult : SignUp() {
        sealed class Success : SignUpResult() {
            data object Verified: Success()
            data object NotVerified: Success()
        }
        data class Failure(val uiText: UiText) : SignUpResult()
    }
}