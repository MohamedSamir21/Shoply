package com.shopping.shoply.authentication.presentation.signup

import com.shopping.shoply.authentication.presentation.utility.UiText

sealed interface SignUpScreenEvent {
    sealed class SignUpResult : SignUpScreenEvent {
        sealed class Success : SignUpResult() {
            data object Verified: Success()
            data object NotVerified: Success()
        }
        data class Failure(val uiText: UiText) : SignUpResult()
    }
}