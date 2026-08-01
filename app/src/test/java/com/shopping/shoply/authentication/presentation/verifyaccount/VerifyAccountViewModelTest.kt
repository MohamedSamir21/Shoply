package com.shopping.shoply.authentication.presentation.verifyaccount

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.model.User
import com.shopping.shoply.authentication.domain.usecase.auth.GetUserInfoUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.RefreshVerificationStatusUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.ResendVerificationEmailUseCase
import com.shopping.shoply.authentication.domain.usecase.countdown.FormatCountDownTimeUseCase
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.testutil.MainDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("VerifyAccountViewModel")
class VerifyAccountViewModelTest {
    private val resendVerificationEmailUseCase = mockk<ResendVerificationEmailUseCase>()
    private val getUserInfoUseCase = mockk<GetUserInfoUseCase>()
    private val refreshVerificationStatusUseCase = mockk<RefreshVerificationStatusUseCase>()

    // Pure and deterministic (millis in, "MM:SS" out) — mocking it would only restate it.
    private val formatCountDownTimeUseCase = FormatCountDownTimeUseCase()

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    // The state property calls .stateIn(viewModelScope, ...) at construction time, so the SUT
    // must be built inside each test body — after MainDispatcherExtension.beforeEach() has set
    // Dispatchers.Main — never as a class-level field. onStart{} always calls loadEmail(), so
    // the factory stubs getUserInfoUseCase up front instead of repeating it in every test.
    private fun createSut(
        userInfoResult: Result<User, DataError> = Result.Success(User())
    ): VerifyAccountViewModel {
        coEvery { getUserInfoUseCase() } returns userInfoResult
        return VerifyAccountViewModel(
            resendVerificationEmailUseCase = resendVerificationEmailUseCase,
            getUserInfoUseCase = getUserInfoUseCase,
            refreshVerificationStatusUseCase = refreshVerificationStatusUseCase,
            formatCountDownTimeUseCase = formatCountDownTimeUseCase
        )
    }

    @Nested
    @DisplayName("loading the email")
    inner class LoadingTheEmail {
        @Test
        @DisplayName("shows the signed-in user's email once it loads")
        fun `shows the signed-in user's email once it loads`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut(
                    userInfoResult = Result.Success(User(email = "mohamed@gmail.com"))
                )

                // state is built with SharingStarted.Lazily, so onStart { loadEmail() } only
                // fires once something collects it — start a collector, then read the value.
                val collectJob = launch { sut.state.collect { } }
                advanceUntilIdle()

                assertThat(sut.state.value.email).isEqualTo("mohamed@gmail.com")

                collectJob.cancel()
            }

        @Test
        @DisplayName("leaves the email empty when loading the user info fails")
        fun `leaves the email empty when loading the user info fails`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut(userInfoResult = Result.Error(DataError.Unknown))

                val collectJob = launch { sut.state.collect { } }
                advanceUntilIdle()

                assertThat(sut.state.value.email).isNull()

