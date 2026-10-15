package com.shopping.shoply.authentication.presentation.resetpassword.enteremail

sealed interface EnterEmailEvent {
    data object Success: EnterEmailEvent
}