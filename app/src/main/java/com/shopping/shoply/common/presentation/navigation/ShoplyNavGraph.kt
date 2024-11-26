package com.shopping.shoply.common.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.shopping.shoply.authentication.presentation.screen.onboarding.OnboardingScreen
import com.shopping.shoply.authentication.presentation.screen.sign_up.SignUpScreen
import com.shopping.shoply.authentication.presentation.screen.verify_account.VerifyAccountScreen
import com.shopping.shoply.authentication.presentation.screen.sign_up.SignUpViewModel

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
        startDestination = NavRoutes.VerifyAccount.value
    ) {
        addOnboardingScreen(navController)
        addSignupScreen(navController, isExpandedScreen)
        addVerifyAccount()
    }
}

private fun NavGraphBuilder.addOnboardingScreen(
    navController: NavHostController
) {
    composable(
        route = NavRoutes.Onboarding.value
    ) {
        OnboardingScreen(navController = navController)
    }
}

private fun NavGraphBuilder.addSignupScreen(
    navController: NavHostController,
    isExpandedScreen: Boolean
) {
    composable(
        route = NavRoutes.SignUp.value
    ) {
        val viewModel: SignUpViewModel = hiltViewModel()

        SignUpScreen(
            viewModel,
            navController,
            isExpandedScreen
        )
    }
}

private fun NavGraphBuilder.addVerifyAccount() {
    composable(
        route = NavRoutes.VerifyAccount.value
    ) {
        VerifyAccountScreen()
    }
}