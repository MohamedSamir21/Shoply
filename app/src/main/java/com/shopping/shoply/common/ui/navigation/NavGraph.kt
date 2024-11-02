package com.shopping.shoply.common.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.window.core.layout.WindowSizeClass
import com.shopping.shoply.authentication.presentation.screen.OnboardingScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Onboarding.value
    ) {
        addOnboardingScreen(navController)
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
