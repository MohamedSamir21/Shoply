package com.shopping.shoply.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.authentication.domain.interfaces.repository.AuthRepository
import com.shopping.shoply.authentication.domain.model.SendOtpEvent
import com.shopping.shoply.authentication.domain.model.User
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.profile.domain.model.Account
import com.shopping.shoply.settings.domain.SettingsRepository
import com.shopping.shoply.settings.domain.model.AppSettings
import com.shopping.shoply.settings.domain.model.NotificationType
import com.shopping.shoply.settings.domain.model.ThemeMode
import com.shopping.shoply.settings.domain.rules.isDarkTheme
import com.shopping.shoply.settings.domain.rules.maskEmail
import com.shopping.shoply.settings.presentation.SettingsEvent
import com.shopping.shoply.settings.presentation.SettingsIntent
import com.shopping.shoply.settings.presentation.SettingsViewModel
import com.shopping.shoply.testutil.MainDispatcherExtension
import com.shopping.shoply.testutil.fakes.FakeProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@DisplayName("SettingsTest")
class SettingsTest {
    val fakeSettings = FakeSettingsRepository()
    val fakeAuth = FakeResetAuthRepository()
    val fakeProfileRepository = FakeProfileRepository().apply { account.value = Account("Ahmed", "ahmed@gmail.com", null) }

    @RegisterExtension
    @JvmField
    val main = MainDispatcherExtension()

    private fun createSut() = SettingsViewModel(fakeSettings, fakeAuth, fakeProfileRepository)

    @Test
    @DisplayName("isDarkTheme follows the device only for System; maskEmail keeps the last 3 before @")
    fun `rules`() {
        assertThat(isDarkTheme(ThemeMode.LIGHT, systemDark = true)).isFalse()
        assertThat(isDarkTheme(ThemeMode.DARK, systemDark = false)).isTrue()
        assertThat(isDarkTheme(ThemeMode.SYSTEM, systemDark = true)).isTrue()
        assertThat(maskEmail("ahmed@gmail.com")).isEqualTo("**med@gmail.com")
        assertThat(maskEmail("al@x.io")).isEqualTo("al@x.io")
    }

    @Test
    @DisplayName("theme and notification choices are saved and shown")
    fun `saves choices`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.onIntent(SettingsIntent.SelectTheme(ThemeMode.DARK))
        sut.onIntent(SettingsIntent.ToggleNotification(NotificationType.OFFERS, true))

        assertThat(sut.state.value.settings.theme).isEqualTo(ThemeMode.DARK)
        assertThat(sut.state.value.settings.isOn(NotificationType.OFFERS)).isTrue()
    }

    @Test
    @DisplayName("Change Password emails the account address and reports it masked; a failure says ResetFailed")
    fun `change password`() = runTest(main.dispatcher) {
        val sut = createSut()

        sut.events.test {
            sut.onIntent(SettingsIntent.ChangePassword)
            assertThat(awaitItem()).isEqualTo(SettingsEvent.ResetLinkSent("**med@gmail.com"))

            fakeAuth.result = Result.Error(DataError.Network)
            sut.onIntent(SettingsIntent.ChangePassword)
            assertThat(awaitItem()).isEqualTo(SettingsEvent.ResetFailed)
        }
        assertThat(fakeAuth.resetEmails).containsExactly("ahmed@gmail.com", "ahmed@gmail.com")
    }

    class FakeSettingsRepository : SettingsRepository {
        val settings = MutableStateFlow(AppSettings())
        override fun observe() = settings
        override suspend fun setTheme(mode: ThemeMode) = settings.update { it.copy(theme = mode) }
        override suspend fun setNotification(type: NotificationType, enabled: Boolean) =
            settings.update { it.copy(notifications = it.notifications + (type to enabled)) }
    }

    /** Only [sendPasswordResetEmail] is used by Settings; everything else is unreachable here. */
    class FakeResetAuthRepository : AuthRepository {
        var result: Result<Unit, DataError> = Result.Success(Unit)
        val resetEmails = mutableListOf<String>()

        override suspend fun sendPasswordResetEmail(email: String): Result<Unit, DataError> {
            resetEmails += email
            return result
        }

        override suspend fun signUp(user: User, password: String) = unused()
        override suspend fun signIn(email: String, password: String) = unused()
        override suspend fun signUpWithGoogle() = unused()
        override suspend fun signUpWithFacebook() = unused()
        override suspend fun resendVerificationEmailLink() = unused()
        override suspend fun refreshVerificationStatus(): Result<Boolean, DataError> = error("unused")
        override suspend fun getUserInfo(): Result<User, DataError> = error("unused")
        override suspend fun checkIfEmailExists(email: String) = unused()
        override suspend fun checkIfPhoneNumberExists(phoneNumber: String) = unused()
        override suspend fun markPhoneNumberVerified() = unused()
        override fun sendOTPCode(countryCode: String, phoneNumber: String, link: Boolean): Flow<SendOtpEvent> = error("unused")
        override suspend fun verifyOTPCode(code: String) = unused()
        override suspend fun linkPhoneNumber(code: String) = unused()
        override suspend fun updatePassword(newPassword: String) = unused()

        private fun unused(): Result<Unit, DataError> = error("unused")
    }
}
