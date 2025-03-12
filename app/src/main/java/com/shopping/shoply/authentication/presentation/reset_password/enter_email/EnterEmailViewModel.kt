package com.shopping.shoply.authentication.presentation.reset_password.enter_email

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.authentication.domain.usecase.auth.CheckIfEmailExistsUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SendPasswordRestEmailUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidateEmailUseCase
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.authentication.presentation.utility.asUiText
import com.shopping.shoply.common.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnterEmailViewModel @Inject constructor(
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val checkIfEmailExistsUseCase: CheckIfEmailExistsUseCase,
    private val sendPasswordRestEmailUseCase: SendPasswordRestEmailUseCase
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
        val validationResult = validateEmailUseCase(state.value.email)
        if (!validationResult.error.isNullOrEmpty()) {
            _state.update {
                it.copy(
                    error = UiText.DynamicString(validationResult.error)
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

            checkIfEmailExistsUseCase(
                email = state.value.email,
                onComplete = { result ->
                    when(result) {
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
                            sendPasswordRestEmail()
                        }
                    }
                }
            )
        }
    }

    private fun sendPasswordRestEmail() {
        viewModelScope.launch {
            sendPasswordRestEmailUseCase(
                email = state.value.email,
                onComplete = { result ->
                    when(result) {
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
                            viewModelScope.launch {
                                _emailVerificationResult.send(EnterEmailEvent.Success)
                            }
                        }
                    }
                }
            )
        }
    }

}