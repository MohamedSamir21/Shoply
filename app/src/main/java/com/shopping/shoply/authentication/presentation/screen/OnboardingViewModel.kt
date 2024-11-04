package com.shopping.shoply.authentication.presentation.screen

import androidx.lifecycle.ViewModel
import com.shopping.shoply.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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
                if (currentPageIndex < 0)
                    return
                _state.value = state.value.copy(
                    currentPageIndex = currentPageIndex - 1
                )
            }
        }
    }
}

fun generateOnboardings(): List<Onboarding> {
    return listOf(
        Onboarding(
            image = R.drawable.shopping_illustration,
            title = "Shop From your favorite store",
            description = "Discover a world of convenience and endless choices"
        ),
        Onboarding(
            image = R.drawable.delivery_illustration,
            title = "Get IT Delivered",
            description = "Get a fast and reliable delivery service"
        ),
        Onboarding(
            image = R.drawable.payment_illustration,
            title = "Flexible payment",
            description = "Pay in versatile and secure payment methods"
        )
    )
}