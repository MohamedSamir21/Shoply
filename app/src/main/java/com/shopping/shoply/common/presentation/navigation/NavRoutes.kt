package com.shopping.shoply.common.presentation.navigation

sealed class NavRoutes(val route: String) {
    data object Splash : NavRoutes(route = "splash")
    data object Onboarding : NavRoutes(route = "onboarding")
    data object SignIn : NavRoutes(route = "sign_in")

    data object Result : NavRoutes(route = "result/{purpose}") {
        object Args {
            const val PURPOSE = "purpose"
        }

        fun build(purpose: String): String = "result/$purpose"
    }

    data object SignUp : NavRoutes(route = "sign_up")
    data object VerifyAccount : NavRoutes(route = "verify_account")

    data object SendResetLink : NavRoutes(route = "send_reset_link/{email}") {
        object Args {
            const val EMAIL = "email"
        }
    }

    data object OTPCodeVerification : NavRoutes(route = "otp_verification/{${Args.COUNTRY_CODE}}/{${Args.PHONE_NUMBER_WITH_COUNTRY_CODE}}/{${Args.PURPOSE}}") {
        object Args {
            const val COUNTRY_CODE = "country_code"
            const val PHONE_NUMBER_WITH_COUNTRY_CODE = "phone_number_with_country_code"
            const val PURPOSE = "purpose"
        }
    }

    data object ForgotPassword : NavRoutes(route = "forgot_password")
    data object EnterEmail : NavRoutes(route = "enter_email")
    data object EnterPhone : NavRoutes(route = "enter_phone")
    data object VerifyPhone : NavRoutes(route = "verify_phone")
    data object ResetPassword : NavRoutes(route = "reset_password")
    data object Home : NavRoutes("home")
}