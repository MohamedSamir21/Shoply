package com.shopping.shoply.authentication.presentation.screen

sealed class OnboardingScreenEvent {
    data class Swipe(val index: Int) : OnboardingScreenEvent()
    data object Forward: OnboardingScreenEvent()
    data object Previous: OnboardingScreenEvent()
}