package com.shopping.shoply.authentication.presentation.resetpassword.sendresetlink

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.usecase.auth.SendPasswordResetEmailUseCase
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
 * Backs the "reset link sent" screen: runs the resend countdown and re-sends the password-reset
 * email on demand. [state] is shared with [SharingStarted.Lazily], so the countdown starts on
 * first collection; the target email arrives afterwards via [SendResetLinkIntent.OnFirstPopUp].
 */
@HiltViewModel
class SendResetLinkViewModel @Inject constructor(
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
    private val formatCountDownTimeUseCase: FormatCountDownTimeUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(SendResetLinkState())
    // Only arm the countdown on start: the email is always null until the pop-up delivers it
    // via OnFirstPopUp, so there is nothing to send yet.
    val state = _state.onStart {
        startCountDownTimer()
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        SendResetLinkState()
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

    fun onIntent(intent: SendResetLinkIntent) {
        when (intent) {
            SendResetLinkIntent.Resend -> {
                sendPasswordResetEmail()
            }

            is SendResetLinkIntent.OnFirstPopUp -> {
                saveEmailInState(intent.email)
            }
        }
    }

    private fun saveEmailInState(email: String) {
        _state.update {
            it.copy(
                email = email
            )
        }
    }

    private fun sendPasswordResetEmail() {
        startCountDownTimer()

        val email = state.value.email
        viewModelScope.launch {
            email?.let { email ->
                when (val result = sendPasswordResetEmailUseCase(email)) {
                    is Result.Error -> {
                        _state.update {
                            it.copy(
                                sendEmailLinkError = result.error.asUiText()
                            )
                        }
                    }

                    is Result.Success -> {
                        _state.update {
                            it.copy(
                                sendEmailLinkSuccessMessage = UiText.StringResource(
                                    id = R.string.new_email_password_reset_link
                                )
                            )
                        }
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
}