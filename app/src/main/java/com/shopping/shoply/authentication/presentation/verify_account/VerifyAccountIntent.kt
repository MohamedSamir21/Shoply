package com.shopping.shoply.authentication.presentation.verify_account

sealed interface VerifyAccountIntent {
    data object Resend: VerifyAccountIntent
}