package com.shopping.shoply.authentication.presentation.verifyaccount

sealed interface VerifyAccountIntent {
    data object Resend: VerifyAccountIntent
    // User tapped "continue": re-check verification, showing a message if still unverified.
    data object ConfirmVerified: VerifyAccountIntent
}