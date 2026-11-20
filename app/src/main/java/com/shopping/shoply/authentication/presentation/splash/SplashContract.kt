package com.shopping.shoply.authentication.presentation.splash

import com.shopping.shoply.launch.domain.LaunchDestination

/** Where the app goes once the splash ends; null while it is still being worked out. */
data class SplashState(val destination: LaunchDestination? = null)

sealed interface SplashIntent {
    /** The user tapped the splash to skip the wait. */
    data object Skip : SplashIntent
}

sealed interface SplashEvent {
    data class Navigate(val destination: LaunchDestination) : SplashEvent
}
