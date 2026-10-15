package com.shopping.shoply.authentication.presentation.verifyaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.usecase.auth.GetUserInfoUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.RefreshVerificationStatusUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.ResendVerificationEmailUseCase
import com.shopping.shoply.authentication.domain.usecase.countdown.FormatCountDownTimeUseCase
import com.shopping.shoply.authentication.presentation.utility.CountdownTicker
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.authentication.presentation.utility.asUiText
import com.shopping.shoply.common.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the verify-email screen: loads the signed-in user's email, runs the resend countdown, and
 * re-checks the verification status — loudly on the user's request or silently when the screen
 * resumes. [state] is shared with [SharingStarted.Lazily], so the email load and countdown start
 * on first collection rather than at construction.
 */
@HiltViewModel
class VerifyAccountViewModel @Inject constructor(
    private val resendVerificationEmailUseCase: ResendVerificationEmailUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val refreshVerificationStatusUseCase: RefreshVerificationStatusUseCase,
    private val formatCountDownTimeUseCase: FormatCountDownTimeUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(VerifyAccountState())
    val state = _state.onStart {
        startCountDownTimer()
        loadEmail()
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        VerifyAccountState()
    )

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

    fun onIntent(intent: VerifyAccountIntent) {
        when (intent) {
            VerifyAccountIntent.Resend -> {
                resendVerification()
            }

            VerifyAccountIntent.ConfirmVerified -> {
                checkVerification(showMessageIfUnverified = true)
            }
        }
    }

    /**
     * Silent re-check used when the screen resumes (e.g. the user returns from the browser after
     * clicking the verification link). Shows no message if the email is still unverified.
     */
    fun refreshVerificationSilently() {
        checkVerification(showMessageIfUnverified = false)
    }

    private fun checkVerification(showMessageIfUnverified: Boolean) {
        if (_state.value.isCheckingVerification) return
        _state.update { it.copy(isCheckingVerification = true) }

        viewModelScope.launch {
            when (val result = refreshVerificationStatusUseCase()) {
                is Result.Success -> {
                    if (result.data) {
                        _state.update {
                            it.copy(isCheckingVerification = false, isEmailVerified = true)
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isCheckingVerification = false,
                                verificationError = if (showMessageIfUnverified) {
                                    UiText.StringResource(id = R.string.email_not_verified_yet)
                                } else {
                                    it.verificationError
                                }
                            )
                        }
                    }
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isCheckingVerification = false,
                            verificationError = if (showMessageIfUnverified) {
                                result.error.asUiText()
                            } else {
                                it.verificationError
                            }
                        )
                    }
                }
            }
        }
    }

    private fun loadEmail() {
        viewModelScope.launch {
            when (val result = getUserInfoUseCase()) {
                is Result.Success -> _state.update { it.copy(email = result.data.email) }
                is Result.Error -> Unit
            }
        }
    }

    private fun resendVerification() {
        startCountDownTimer()

        viewModelScope.launch {
            when (val result = resendVerificationEmailUseCase()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            sendEmailLinkSuccessMessage = UiText.StringResource(
                                id = R.string.new_email_link_verification
                            )
                        )
                    }
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(
                            sendEmailLinkError = result.error.asUiText()
                        )
                    }
                }
            }
        }
    }

    private fun startCountDownTimer() {
        _state.update {
            it.copy(
                countDownFinished = false
            )
        }

        countdownTicker.start()
    }

    fun resetEmailLinkSuccessMessage() {
        _state.update {
            it.copy(
                sendEmailLinkSuccessMessage = null
            )
        }
    }

    fun resetEmailLinkErrorMessage() {
        _state.update {
            it.copy(
                sendEmailLinkError = null
            )
        }
    }

    fun resetVerificationError() {
        _state.update {
            it.copy(
                verificationError = null
            )
        }
    }
}