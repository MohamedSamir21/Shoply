package com.shopping.shoply.authentication.presentation.resetpassword.sendresetlink

sealed interface SendResetLinkIntent {
    data class OnFirstPopUp(val email: String): SendResetLinkIntent
    data object Resend: SendResetLinkIntent
}