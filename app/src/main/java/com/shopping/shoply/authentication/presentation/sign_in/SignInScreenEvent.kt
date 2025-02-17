package com.shopping.shoply.authentication.presentation.sign_in

import com.shopping.shoply.authentication.presentation.utility.UiText

sealed interface SignInScreenEvent {
    sealed class SignInResult : SignInScreenEvent {
        data object Success : SignInResult()
        data class Failure(val uiText: UiText) : SignInResult()
    }
}