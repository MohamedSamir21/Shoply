package com.shopping.shoply.authentication.presentation.reset_password.enter_phone

sealed interface EnterPhoneEvent {
    data object Success: EnterPhoneEvent
}