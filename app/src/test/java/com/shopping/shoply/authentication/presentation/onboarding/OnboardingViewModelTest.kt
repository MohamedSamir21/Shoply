package com.shopping.shoply.authentication.presentation.onboarding

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("OnboardingViewModel")
class OnboardingViewModelTest {

    private val sut = OnboardingViewModel()

    @Test
    @DisplayName("Swipe sets the current page index directly")
    fun `Swipe sets the current page index directly`() {
        sut.onEvent(OnboardingScreenEvent.Swipe(2))

        assertThat(sut.state.value.currentPageIndex).isEqualTo(2)
    }

    @Test
    @DisplayName("Forward advances to the next page")
    fun `Forward advances to the next page`() {
        sut.onEvent(OnboardingScreenEvent.Forward)

        assertThat(sut.state.value.currentPageIndex).isEqualTo(1)
        assertThat(sut.state.value.finish).isFalse()
    }

    @Test
    @DisplayName("Forward on the last page finishes onboarding instead of advancing")
    fun `Forward on the last page finishes onboarding instead of advancing`() {
        val lastIndex = sut.state.value.onboardings.lastIndex
        sut.onEvent(OnboardingScreenEvent.Swipe(lastIndex))

        sut.onEvent(OnboardingScreenEvent.Forward)

        assertThat(sut.state.value.currentPageIndex).isEqualTo(lastIndex)
        assertThat(sut.state.value.finish).isTrue()
    }

    @Test
    @DisplayName("Previous goes back to the prior page")
    fun `Previous goes back to the prior page`() {
        sut.onEvent(OnboardingScreenEvent.Swipe(2))

        sut.onEvent(OnboardingScreenEvent.Previous)

        assertThat(sut.state.value.currentPageIndex).isEqualTo(1)
    }

    @Test
    @DisplayName("Previous at the first page does not go negative")
    fun `Previous at the first page does not go negative`() {
        sut.onEvent(OnboardingScreenEvent.Previous)

        assertThat(sut.state.value.currentPageIndex).isEqualTo(0)
    }
}