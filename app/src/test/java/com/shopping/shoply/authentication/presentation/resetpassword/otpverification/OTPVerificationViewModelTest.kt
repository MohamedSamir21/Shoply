package com.shopping.shoply.authentication.presentation.resetpassword.otpverification

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.model.SendOtpEvent
import com.shopping.shoply.authentication.domain.usecase.auth.LinkPhoneNumberUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.MarkPhoneVerifiedUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SendOTPCodeUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.VerifyOTPCodeUseCase
import com.shopping.shoply.authentication.domain.usecase.countdown.FormatCountDownTimeUseCase
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.presentation.navigation.NavRoutes
import com.shopping.shoply.testutil.MainDispatcherExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
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
@DisplayName("OTPVerificationViewModel")
class OTPVerificationViewModelTest {
    private val sendOTPCodeUseCase = mockk<SendOTPCodeUseCase>()
    private val verifyOTPCodeUseCase = mockk<VerifyOTPCodeUseCase>()
    private val linkPhoneNumberUseCase = mockk<LinkPhoneNumberUseCase>()
    private val markPhoneVerifiedUseCase = mockk<MarkPhoneVerifiedUseCase>()

    // Pure and deterministic (millis in, "MM:SS" out) — mocking it would only restate it.
    private val formatCountDownTimeUseCase = FormatCountDownTimeUseCase()

    @JvmField
    @RegisterExtension
    val mainDispatcherExtension = MainDispatcherExtension()

    // The state property calls .stateIn(viewModelScope, ...) at construction time, so the SUT
    // must be built inside each test body — after MainDispatcherExtension.beforeEach() has set
    // Dispatchers.Main. The nav arguments arrive through a real SavedStateHandle (a plain map
    // under the hood — nothing to mock), and onStart{} immediately collects the send flow, so
    // the factory stubs sendOTPCodeUseCase up front; tests that need to control *when* events
    // arrive pass a MutableSharedFlow as [sendEvents] and emit by hand.
    private fun createSut(
        phoneNumber: String? = "+201001234567",
        countryCode: String? = "+20",
        purpose: String? = OtpPurpose.RESET.name,
        sendEvents: Flow<SendOtpEvent> = flowOf(SendOtpEvent.CodeSent)
    ): OTPVerificationViewModel {
        every { sendOTPCodeUseCase(any(), any(), any()) } returns sendEvents
        val args = buildMap<String, Any?> {
            phoneNumber?.let {
                put(NavRoutes.OTPCodeVerification.Args.PHONE_NUMBER_WITH_COUNTRY_CODE, it)
            }
            countryCode?.let { put(NavRoutes.OTPCodeVerification.Args.COUNTRY_CODE, it) }
            purpose?.let { put(NavRoutes.OTPCodeVerification.Args.PURPOSE, it) }
        }
        return OTPVerificationViewModel(
            formatCountDownTimeUseCase = formatCountDownTimeUseCase,
            sendOTPCodeUseCase = sendOTPCodeUseCase,
            verifyOTPCodeUseCase = verifyOTPCodeUseCase,
            linkPhoneNumberUseCase = linkPhoneNumberUseCase,
            markPhoneVerifiedUseCase = markPhoneVerifiedUseCase,
            savedStateHandle = SavedStateHandle(args)
        )
    }

    @Nested
    @DisplayName("reading the navigation arguments")
    inner class ReadingTheNavigationArguments {
        @Test
        @DisplayName("keeps only the digits of the phone number and country code")
        fun `keeps only the digits of the phone number and country code`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut(phoneNumber = "+201001234567", countryCode = "+20")

                val collectJob = launch { sut.state.collect { } }

                assertThat(sut.state.value.phoneNumber).isEqualTo("201001234567")
                assertThat(sut.state.value.countryCode).isEqualTo("20")

                collectJob.cancel()
            }

        @Test
        @DisplayName("parses the sign-up-link purpose")
        fun `parses the sign-up-link purpose`() = runTest(mainDispatcherExtension.dispatcher) {
            val sut = createSut(purpose = OtpPurpose.SIGNUP_LINK.name)

            assertThat(sut.purpose).isEqualTo(OtpPurpose.SIGNUP_LINK)
        }

