package com.shopping.shoply.common.presentation.navigation


import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
import androidx.compose.ui.window.DialogProperties
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.shopping.shoply.authentication.presentation.onboarding.OnboardingScreen
import com.shopping.shoply.authentication.presentation.result.ResultPurpose
import com.shopping.shoply.authentication.presentation.result.ResultScreen
import com.shopping.shoply.authentication.presentation.splash.SplashScreen
import com.shopping.shoply.authentication.presentation.resetpassword.enteremail.EnterEmailScreen
import com.shopping.shoply.authentication.presentation.resetpassword.enterphone.EnterPhoneScreen
import com.shopping.shoply.authentication.presentation.resetpassword.createpassword.CreateNewPasswordScreen
import com.shopping.shoply.authentication.presentation.resetpassword.forgotpassword.ForgotPasswordScreen
import com.shopping.shoply.authentication.presentation.resetpassword.otpverification.OTPVerificationScreen
import com.shopping.shoply.authentication.presentation.resetpassword.sendresetlink.SendResetLinkScreen
import com.shopping.shoply.authentication.presentation.signin.SignInScreen
import com.shopping.shoply.authentication.presentation.signup.SignUpScreen
import com.shopping.shoply.authentication.presentation.verifyaccount.VerifyAccountScreen
import com.shopping.shoply.authentication.presentation.signup.SignUpViewModel

@Composable
fun ShoplyNavGraph(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass
) {
    val isExpandedScreen = when (windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT -> false
        else -> true
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Splash.route
    ) {
        addSplashScreen(navController)
        addResultScreen(navController)
        addOnboardingScreen(navController)
        addSignupScreen(navController, isExpandedScreen)
        addVerifyAccountScreen(navController)
        addSendResetLinkScreen(navController)
        addSignInScreen(navController, isExpandedScreen)
        addHomeScreen(navController)
        addForgotPasswordScreen(navController, isExpandedScreen)
        addEnterEmailScreen(navController, isExpandedScreen)
        addEnterPhoneScreen(navController, isExpandedScreen)
        addOTPVerificationScreen(navController, isExpandedScreen)
        addCreateNewPasswordScreen(navController, isExpandedScreen)
    }
}

fun NavGraphBuilder.addCreateNewPasswordScreen(
    navController: NavHostController,
    isExpandedScreen: Boolean
) {
    composable(
        route = NavRoutes.ResetPassword.route
    ) {
        CreateNewPasswordScreen(
            navController = navController,
            isExpandedScreen = isExpandedScreen
        )
    }
}

fun NavGraphBuilder.addOTPVerificationScreen(
    navController: NavHostController,
    expandedScreen: Boolean
) {

    composable(
        route = NavRoutes.OTPCodeVerification.route,
        arguments = listOf(
            navArgument(
                name = NavRoutes.OTPCodeVerification.Args.COUNTRY_CODE
            ) {
                type = NavType.StringType
            },
            navArgument(
                name = NavRoutes.OTPCodeVerification.Args.PHONE_NUMBER_WITH_COUNTRY_CODE
            ) {
                type = NavType.StringType
            },
            navArgument(
                name = NavRoutes.OTPCodeVerification.Args.PURPOSE
            ) {
                type = NavType.StringType
            }
        )
    ) {
        OTPVerificationScreen(
            navController = navController,
            expandedScreen = expandedScreen
        )
    }
}


fun NavGraphBuilder.addEnterPhoneScreen(
    navController: NavHostController,
    expandedScreen: Boolean
) {
    composable(
        route = NavRoutes.EnterPhone.route
    ) {
        EnterPhoneScreen(
            navController = navController,
            isExpandedScreen = expandedScreen
        )
    }
}

fun NavGraphBuilder.addSendResetLinkScreen(
    navController: NavHostController
) {
    composable(
        route = NavRoutes.SendResetLink.route,
        arguments = listOf(
            navArgument(
                name = NavRoutes.SendResetLink.Args.EMAIL
            ) {
                type = NavType.StringType
            }
        )
    ) { navBackStackEntry ->
        val email = navBackStackEntry.arguments?.getString(NavRoutes.SendResetLink.Args.EMAIL)

        SendResetLinkScreen(
            navController = navController,
            email = email!!
        )
    }
}