                collectJob.cancel()
            }
    }

    @Nested
    @DisplayName("the resend countdown")
    inner class TheResendCountdown {
        @Test
        @DisplayName("starts counting down as soon as the screen starts collecting")
        fun `starts counting down as soon as the screen starts collecting`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut()

                val collectJob = launch { sut.state.collect { } }

                // No virtual time has passed yet: the ticker is armed but hasn't ticked.
                assertThat(sut.state.value.countDownFinished).isFalse()
                assertThat(sut.state.value.countDownTime).isEqualTo("01:00")

                collectJob.cancel()
            }

        @Test
        @DisplayName("ticks down second by second")
        fun `ticks down second by second`() = runTest(mainDispatcherExtension.dispatcher) {
            val sut = createSut()
            val collectJob = launch { sut.state.collect { } }

            // advanceTimeBy(1000) stops just short of t=1000ms, where the tick is scheduled;
            // runCurrent() then executes everything due at exactly that instant.
            advanceTimeBy(1_000)
            runCurrent()
            assertThat(sut.state.value.countDownTime).isEqualTo("00:59")

            advanceTimeBy(1_000)
            runCurrent()
            assertThat(sut.state.value.countDownTime).isEqualTo("00:58")

            collectJob.cancel()
        }

        @Test
        @DisplayName("flags the countdown finished after the full minute")
        fun `flags the countdown finished after the full minute`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut()
                val collectJob = launch { sut.state.collect { } }

                advanceUntilIdle()

                assertThat(sut.state.value.countDownFinished).isTrue()

                collectJob.cancel()
            }

        @Test
        @DisplayName("restarts from the top when the user resends")
        fun `restarts from the top when the user resends`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { resendVerificationEmailUseCase() } returns Result.Success(Unit)
                val sut = createSut()
                val collectJob = launch { sut.state.collect { } }

                advanceTimeBy(30_000)
                runCurrent()
                assertThat(sut.state.value.countDownTime).isEqualTo("00:30")

                sut.onIntent(VerifyAccountIntent.Resend)

                // One second after the resend the display reads 00:59 — the old ticker was
                // cancelled and a fresh 60s one took its place (not 00:29 from the first run).
                advanceTimeBy(1_000)
                runCurrent()
                assertThat(sut.state.value.countDownFinished).isFalse()
                assertThat(sut.state.value.countDownTime).isEqualTo("00:59")

                collectJob.cancel()
            }
    }

    @Nested
    @DisplayName("resending the verification email")
    inner class ResendingTheVerificationEmail {
        @Test
        @DisplayName("shows a success message when the resend succeeds")
        fun `shows a success message when the resend succeeds`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { resendVerificationEmailUseCase() } returns Result.Success(Unit)
                val sut = createSut()
                val collectJob = launch { sut.state.collect { } }

                sut.onIntent(VerifyAccountIntent.Resend)

                val message = sut.state.value.sendEmailLinkSuccessMessage as UiText.StringResource
                assertThat(message.id).isEqualTo(R.string.new_email_link_verification)
                assertThat(sut.state.value.sendEmailLinkError).isNull()

                collectJob.cancel()
            }

        @Test
        @DisplayName("shows the mapped error when the resend fails")
        fun `shows the mapped error when the resend fails`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { resendVerificationEmailUseCase() } returns Result.Error(DataError.Unknown)
                val sut = createSut()
                val collectJob = launch { sut.state.collect { } }

                sut.onIntent(VerifyAccountIntent.Resend)

                val error = sut.state.value.sendEmailLinkError as UiText.StringResource
                assertThat(error.id).isEqualTo(R.string.unknown_error)
                assertThat(sut.state.value.sendEmailLinkSuccessMessage).isNull()

                collectJob.cancel()
            }
    }

    @Nested
    @DisplayName("confirming the verification")
    inner class ConfirmingTheVerification {
        @Test
        @DisplayName("marks the email verified when Firebase reports it verified")
        fun `marks the email verified when Firebase reports it verified`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { refreshVerificationStatusUseCase() } returns Result.Success(true)
                val sut = createSut()
                val collectJob = launch { sut.state.collect { } }

                sut.onIntent(VerifyAccountIntent.ConfirmVerified)

                assertThat(sut.state.value.isEmailVerified).isTrue()
                assertThat(sut.state.value.isCheckingVerification).isFalse()
                assertThat(sut.state.value.verificationError).isNull()

                collectJob.cancel()
            }

        @Test
        @DisplayName("shows the not-verified-yet message when it is still unverified")
        fun `shows the not-verified-yet message when it is still unverified`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { refreshVerificationStatusUseCase() } returns Result.Success(false)
                val sut = createSut()
                val collectJob = launch { sut.state.collect { } }

                sut.onIntent(VerifyAccountIntent.ConfirmVerified)

                val error = sut.state.value.verificationError as UiText.StringResource
                assertThat(error.id).isEqualTo(R.string.email_not_verified_yet)
                assertThat(sut.state.value.isEmailVerified).isFalse()
                assertThat(sut.state.value.isCheckingVerification).isFalse()

                collectJob.cancel()
            }

        @Test
        @DisplayName("shows the mapped error when the status check fails")
        fun `shows the mapped error when the status check fails`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { refreshVerificationStatusUseCase() } returns Result.Error(DataError.Unknown)
                val sut = createSut()
                val collectJob = launch { sut.state.collect { } }

                sut.onIntent(VerifyAccountIntent.ConfirmVerified)

                val error = sut.state.value.verificationError as UiText.StringResource
                assertThat(error.id).isEqualTo(R.string.unknown_error)
                assertThat(sut.state.value.isEmailVerified).isFalse()

                collectJob.cancel()
            }

        @Test
        @DisplayName("ignores a second confirm while a check is already in flight")
        fun `ignores a second confirm while a check is already in flight`() =
            runTest(mainDispatcherExtension.dispatcher) {
                // Make the check take (virtual) time so the second intent arrives mid-flight.
                coEvery { refreshVerificationStatusUseCase() } coAnswers {
                    delay(500)
                    Result.Success(true)
                }
                val sut = createSut()
                val collectJob = launch { sut.state.collect { } }

                sut.onIntent(VerifyAccountIntent.ConfirmVerified)
                assertThat(sut.state.value.isCheckingVerification).isTrue()

                sut.onIntent(VerifyAccountIntent.ConfirmVerified)
                advanceUntilIdle()

                coVerify(exactly = 1) { refreshVerificationStatusUseCase() }
                assertThat(sut.state.value.isEmailVerified).isTrue()

                collectJob.cancel()
            }
    }

    @Nested
    @DisplayName("the silent refresh on resume")
    inner class TheSilentRefreshOnResume {
        @Test
        @DisplayName("stays quiet when the email is still unverified")
        fun `stays quiet when the email is still unverified`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { refreshVerificationStatusUseCase() } returns Result.Success(false)
                val sut = createSut()
                val collectJob = launch { sut.state.collect { } }

                sut.refreshVerificationSilently()

                assertThat(sut.state.value.verificationError).isNull()
                assertThat(sut.state.value.isEmailVerified).isFalse()

                collectJob.cancel()
            }

        @Test
        @DisplayName("stays quiet when the status check fails")
        fun `stays quiet when the status check fails`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { refreshVerificationStatusUseCase() } returns Result.Error(DataError.Unknown)
                val sut = createSut()
                val collectJob = launch { sut.state.collect { } }

                sut.refreshVerificationSilently()

                assertThat(sut.state.value.verificationError).isNull()

                collectJob.cancel()
            }
    }

    @Nested
    @DisplayName("clearing one-shot messages")
    inner class ClearingOneShotMessages {
        @Test
        @DisplayName("clears the resend success message")
        fun `clears the resend success message`() = runTest(mainDispatcherExtension.dispatcher) {
            coEvery { resendVerificationEmailUseCase() } returns Result.Success(Unit)
            val sut = createSut()
            val collectJob = launch { sut.state.collect { } }
            sut.onIntent(VerifyAccountIntent.Resend)
            assertThat(sut.state.value.sendEmailLinkSuccessMessage).isNotNull()

            sut.resetEmailLinkSuccessMessage()

            assertThat(sut.state.value.sendEmailLinkSuccessMessage).isNull()

            collectJob.cancel()
        }

        @Test
        @DisplayName("clears the resend error message")
        fun `clears the resend error message`() = runTest(mainDispatcherExtension.dispatcher) {
            coEvery { resendVerificationEmailUseCase() } returns Result.Error(DataError.Unknown)
            val sut = createSut()
            val collectJob = launch { sut.state.collect { } }
            sut.onIntent(VerifyAccountIntent.Resend)
            assertThat(sut.state.value.sendEmailLinkError).isNotNull()

            sut.resetEmailLinkErrorMessage()

            assertThat(sut.state.value.sendEmailLinkError).isNull()

            collectJob.cancel()
        }

        @Test
        @DisplayName("clears the verification error")
        fun `clears the verification error`() = runTest(mainDispatcherExtension.dispatcher) {
            coEvery { refreshVerificationStatusUseCase() } returns Result.Success(false)
            val sut = createSut()
            val collectJob = launch { sut.state.collect { } }
            sut.onIntent(VerifyAccountIntent.ConfirmVerified)
            assertThat(sut.state.value.verificationError).isNotNull()

            sut.resetVerificationError()

            assertThat(sut.state.value.verificationError).isNull()

            collectJob.cancel()
        }
    }
}
