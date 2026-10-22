package com.shopping.shoply.profile.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.profile.domain.model.Account
import com.shopping.shoply.testutil.MainDispatcherExtension
import com.shopping.shoply.testutil.fakes.FakeNotificationsRepository
import com.shopping.shoply.testutil.fakes.FakeProfileRepository
import com.shopping.shoply.testutil.fakes.FakeSessionRepository
import com.shopping.shoply.testutil.fakes.FakeWishlistRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@DisplayName("ProfileViewModelTest")
class ProfileViewModelTest {
    val fakeProfileRepository = FakeProfileRepository()
    val fakeWishlistRepository = FakeWishlistRepository()
    val fakeNotificationsRepository = FakeNotificationsRepository()
    val fakeSessionRepository = FakeSessionRepository()

    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    private fun createSut() = ProfileViewModel(
        profileRepository = fakeProfileRepository,
        wishlistRepository = fakeWishlistRepository,
        notificationsRepository = fakeNotificationsRepository,
        sessionRepository = fakeSessionRepository,
    )

    @Test
    @DisplayName("state shows the account, initials and counts")
    fun `state shows the account, initials and counts`() = runTest(main.dispatcher) {
        fakeProfileRepository.account.value = Account(name = "Alex Morgan", email = "alex@mail.com", phone = "+15550100")
        fakeWishlistRepository.ids.value = setOf("p1", "p2", "p3")
        fakeNotificationsRepository.unreadCount.value = 2

        val state = createSut().state.value

        assertThat(state).isEqualTo(
            ProfileState(
                name = "Alex Morgan",
                initials = "AM",
                email = "alex@mail.com",
                phone = "+15550100",
                wishlistCount = 3,
                unreadNotificationCount = 2,
            )
        )
    }

    @Test
    @DisplayName("live changes update the state")
    fun `live changes update the state`() = runTest(main.dispatcher) {
        val sut = createSut()

        fakeWishlistRepository.ids.value = setOf("p1")
        fakeNotificationsRepository.unreadCount.value = 5

        assertThat(sut.state.value.wishlistCount).isEqualTo(1)
        assertThat(sut.state.value.unreadNotificationCount).isEqualTo(5)
    }

    @Test
    @DisplayName("a silent account listener doesn't block the counts")
    fun `a silent account listener doesn't block the counts`() = runTest(main.dispatcher) {
        fakeProfileRepository.silentAccount = true
        fakeWishlistRepository.ids.value = setOf("p1", "p2")

        val state = createSut().state.value

        assertThat(state.wishlistCount).isEqualTo(2)
        assertThat(state.initials).isNull()
    }

    @Test
    @DisplayName("LogOutClick opens the dialog and LogOutDismiss closes it")
    fun `LogOutClick opens the dialog and LogOutDismiss closes it`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.onIntent(ProfileIntent.LogOutClick)
        assertThat(sut.state.value.showLogoutDialog).isTrue()

        sut.onIntent(ProfileIntent.LogOutDismiss)
        assertThat(sut.state.value.showLogoutDialog).isFalse()
        assertThat(fakeSessionRepository.signOutCalls).isEqualTo(0)
    }

    @Test
    @DisplayName("LogOutConfirm signs out once, closes the dialog and sends SignedOut")
    fun `LogOutConfirm signs out once, closes the dialog and sends SignedOut`() = runTest(main.dispatcher) {
        val sut = createSut()
        sut.onIntent(ProfileIntent.LogOutClick)

        sut.events.test {
            sut.onIntent(ProfileIntent.LogOutConfirm)

            assertThat(awaitItem()).isEqualTo(ProfileEvent.SignedOut)
        }
        assertThat(fakeSessionRepository.signOutCalls).isEqualTo(1)
        assertThat(sut.state.value.showLogoutDialog).isFalse()
    }
}