fun NavGraphBuilder.addEnterEmailScreen(
    navController: NavHostController,
    isExpandedScreen: Boolean
) {
    composable(
        route = NavRoutes.EnterEmail.route
    ) {
        EnterEmailScreen(
            navController = navController,
            isExpandedScreen = isExpandedScreen
        )
    }
}

fun NavGraphBuilder.addForgotPasswordScreen(
    navController: NavHostController,
    isExpandedScreen: Boolean
) {
    composable(
        route = NavRoutes.ForgotPassword.route
    ) {
        ForgotPasswordScreen(
            navController = navController,
            isExpandedScreen = isExpandedScreen
        )
    }
}

/**
 * Opens Home with nothing behind it, so Back from Home leaves the app instead of returning to the
 * sign-in screens. Clears the whole graph: Splash (the start destination) is already gone by now.
 */
fun NavHostController.navigateToHome() {
    navigate(NavRoutes.Home.route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}

fun NavGraphBuilder.addHomeScreen(navController: NavHostController) {
    composable(
        route = NavRoutes.Home.route
    ) {
        MainShell(
            // Signed out: back to Sign In with nothing behind it.
            onSignedOut = {
                navController.navigate(NavRoutes.SignIn.route) {
                    popUpTo(navController.graph.id) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }
}

fun NavGraphBuilder.addSignInScreen(
    navController: NavHostController,
    isExpandedScreen: Boolean
) {
    composable(
        route = NavRoutes.SignIn.route
    ) {
        SignInScreen(
            navController = navController,
            isExpandedScreen = isExpandedScreen
        )
    }
}

private fun NavGraphBuilder.addSplashScreen(
    navController: NavHostController
) {
    composable(route = NavRoutes.Splash.route) {
        SplashScreen(navController = navController)
    }
}

private fun NavGraphBuilder.addResultScreen(
    navController: NavHostController
) {
    // Rendered as a dialog so it floats over the screen that launched it; the card
    // animates in with a spring "pop" (see ResultScreen). usePlatformDefaultWidth =
    // false lets the card own its width and the scrim span the whole screen.
    dialog(
        route = NavRoutes.Result.route,
        arguments = listOf(
            navArgument(NavRoutes.Result.Args.PURPOSE) { type = NavType.StringType }
        ),
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
    ) { backStackEntry ->
        val purposeName = backStackEntry.arguments?.getString(NavRoutes.Result.Args.PURPOSE)
        val purpose = runCatching { ResultPurpose.valueOf(purposeName.orEmpty()) }
            .getOrDefault(ResultPurpose.VERIFY_FAILED)

        ResultScreen(
            purpose = purpose,
            onContinue = {
                when (purpose) {
                    ResultPurpose.PASSWORD_RESET -> navController.navigate(NavRoutes.SignIn.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                        launchSingleTop = true
                    }

                    ResultPurpose.EMAIL_VERIFIED -> navController.navigateToHome()

                    ResultPurpose.VERIFY_FAILED -> navController.popBackStack()
                }
            },
            onClose = { navController.popBackStack() }
        )
    }
}

private fun NavGraphBuilder.addOnboardingScreen(
    navController: NavHostController
) {
    composable(
        route = NavRoutes.Onboarding.route
    ) {
        OnboardingScreen(navController = navController)
    }
}

private fun NavGraphBuilder.addSignupScreen(
    navController: NavHostController,
    isExpandedScreen: Boolean
) {
    composable(
        route = NavRoutes.SignUp.route
    ) {
        val viewModel: SignUpViewModel = hiltViewModel()

        SignUpScreen(
            viewModel,
            navController,
            isExpandedScreen
        )
    }
}

private fun NavGraphBuilder.addVerifyAccountScreen(
    navController: NavHostController
) {
    composable(
        route = NavRoutes.VerifyAccount.route
    ) {
        VerifyAccountScreen(
            navController = navController
        )
    }
}