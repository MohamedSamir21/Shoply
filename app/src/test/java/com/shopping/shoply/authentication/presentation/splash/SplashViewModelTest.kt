package com.shopping.shoply.authentication.presentation.splash

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.launch.domain.LaunchDestination
import com.shopping.shoply.launch.domain.LaunchRepository
import com.shopping.shoply.testutil.MainDispatcherExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@DisplayName("SplashViewModel")
class SplashViewModelTest {
    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    private class FakeLaunchRepository(
        var signedIn: Boolean = false,
        var seen: Boolean = false,
    ) : LaunchRepository {
        override fun isSignedIn() = signedIn
        override suspend fun hasSeenOnboarding() = seen
        override suspend fun markOnboardingSeen() {
            seen = true
        }
    }

    private suspend fun SplashViewModel.destination(): LaunchDestination {
        lateinit var result: LaunchDestination
        events.test { result = (awaitItem() as SplashEvent.Navigate).destination }
        return result
    }

    @Test
    @DisplayName("a signed-in user goes straight to Home")
    fun `a signed-in user goes straight to Home`() = runTest(main.dispatcher) {
        val sut = SplashViewModel(FakeLaunchRepository(signedIn = true, seen = true))

        assertThat(sut.destination()).isEqualTo(LaunchDestination.HOME)
    }

    @Test
    @DisplayName("a signed-out user who saw onboarding goes to Sign In")
    fun `a signed-out user who saw onboarding goes to Sign In`() = runTest(main.dispatcher) {
        val sut = SplashViewModel(FakeLaunchRepository(seen = true))

        assertThat(sut.destination()).isEqualTo(LaunchDestination.SIGN_IN)
    }

    @Test
    @DisplayName("the first launch shows onboarding once, then Sign In next time")
    fun `the first launch shows onboarding once, then Sign In next time`() = runTest(main.dispatcher) {
        val repository = FakeLaunchRepository()

        assertThat(SplashViewModel(repository).destination()).isEqualTo(LaunchDestination.ONBOARDING)
        assertThat(SplashViewModel(repository).destination()).isEqualTo(LaunchDestination.SIGN_IN)
    }

    @Test
    @DisplayName("tapping skips the wait and navigates only once")
    fun `tapping skips the wait and navigates only once`() = runTest(main.dispatcher) {
        val sut = SplashViewModel(FakeLaunchRepository(seen = true))

        sut.events.test {
            sut.onIntent(SplashIntent.Skip)
            assertThat((awaitItem() as SplashEvent.Navigate).destination).isEqualTo(LaunchDestination.SIGN_IN)
            testScheduler.advanceUntilIdle()
            expectNoEvents()
        }
    }
}
