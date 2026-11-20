package com.shopping.shoply.authentication.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.launch.domain.LaunchDestination
import com.shopping.shoply.launch.domain.LaunchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Picks the first screen: Home when signed in, Sign In when onboarding was already seen, and
 * onboarding only on the very first launch.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val launchRepository: LaunchRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(SplashState())
    val state = _state.asStateFlow()
    private val _events = Channel<SplashEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private var navigated = false

    init {
        viewModelScope.launch {
            val destination = when {
                launchRepository.isSignedIn() -> LaunchDestination.HOME
                launchRepository.hasSeenOnboarding() -> LaunchDestination.SIGN_IN
                else -> LaunchDestination.ONBOARDING.also { launchRepository.markOnboardingSeen() }
            }
            _state.update { it.copy(destination = destination) }
            delay(SPLASH_DURATION_MS)
            navigate()
        }
    }

    fun onIntent(intent: SplashIntent) {
        when (intent) {
            SplashIntent.Skip -> navigate()
        }
    }

    /** Navigates once, as soon as the destination is known (a tap before that is ignored). */
    private fun navigate() {
        val destination = state.value.destination ?: return
        if (navigated) return
        navigated = true
        _events.trySend(SplashEvent.Navigate(destination))
    }

    private companion object {
        // Kept short because the OS cold-start splash (installSplashScreen) already runs ~1.2s first.
        const val SPLASH_DURATION_MS = 1400L
    }
}
