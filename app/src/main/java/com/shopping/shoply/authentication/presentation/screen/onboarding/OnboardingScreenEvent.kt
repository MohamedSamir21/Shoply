package com.shopping.shoply.authentication.presentation.screen.onboarding

sealed class OnboardingScreenEvent {
    data class Swipe(val index: Int) : OnboardingScreenEvent()
    data object Forward: OnboardingScreenEvent()
    data object Previous: OnboardingScreenEvent()
}