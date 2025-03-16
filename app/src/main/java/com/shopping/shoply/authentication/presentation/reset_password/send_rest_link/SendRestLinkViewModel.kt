package com.shopping.shoply.authentication.presentation.reset_password.send_rest_link

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.usecase.auth.SendPasswordRestEmailUseCase
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

const val TAG = "SendRestLinkViewModel"

@HiltViewModel
class SendRestLinkViewModel @Inject constructor(
    private val sendPasswordRestEmailUseCase: SendPasswordRestEmailUseCase,
    private val formatCountDownTimeUseCase: FormatCountDownTimeUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(SendRestLinkState())
    val state = _state.onStart {
        startCountDownTimer()
    }.stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        SendRestLinkState()
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

    fun onIntent(intent: SendRestLinkIntent) {
        when (intent) {
            SendRestLinkIntent.Resend -> {
                sendPasswordRestEmail()
            }

            is SendRestLinkIntent.OnFirstPopUp -> {
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

    private fun sendPasswordRestEmail() {
        startCountDownTimer()

        val email = state.value.email
        Log.d(TAG, email.toString())
        viewModelScope.launch {
            email?.let { email ->
                sendPasswordRestEmailUseCase(
                    email = email,
                    onComplete = { result ->
                        when (result) {
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
                )
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