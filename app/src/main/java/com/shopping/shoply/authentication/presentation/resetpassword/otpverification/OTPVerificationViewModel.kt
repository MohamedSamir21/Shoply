package com.shopping.shoply.authentication.presentation.resetpassword.otpverification

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.authentication.domain.model.SendOtpEvent
import com.shopping.shoply.authentication.domain.usecase.auth.LinkPhoneNumberUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.MarkPhoneVerifiedUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SendOTPCodeUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.VerifyOTPCodeUseCase
import com.shopping.shoply.authentication.domain.usecase.countdown.FormatCountDownTimeUseCase
import com.shopping.shoply.R
import com.shopping.shoply.authentication.presentation.utility.CountdownTicker
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.authentication.presentation.utility.asUiText
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.presentation.navigation.NavRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Backs the OTP entry screen — the single owner of sending the code: callers navigate here with
 * the phone number and an [OtpPurpose], and the code is sent on first collection of [state] and
 * re-sent on demand. A code completed before the send is confirmed is held and verified once the
 * confirmation arrives; a correct code either signs the user in ([OtpPurpose.RESET]) or links the
 * number onto the new account ([OtpPurpose.SIGNUP_LINK]), with outcomes delivered as one-shot
 * [otpCodeVerificationResult] events.
 */
