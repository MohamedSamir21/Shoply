package com.shopping.shoply.common.presentation.navigation


import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.shopping.shoply.authentication.presentation.onboarding.OnboardingScreen
import com.shopping.shoply.authentication.presentation.reset_password.enter_email.EnterEmailScreen
import com.shopping.shoply.authentication.presentation.reset_password.forgot_password.ForgotPasswordScreen
import com.shopping.shoply.authentication.presentation.sign_in.SignInScreen
import com.shopping.shoply.authentication.presentation.sign_up.SignUpScreen
import com.shopping.shoply.authentication.presentation.verify_account.VerifyAccountScreen
import com.shopping.shoply.authentication.presentation.sign_up.SignUpViewModel
import com.shopping.shoply.authentication.presentation.verify_account.VerifyAccountIntent
import com.shopping.shoply.authentication.presentation.verify_account.VerifyAccountViewModel
import com.shopping.shoply.authentication.presentation.verify_result.VerificationResultScreen
import com.shopping.shoply.common.presentation.util.getActivity

@Composable
fun ShoplyNavGraph(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass
) {
    val isExpandedScreen = when (windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT -> false
        else -> true
    }
    val deepLink = extractDeepLink()

    NavHost(
        navController = navController,
        startDestination = if (deepLink != null) {
            NavRoutes.VerificationResult.route
        } else {
            NavRoutes.EnterEmail.route
        }
    ) {
        addOnboardingScreen(navController)
        addSignupScreen(navController, isExpandedScreen)
        addVerifyAccountScreen(navController)
        addSignInScreen(navController, isExpandedScreen)
        addVerificationResultScreen(navController, deepLink.toString())
        addHomeScreen()
        addForgotPasswordScreen(navController, isExpandedScreen)
        addEnterEmailScreen(navController, isExpandedScreen)
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

fun NavGraphBuilder.addVerificationResultScreen(
    navController: NavHostController,
    deepLink: String
) {
    composable(
        route = NavRoutes.VerificationResult.route,
        deepLinks = listOf(
            navDeepLink {
                uriPattern = DeepLinks.VERIFICATION_RESULT_ROUTE
            }
        )
    ) {
        VerificationResultScreen(
            navHostController = navController,
            deepLink = deepLink
        )
    }
}

fun NavGraphBuilder.addHomeScreen() {
    composable(
        route = NavRoutes.Home.route
    ) {

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
        route = NavRoutes.VerifyAccount.route,
        arguments = listOf(
            navArgument(
                name = NavRoutes.VerifyAccount.Args.EMAIL
            ) {
                type = NavType.StringType
            }
        )
    ) { navBackStack ->

        val email = navBackStack.arguments?.getString(NavRoutes.VerifyAccount.Args.EMAIL)

        VerifyAccountScreen(
            navController = navController,
            email = email
        )
    }
}

@Composable
private fun extractDeepLink(): Uri? {
    val context = LocalContext.current
    val activity = context.getActivity()
    val deepLink = activity.intent.data
    return deepLink
}