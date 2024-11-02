package com.shopping.shoply.authentication.presentation.screen

import androidx.annotation.DrawableRes

data class OnboardingScreenState(
    val currentPageIndex: Int = 0,
    val onboardings: List<Onboarding>,
    val finish: Boolean = false
)

data class Onboarding(
    @DrawableRes
    val image: Int,
    val title: String,
    val description: String
)