@HiltViewModel
class OTPVerificationViewModel @Inject constructor(
    private val formatCountDownTimeUseCase: FormatCountDownTimeUseCase,
    private val sendOTPCodeUseCase: SendOTPCodeUseCase,
    private val verifyOTPCodeUseCase: VerifyOTPCodeUseCase,
    private val linkPhoneNumberUseCase: LinkPhoneNumberUseCase,
    private val markPhoneVerifiedUseCase: MarkPhoneVerifiedUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    // This screen is the single owner of sending the OTP. Callers (forgot-password or
    // enter-phone) navigate here with the full E.164 number as digits (no '+'); the code is
    // sent on start and re-sent on demand.
    private val argPhoneNumber: String? =
        savedStateHandle.get<String>(NavRoutes.OTPCodeVerification.Args.PHONE_NUMBER_WITH_COUNTRY_CODE)
            ?.filter(Char::isDigit)
            ?.takeIf { it.isNotEmpty() }
    private val argCountryCode: String? =
        savedStateHandle.get<String>(NavRoutes.OTPCodeVerification.Args.COUNTRY_CODE)
            ?.filter(Char::isDigit)
            ?.takeIf { it.isNotEmpty() }

    /**
     * Why the user is here: [OtpPurpose.RESET] (verify -> sign in) or [OtpPurpose.SIGNUP_LINK]
     * (verify -> link the phone onto the just-created account).
     */
    val purpose: OtpPurpose =
        savedStateHandle.get<String>(NavRoutes.OTPCodeVerification.Args.PURPOSE)
            ?.let { runCatching { OtpPurpose.valueOf(it) }.getOrNull() }
            ?: OtpPurpose.RESET

    // Verification needs a verificationId, which only exists once the code has actually been
    // sent (onCodeSent), so there's a brief window after start before it's ready.
    private var isCodeSent: Boolean = false

    // Set if the user completes the code before the send is confirmed; verified once it is.
    private var pendingVerification: Boolean = false

    private val _state =
        MutableStateFlow(
            OTPVerificationState(
                countryCode = argCountryCode.orEmpty(),
                phoneNumber = argPhoneNumber.orEmpty()
            )
        )
    val state = _state.onStart {
        argPhoneNumber?.let { sendOTPCode(it) }
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        OTPVerificationState(
            countryCode = argCountryCode.orEmpty(),
            phoneNumber = argPhoneNumber.orEmpty()
        )
    )
    private val _otpCodeVerificationResult = Channel<OTPVerificationEvent>()
    val otpCodeVerificationResult = _otpCodeVerificationResult.receiveAsFlow()

    // Lazy: viewModelScope must not be touched at construction time, or a test's class-level
    // `sut` field would crash before MainDispatcherExtension gets to set Dispatchers.Main.
    private val countdownTicker by lazy {
        CountdownTicker(
            scope = viewModelScope,
            durationMillis = 60_000L,
            intervalMillis = 1_000L,
            onTick = { remaining ->
                _state.update { it.copy(countDownTime = formatCountDownTimeUseCase(remaining)) }
            },
            onFinish = { _state.update { it.copy(countDownFinished = true) } }
        )
    }

    private fun startCountDownTimer() {
        _state.update {
            it.copy(
                countDownFinished = false
            )
        }
        countdownTicker.start()
    }

    fun onIntent(intent: OTPVerificationIntent) {
        when (intent) {
            is OTPVerificationIntent.ChangeCode -> onChangeCode(intent.value)
            OTPVerificationIntent.Resend -> argPhoneNumber?.let { sendOTPCode(it) }
        }
    }

    private fun onChangeCode(value: String) {
        // Keep only digits and cap at the code length; a single field drives the whole code.
        val sanitized = value.filter(Char::isDigit).take(OTP_CODE_LENGTH)
        _state.update { it.copy(enteredCode = sanitized) }
        proceedToVerifyOTPCode()
    }

    // phoneNumber is the full E.164 number as digits (no '+'); the data layer prefixes '+'.
    private fun sendOTPCode(phoneNumber: String) {
        // Collect the OTP event stream; restart the countdown once the code is (re-)sent.
        startCountDownTimer()
        viewModelScope.launch {
            sendOTPCodeUseCase(
                countryCode = "",
                phoneNumber = phoneNumber,
                link = purpose == OtpPurpose.SIGNUP_LINK
            ).collect { event ->
                when (event) {
                    SendOtpEvent.CodeSent -> {
                        isCodeSent = true
                        startCountDownTimer()
                        // If the user already finished typing while we were still sending,
                        // verify now instead of leaving them stuck.
                        if (pendingVerification) {
                            pendingVerification = false
                            proceedToVerifyOTPCode()
                        }
                    }

                    is SendOtpEvent.AutoVerified -> when (val result = event.result) {
                        // Instant verification (no code entry needed): forward the user on.
                        is Result.Success -> onVerificationSucceeded()
                        // Send/verification failed: drop any pending verify and clear the spinner
                        // so the user isn't stuck waiting on a code that will never arrive.
                        is Result.Error -> {
                            pendingVerification = false
                            resetCodeSlots()
                            _otpCodeVerificationResult.send(
                                OTPVerificationEvent.Error(result.error.asUiText())
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Submits the entered code for verification. No-ops while the code is incomplete; if the send
     * hasn't been confirmed yet, the code is held (spinner on) and verified automatically once
     * the confirmation arrives.
     */
    fun proceedToVerifyOTPCode() {
        // Length first, THEN the isCodeSent check: onChangeCode calls this on every keystroke,
        // and an incomplete code must never park a pending verification or show the spinner.
        val code = state.value.enteredCode
        if (code.length != OTP_CODE_LENGTH) {
            return
        }
        // Code completed before the SMS was actually dispatched: keep the input, show the
        // spinner, and verify as soon as onCodeSent arrives — don't fail and wipe the code.
        if (!isCodeSent) {
            pendingVerification = true
            _state.update { it.copy(isLoading = true) }
            return
        }
        _state.update {
            it.copy(
                isLoading = true
            )
        }
        viewModelScope.launch {
            // RESET signs in with the credential; SIGNUP_LINK links it onto the new account.
            val result = when (purpose) {
                OtpPurpose.RESET -> verifyOTPCodeUseCase(code)
                OtpPurpose.SIGNUP_LINK -> linkPhoneNumberUseCase(code)
            }
            _state.update {
                it.copy(
                    isLoading = false
                )
            }
            when (result) {
                is Result.Error -> {
                    resetCodeSlots()
                    // Reset shows a generic "wrong code"; link surfaces the specific cause
                    // (e.g. the number is already linked to another account).
                    val message = when (purpose) {
                        OtpPurpose.RESET -> UiText.StringResource(R.string.incorrect_otp_code)
                        OtpPurpose.SIGNUP_LINK -> result.error.asUiText()
                    }
                    _otpCodeVerificationResult.send(OTPVerificationEvent.Error(message))
                }

                is Result.Success -> {
                    onVerificationSucceeded()
                }
            }
        }
    }

    // Single success path for both manual and auto verification. For SIGNUP_LINK the phone was
    // just linked onto the new (authenticated) account, so persist the verified flag before
    // forwarding the user on; a failed flag write shouldn't block navigation.
    private suspend fun onVerificationSucceeded() {
        if (purpose == OtpPurpose.SIGNUP_LINK) {
            markPhoneVerifiedUseCase()
        }
        _otpCodeVerificationResult.send(OTPVerificationEvent.Success)
    }

    // Clears the entered digits (and the spinner) so the user can retype after an error,
    // without disturbing the resend countdown.
    private fun resetCodeSlots() {
        _state.update {
            it.copy(
                enteredCode = "",
                isLoading = false
            )
        }
    }
}