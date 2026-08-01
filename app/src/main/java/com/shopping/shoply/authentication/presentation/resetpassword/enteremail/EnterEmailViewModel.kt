package com.shopping.shoply.authentication.presentation.resetpassword.enteremail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.authentication.domain.usecase.auth.CheckIfEmailExistsUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SendPasswordResetEmailUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidateEmailUseCase
import com.shopping.shoply.authentication.presentation.utility.asUiText
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.errorOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the enter-email step of the password-reset flow: validates the address and confirms an
 * account exists for it before sending the reset email. Success is delivered as a one-shot
 * [emailVerificationResult] event; validation and lookup failures surface through [state].
 */
@HiltViewModel
class EnterEmailViewModel @Inject constructor(
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val checkIfEmailExistsUseCase: CheckIfEmailExistsUseCase,
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase
): ViewModel() {
    private val _state = MutableStateFlow(EnterEmailState())
    val state = _state.asStateFlow()
    private val _emailVerificationResult = Channel<EnterEmailEvent.Success>()
    val emailVerificationResult = _emailVerificationResult.receiveAsFlow()

    fun onChangeEmail(value: String) {
        _state.update {
            it.copy(
                email = value
            )
        }
    }

    fun onNext() {
        val validationError = validateEmailUseCase(state.value.email).errorOrNull()
        if (validationError != null) {
            _state.update {
                it.copy(
                    error = validationError.asUiText()
                )
            }
            return
        }

        // Check if email exists
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isProcessing = true,
                    error = null
                )
            }

            when (val result = checkIfEmailExistsUseCase(state.value.email)) {
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            error = result.error.asUiText(),
                            isProcessing = false
                        )
                    }
                }
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            error = null
                        )
                    }
                    sendPasswordResetEmail()
                }
            }
        }
    }

    private fun sendPasswordResetEmail() {
        viewModelScope.launch {
            when (val result = sendPasswordResetEmailUseCase(state.value.email)) {
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            error = result.error.asUiText(),
                            isProcessing = false
                        )
                    }
                }
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            isProcessing = false
                        )
                    }
                    _emailVerificationResult.send(EnterEmailEvent.Success)
                }
            }
        }
    }

}