        @Test
        @DisplayName("falls back to RESET when the purpose is missing or unknown")
        fun `falls back to RESET when the purpose is missing or unknown`() =
            runTest(mainDispatcherExtension.dispatcher) {
                assertThat(createSut(purpose = "garbage").purpose).isEqualTo(OtpPurpose.RESET)
                assertThat(createSut(purpose = null).purpose).isEqualTo(OtpPurpose.RESET)
            }

        @Test
        @DisplayName("does not send a code when no phone number was passed")
        fun `does not send a code when no phone number was passed`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut(phoneNumber = null)

                val collectJob = launch { sut.state.collect { } }
                advanceUntilIdle()

                verify(exactly = 0) { sendOTPCodeUseCase(any(), any(), any()) }

                collectJob.cancel()
            }
    }

    @Nested
    @DisplayName("sending the code")
    inner class SendingTheCode {
        @Test
        @DisplayName("sends the OTP code once the screen starts collecting")
        fun `sends the OTP code once the screen starts collecting`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut()

                val collectJob = launch { sut.state.collect { } }

                verify(exactly = 1) { sendOTPCodeUseCase("", "201001234567", link = false) }

                collectJob.cancel()
            }

        @Test
        @DisplayName("asks the data layer to link the number when the purpose is sign-up-link")
        fun `asks the data layer to link the number when the purpose is sign-up-link`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut(purpose = OtpPurpose.SIGNUP_LINK.name)

                val collectJob = launch { sut.state.collect { } }

                verify(exactly = 1) { sendOTPCodeUseCase("", "201001234567", link = true) }

                collectJob.cancel()
            }

        @Test
        @DisplayName("sends the code again on Resend")
        fun `sends the code again on Resend`() = runTest(mainDispatcherExtension.dispatcher) {
            val sut = createSut()
            val collectJob = launch { sut.state.collect { } }

            sut.onIntent(OTPVerificationIntent.Resend)

            verify(exactly = 2) { sendOTPCodeUseCase("", "201001234567", link = false) }

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
                val sut = createSut()
                val collectJob = launch { sut.state.collect { } }

                advanceTimeBy(30_000)
                runCurrent()
                assertThat(sut.state.value.countDownTime).isEqualTo("00:30")

                sut.onIntent(OTPVerificationIntent.Resend)

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
    @DisplayName("entering the code")
    inner class EnteringTheCode {
        @Test
        @DisplayName("keeps only the digits of what was typed")
        fun `keeps only the digits of what was typed`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut()
                val collectJob = launch { sut.state.collect { } }

                sut.onIntent(OTPVerificationIntent.ChangeCode("12a3-4"))

                assertThat(sut.state.value.enteredCode).isEqualTo("1234")
                coVerify(exactly = 0) { verifyOTPCodeUseCase(any()) }

                collectJob.cancel()
            }

        @Test
        @DisplayName("caps the code at six digits and auto-submits it once complete")
        fun `caps the code at six digits and auto-submits it once complete`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { verifyOTPCodeUseCase("123456") } returns Result.Success(Unit)
                val sut = createSut()
                val collectJob = launch { sut.state.collect { } }

                sut.otpCodeVerificationResult.test {
                    sut.onIntent(OTPVerificationIntent.ChangeCode("12345678"))

                    assertThat(sut.state.value.enteredCode).isEqualTo("123456")
                    assertThat(awaitItem()).isEqualTo(OTPVerificationEvent.Success)
                }
                coVerify(exactly = 1) { verifyOTPCodeUseCase("123456") }

                collectJob.cancel()
            }
    }

    @Nested
    @DisplayName("verifying the code (reset purpose)")
    inner class VerifyingTheCodeResetPurpose {
        @Test
        @DisplayName("emits Success when the code is correct")
        fun `emits Success when the code is correct`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { verifyOTPCodeUseCase("123456") } returns Result.Success(Unit)
                val sut = createSut()
                val collectJob = launch { sut.state.collect { } }

                sut.otpCodeVerificationResult.test {
                    sut.onIntent(OTPVerificationIntent.ChangeCode("123456"))

                    assertThat(awaitItem()).isEqualTo(OTPVerificationEvent.Success)
                }
                assertThat(sut.state.value.isLoading).isFalse()
                // RESET signs in with the credential; nothing to persist afterwards.
                coVerify(exactly = 0) { markPhoneVerifiedUseCase() }

                collectJob.cancel()
            }

        @Test
        @DisplayName("shows the generic wrong-code error and clears the slots when it is wrong")
        fun `shows the generic wrong-code error and clears the slots when it is wrong`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { verifyOTPCodeUseCase("123456") } returns Result.Error(DataError.Unknown)
                val sut = createSut()
                val collectJob = launch { sut.state.collect { } }

                sut.otpCodeVerificationResult.test {
                    sut.onIntent(OTPVerificationIntent.ChangeCode("123456"))

                    val error = awaitItem() as OTPVerificationEvent.Error
                    val message = error.message as UiText.StringResource
                    assertThat(message.id).isEqualTo(R.string.incorrect_otp_code)
                }
                assertThat(sut.state.value.enteredCode).isEmpty()
                assertThat(sut.state.value.isLoading).isFalse()

                collectJob.cancel()
            }

        @Test
        @DisplayName("ignores a submit while the code is still incomplete")
        fun `ignores a submit while the code is still incomplete`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sut = createSut()
                val collectJob = launch { sut.state.collect { } }

                sut.onIntent(OTPVerificationIntent.ChangeCode("123"))
                sut.proceedToVerifyOTPCode()
                advanceUntilIdle()

                coVerify(exactly = 0) { verifyOTPCodeUseCase(any()) }

                collectJob.cancel()
            }
    }

    @Nested
    @DisplayName("linking the phone (sign-up-link purpose)")
    inner class LinkingThePhoneSignUpLinkPurpose {
        @Test
        @DisplayName("links the number, persists the verified flag, then emits Success")
        fun `links the number, persists the verified flag, then emits Success`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { linkPhoneNumberUseCase("123456") } returns Result.Success(Unit)
                coEvery { markPhoneVerifiedUseCase() } returns Result.Success(Unit)
                val sut = createSut(purpose = OtpPurpose.SIGNUP_LINK.name)
                val collectJob = launch { sut.state.collect { } }

                sut.otpCodeVerificationResult.test {
                    sut.onIntent(OTPVerificationIntent.ChangeCode("123456"))

                    assertThat(awaitItem()).isEqualTo(OTPVerificationEvent.Success)
                }
                coVerify(exactly = 1) { linkPhoneNumberUseCase("123456") }
                coVerify(exactly = 1) { markPhoneVerifiedUseCase() }
                coVerify(exactly = 0) { verifyOTPCodeUseCase(any()) }

                collectJob.cancel()
            }

        @Test
        @DisplayName("surfaces the specific cause when linking fails")
        fun `surfaces the specific cause when linking fails`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { linkPhoneNumberUseCase("123456") } returns Result.Error(DataError.Unknown)
                val sut = createSut(purpose = OtpPurpose.SIGNUP_LINK.name)
                val collectJob = launch { sut.state.collect { } }

                sut.otpCodeVerificationResult.test {
                    sut.onIntent(OTPVerificationIntent.ChangeCode("123456"))

                    val error = awaitItem() as OTPVerificationEvent.Error
                    // Link failures surface the mapped cause (e.g. number already linked),
                    // not the reset flow's generic "incorrect code".
                    val message = error.message as UiText.StringResource
                    assertThat(message.id).isEqualTo(R.string.unknown_error)
                }
                coVerify(exactly = 0) { markPhoneVerifiedUseCase() }

                collectJob.cancel()
            }

        @Test
        @DisplayName("still emits Success when persisting the verified flag fails")
        fun `still emits Success when persisting the verified flag fails`() =
            runTest(mainDispatcherExtension.dispatcher) {
                coEvery { linkPhoneNumberUseCase("123456") } returns Result.Success(Unit)
                coEvery { markPhoneVerifiedUseCase() } returns Result.Error(DataError.Unknown)
                val sut = createSut(purpose = OtpPurpose.SIGNUP_LINK.name)
                val collectJob = launch { sut.state.collect { } }

                sut.otpCodeVerificationResult.test {
                    sut.onIntent(OTPVerificationIntent.ChangeCode("123456"))

                    // A failed flag write must not block the user's navigation forward.
                    assertThat(awaitItem()).isEqualTo(OTPVerificationEvent.Success)
                }

                collectJob.cancel()
            }
    }

    @Nested
    @DisplayName("racing the code against the send confirmation")
    inner class RacingTheCodeAgainstTheSendConfirmation {
        @Test
        @DisplayName("holds a completed code until the send is confirmed, then verifies it")
        fun `holds a completed code until the send is confirmed, then verifies it`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sendEvents = MutableSharedFlow<SendOtpEvent>()
                coEvery { verifyOTPCodeUseCase("123456") } returns Result.Success(Unit)
                val sut = createSut(sendEvents = sendEvents)
                val collectJob = launch { sut.state.collect { } }

                // The user finishes typing before onCodeSent has arrived: the code must be
                // kept (spinner on), not failed and wiped.
                sut.onIntent(OTPVerificationIntent.ChangeCode("123456"))
                assertThat(sut.state.value.isLoading).isTrue()
                assertThat(sut.state.value.enteredCode).isEqualTo("123456")
                coVerify(exactly = 0) { verifyOTPCodeUseCase(any()) }

                sut.otpCodeVerificationResult.test {
                    sendEvents.emit(SendOtpEvent.CodeSent)

                    assertThat(awaitItem()).isEqualTo(OTPVerificationEvent.Success)
                }
                coVerify(exactly = 1) { verifyOTPCodeUseCase("123456") }

                collectJob.cancel()
            }

        @Test
        @DisplayName("does not park an incomplete code while the send is unconfirmed")
        fun `does not park an incomplete code while the send is unconfirmed`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sendEvents = MutableSharedFlow<SendOtpEvent>()
                val sut = createSut(sendEvents = sendEvents)
                val collectJob = launch { sut.state.collect { } }

                sut.onIntent(OTPVerificationIntent.ChangeCode("123"))

                // Three digits pre-confirmation must not flip the spinner on...
                assertThat(sut.state.value.isLoading).isFalse()

                // ...nor register a pending verification for the confirmation to fire later.
                sendEvents.emit(SendOtpEvent.CodeSent)
                advanceUntilIdle()
                coVerify(exactly = 0) { verifyOTPCodeUseCase(any()) }

                collectJob.cancel()
            }

        @Test
        @DisplayName("forwards the user on when Android auto-verifies the SMS")
        fun `forwards the user on when Android auto-verifies the SMS`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sendEvents = MutableSharedFlow<SendOtpEvent>()
                val sut = createSut(sendEvents = sendEvents)
                val collectJob = launch { sut.state.collect { } }

                sut.otpCodeVerificationResult.test {
                    sendEvents.emit(SendOtpEvent.AutoVerified(Result.Success(Unit)))

                    assertThat(awaitItem()).isEqualTo(OTPVerificationEvent.Success)
                }
                // No code was ever typed — manual verification must not have run.
                coVerify(exactly = 0) { verifyOTPCodeUseCase(any()) }

                collectJob.cancel()
            }

        @Test
        @DisplayName("drops a pending code and clears the slots when the send fails")
        fun `drops a pending code and clears the slots when the send fails`() =
            runTest(mainDispatcherExtension.dispatcher) {
                val sendEvents = MutableSharedFlow<SendOtpEvent>()
                val sut = createSut(sendEvents = sendEvents)
                val collectJob = launch { sut.state.collect { } }

                sut.onIntent(OTPVerificationIntent.ChangeCode("123456")) // parks as pending

                sut.otpCodeVerificationResult.test {
                    sendEvents.emit(SendOtpEvent.AutoVerified(Result.Error(DataError.Unknown)))

                    val error = awaitItem() as OTPVerificationEvent.Error
                    val message = error.message as UiText.StringResource
                    assertThat(message.id).isEqualTo(R.string.unknown_error)
                }
                assertThat(sut.state.value.enteredCode).isEmpty()
                assertThat(sut.state.value.isLoading).isFalse()

                // The pending verification was dropped along with the failed send: a later
                // CodeSent must not resurrect it.
                sendEvents.emit(SendOtpEvent.CodeSent)
                advanceUntilIdle()
                coVerify(exactly = 0) { verifyOTPCodeUseCase(any()) }

                collectJob.cancel()
            }
    }
}
