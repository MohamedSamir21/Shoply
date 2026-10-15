package com.shopping.shoply.authentication.presentation.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Drives the onboarding pager: tracks the current page across swipes and forward/previous
 * navigation, and flags the state as finished once the user moves forward past the last page.
 */
class OnboardingViewModel : ViewModel() {
    private val _state = MutableStateFlow(
        OnboardingScreenState(
            onboardings = generateOnboardings()
        )
    )
    val state = _state.asStateFlow()


    fun onEvent(event: OnboardingScreenEvent) {
        val currentPageIndex = state.value.currentPageIndex

        when (event) {
            is OnboardingScreenEvent.Swipe -> {
                _state.value = state.value.copy(
                    currentPageIndex = event.index
                )
            }

            OnboardingScreenEvent.Forward -> {
                if (currentPageIndex == state.value.onboardings.size - 1) {
                    _state.value = state.value.copy(finish = true)
                    return
                }
                _state.value = state.value.copy(
                    currentPageIndex = currentPageIndex + 1
                )
            }

            OnboardingScreenEvent.Previous -> {
                if (currentPageIndex <= 0) {
                    return
                }
                _state.value = state.value.copy(
                    currentPageIndex = currentPageIndex - 1
                )
            }
        }
    }
}
