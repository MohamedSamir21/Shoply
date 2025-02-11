package com.shopping.shoply.common.presentation.navigation

sealed class NavRoutes(val route: String) {
    data object Onboarding: NavRoutes(route = "onboarding")
    data object SignIn: NavRoutes(route = "sign_in")
    data object SignUp: NavRoutes(route = "sign_up")
    data object VerifyAccount: NavRoutes(route = "verify_account/{email}") {
        object Args {
            const val EMAIL = "email"
        }
    }
    data object VerificationResult: NavRoutes(route = "verification_result")
    data object ForgotPassword: NavRoutes(route = "forgot_password")
    data object VerifyEmail: NavRoutes(route = "verify_email")
    data object VerifyPhone: NavRoutes(route = "verify_phone")
    data object ResetPassword: NavRoutes(route = "reset_password")
    data object Home: NavRoutes("home")
}