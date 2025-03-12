package com.shopping.shoply.authentication.presentation.reset_password.enter_email

sealed interface EnterEmailEvent {
    data object Success: EnterEmailEvent
}