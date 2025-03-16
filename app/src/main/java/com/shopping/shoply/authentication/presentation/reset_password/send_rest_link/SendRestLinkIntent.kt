package com.shopping.shoply.authentication.presentation.reset_password.send_rest_link

sealed interface SendRestLinkIntent {
    data class OnFirstPopUp(val email: String): SendRestLinkIntent
    data object Resend: SendRestLinkIntent
}