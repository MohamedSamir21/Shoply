package com.shopping.shoply.common.presentation.navigation

sealed class NavRoutes(val value: String) {
    data object Onboarding: NavRoutes(value = "onboarding")
    data object SignIn: NavRoutes(value = "sign_in")
    data object SignUp: NavRoutes(value = "sign_up")
    data object VerifyAccount: NavRoutes("verify_account")
    data object ForgotPassword: NavRoutes(value = "forgot_password")
    data object VerifyEmail: NavRoutes("verify_email")
    data object VerifyPhone: NavRoutes("verify_phone")
    data object ResetPassword: NavRoutes("reset_password")
}