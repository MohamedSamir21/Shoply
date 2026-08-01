package com.shopping.shoply.authentication.presentation.resetpassword.sendresetlink

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.usecase.auth.SendPasswordResetEmailUseCase
import com.shopping.shoply.authentication.domain.usecase.countdown.FormatCountDownTimeUseCase
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.testutil.MainDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
@DisplayName("SendResetLinkViewModel")
class SendResetLinkViewModelTest {
    private val sendPasswordResetEmailUseCase = mockk<SendPasswordResetEmailUseCase>()

    // Pure and deterministic (millis in, "MM:SS" out) — mocking it would only restate it.
    private val formatCountDownTimeUseCase = FormatCountDownTimeUseCase()

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    // The state property calls .stateIn(viewModelScope, ...) at construction time, so the SUT
    // must be built inside each test body — after MainDispatcherExtension.beforeEach() has set
    // Dispatchers.Main — never as a class-level field.
    private fun createSut() = SendResetLinkViewModel(
        sendPasswordResetEmailUseCase = sendPasswordResetEmailUseCase,
        formatCountDownTimeUseCase = formatCountDownTimeUseCase
    )

    @Nested
    @DisplayName("starting up")
    inner class StartingUp {
        @Test
        @DisplayName("starts the countdown as soon as the screen starts collecting")
        fun `starts the countdown as soon as the screen starts collecting`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut()

                val collectJob = launch { sut.state.collect { } }

                assertThat(sut.state.value.countDownFinished).isFalse()
                assertThat(sut.state.value.countDownTime).isEqualTo("01:00")

                collectJob.cancel()
            }

        @Test
        @DisplayName("does not send a reset email before an email is known")
        fun `does not send a reset email before an email is known`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut()

                // onStart{} calls sendPasswordResetEmail(), but state.email is still null at
                // first collection — OnFirstPopUp hasn't delivered it yet — so the send is a
                // no-op rather than a call with a null/empty address.
                val collectJob = launch { sut.state.collect { } }
                advanceUntilIdle()

                coVerify(exactly = 0) { sendPasswordResetEmailUseCase(any()) }

                collectJob.cancel()
            }
    }

    @Nested
    @DisplayName("the resend countdown")
    inner class TheResendCountdown {
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
                coEvery { sendPasswordResetEmailUseCase("mohamed@gmail.com") } returns
                    Result.Success(Unit)
                val sut = createSut()
                val collectJob = launch { sut.state.collect { } }
                sut.onIntent(SendResetLinkIntent.OnFirstPopUp("mohamed@gmail.com"))

                advanceTimeBy(30_000)
                runCurrent()
                assertThat(sut.state.value.countDownTime).isEqualTo("00:30")

                sut.onIntent(SendResetLinkIntent.Resend)

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
    @DisplayName("receiving the email from the pop-up")
    inner class ReceivingTheEmailFromThePopUp {
        @Test
        @DisplayName("stores the email in state")
        fun `stores the email in state`() = runTest(mainDispatcherExtension.dispatcher) {
            val sut = createSut()
            val collectJob = launch { sut.state.collect { } }

            sut.onIntent(SendResetLinkIntent.OnFirstPopUp("mohamed@gmail.com"))

            assertThat(sut.state.value.email).isEqualTo("mohamed@gmail.com")

            collectJob.cancel()
        }
    }

    @Nested
    @DisplayName("resending the reset link")
    inner class ResendingTheResetLink {
        @Test
        @DisplayName("shows a success message when the resend succeeds")
        fun `shows a success message when the resend succeeds`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { sendPasswordResetEmailUseCase("mohamed@gmail.com") } returns
                    Result.Success(Unit)
                val sut = createSut()
                val collectJob = launch { sut.state.collect { } }
                sut.onIntent(SendResetLinkIntent.OnFirstPopUp("mohamed@gmail.com"))

                sut.onIntent(SendResetLinkIntent.Resend)

                val message = sut.state.value.sendEmailLinkSuccessMessage as UiText.StringResource
                assertThat(message.id).isEqualTo(R.string.new_email_password_reset_link)
                assertThat(sut.state.value.sendEmailLinkError).isNull()

                collectJob.cancel()
            }

        @Test
        @DisplayName("shows the mapped error when the resend fails")
        fun `shows the mapped error when the resend fails`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { sendPasswordResetEmailUseCase("mohamed@gmail.com") } returns
                    Result.Error(DataError.Unknown)
                val sut = createSut()
                val collectJob = launch { sut.state.collect { } }
                sut.onIntent(SendResetLinkIntent.OnFirstPopUp("mohamed@gmail.com"))

                sut.onIntent(SendResetLinkIntent.Resend)

                val error = sut.state.value.sendEmailLinkError as UiText.StringResource
                assertThat(error.id).isEqualTo(R.string.unknown_error)
                assertThat(sut.state.value.sendEmailLinkSuccessMessage).isNull()

                collectJob.cancel()
            }

        @Test
        @DisplayName("does nothing when no email was ever provided")
        fun `does nothing when no email was ever provided`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut()
                val collectJob = launch { sut.state.collect { } }

                sut.onIntent(SendResetLinkIntent.Resend)
                advanceUntilIdle()

                coVerify(exactly = 0) { sendPasswordResetEmailUseCase(any()) }
                assertThat(sut.state.value.sendEmailLinkSuccessMessage).isNull()
                assertThat(sut.state.value.sendEmailLinkError).isNull()

                collectJob.cancel()
            }
    }

    @Nested
    @DisplayName("clearing one-shot messages")
    inner class ClearingOneShotMessages {
        @Test
        @DisplayName("clears the resend success message")
        fun `clears the resend success message`() = runTest(mainDispatcherExtension.dispatcher) {
            coEvery { sendPasswordResetEmailUseCase("mohamed@gmail.com") } returns
                Result.Success(Unit)
            val sut = createSut()
            val collectJob = launch { sut.state.collect { } }
            sut.onIntent(SendResetLinkIntent.OnFirstPopUp("mohamed@gmail.com"))
            sut.onIntent(SendResetLinkIntent.Resend)
            assertThat(sut.state.value.sendEmailLinkSuccessMessage).isNotNull()

            sut.resetEmailLinkSuccessMessage()

            assertThat(sut.state.value.sendEmailLinkSuccessMessage).isNull()

            collectJob.cancel()
        }

        @Test
        @DisplayName("clears the resend error message")
        fun `clears the resend error message`() = runTest(mainDispatcherExtension.dispatcher) {
            coEvery { sendPasswordResetEmailUseCase("mohamed@gmail.com") } returns
                Result.Error(DataError.Unknown)
            val sut = createSut()
            val collectJob = launch { sut.state.collect { } }
            sut.onIntent(SendResetLinkIntent.OnFirstPopUp("mohamed@gmail.com"))
            sut.onIntent(SendResetLinkIntent.Resend)
            assertThat(sut.state.value.sendEmailLinkError).isNotNull()

            sut.resetEmailLinkErrorMessage()

            assertThat(sut.state.value.sendEmailLinkError).isNull()

            collectJob.cancel()
        }
    }
}
