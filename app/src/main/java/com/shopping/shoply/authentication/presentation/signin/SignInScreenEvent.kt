package com.shopping.shoply.authentication.presentation.signin

import com.shopping.shoply.authentication.presentation.utility.UiText

sealed interface SignInScreenEvent {
    sealed class SignInResult : SignInScreenEvent {
        data object Success : SignInResult()
        data class Failure(val uiText: UiText) : SignInResult()
    }
}