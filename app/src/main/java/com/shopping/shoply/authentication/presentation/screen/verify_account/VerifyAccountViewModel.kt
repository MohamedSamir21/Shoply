package com.shopping.shoply.authentication.presentation.screen.verify_account

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.usecase.ResendVerificationEmailUseCase
import com.shopping.shoply.authentication.domain.usecase.count_down.FormatCountDownTimeUseCase
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

@HiltViewModel
class VerifyAccountViewModel @Inject constructor(
    private val resendVerificationEmailUseCase: ResendVerificationEmailUseCase,
    private val formatCountDownTimeUseCase: FormatCountDownTimeUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(VerifyAccountState())
    val state = _state.onStart {
        startCountDownTimer()
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        VerifyAccountState()
    )

    private var countDownTimer: CountDownTimer? = object : CountDownTimer(60000L, 1000L) {
        override fun onTick(millisUntilFinished: Long) {
            _state.update {
                it.copy(
                    countDownTime = formatCountDownTimeUseCase(millisUntilFinished)
                )
            }
        }

        override fun onFinish() {
            _state.update {
                it.copy(
                    countDownFinished = true
                )
            }
        }
    }

    fun onIntent(intent: VerifyAccountIntent) {
        when (intent) {
            VerifyAccountIntent.Resend -> {
                resendVerification()
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

        countDownTimer?.start()
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

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }
}