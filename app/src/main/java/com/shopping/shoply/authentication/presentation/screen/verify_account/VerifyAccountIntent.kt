package com.shopping.shoply.authentication.presentation.screen.verify_account

sealed interface VerifyAccountIntent {
    data object Resend: VerifyAccountIntent
}