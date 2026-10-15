package com.shopping.shoply.authentication.presentation.onboarding

import androidx.annotation.DrawableRes
import com.shopping.shoply.authentication.presentation.utility.UiText

data class OnboardingScreenState(
    val currentPageIndex: Int = 0,
    val onboardings: List<Onboarding>,
    val finish: Boolean = false
)

data class Onboarding(
    @param:DrawableRes
    val image: Int,
    val title: UiText,
    val description: UiText
)