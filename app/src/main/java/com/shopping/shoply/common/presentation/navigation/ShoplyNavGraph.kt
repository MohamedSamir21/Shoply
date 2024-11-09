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
import com.shopping.shoply.authentication.presentation.viewmodel.SignUpViewModel

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
        startDestination = NavRoutes.SignUp.value
    ) {
        addOnboardingScreen(navController)
        addSignupScreen(isExpandedScreen)
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
    isExpandedScreen: Boolean
) {
    composable(
        route = NavRoutes.SignUp.value
    ) {
        val viewModel: SignUpViewModel = hiltViewModel()

        SignUpScreen(
            viewModel,
            isExpandedScreen
        )
    }
}
