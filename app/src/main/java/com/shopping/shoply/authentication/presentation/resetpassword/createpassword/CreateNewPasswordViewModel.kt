package com.shopping.shoply.authentication.presentation.resetpassword.createpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.authentication.domain.usecase.auth.UpdatePasswordUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.MeasurePasswordStrengthUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidateConfirmedPasswordUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidatePasswordUseCase
import com.shopping.shoply.authentication.presentation.utility.PasswordStrengthDebouncer
import com.shopping.shoply.authentication.presentation.utility.asUiText
import com.shopping.shoply.common.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the create-new-password step at the end of the reset flow: validates the password pair,
 * shows debounced strength feedback while the user types, and updates the account password,
 * flagging [CreateNewPasswordState.resetSucceeded] on success.
 */
@HiltViewModel
class CreateNewPasswordViewModel @Inject constructor(
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val validateConfirmedPasswordUseCase: ValidateConfirmedPasswordUseCase,
    private val measurePasswordStrengthUseCase: MeasurePasswordStrengthUseCase,
    private val updatePasswordUseCase: UpdatePasswordUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(CreateNewPasswordState())
    val state = _state.asStateFlow()

    // Lazy: viewModelScope must not be touched at construction time, since this ViewModel's
    // tests use a class-level `sut` field — touching it eagerly would resolve Dispatchers.Main
    // before MainDispatcherExtension.beforeEach() gets a chance to set it.
    private val passwordStrengthDebouncer by lazy {
        PasswordStrengthDebouncer(
            scope = viewModelScope,
            measureStrength = { password -> measurePasswordStrengthUseCase(password) }
        ) { strength -> _state.update { it.copy(passwordStrength = strength) } }
    }

    fun onIntent(intent: CreateNewPasswordIntent) {
        when (intent) {
            is CreateNewPasswordIntent.ChangePassword -> {
                _state.update { it.copy(password = intent.password, passwordError = null) }
                passwordStrengthDebouncer.measure(intent.password)
            }

            is CreateNewPasswordIntent.ChangeConfirmedPassword -> {
                _state.update {
                    it.copy(
                        confirmedPassword = intent.confirmedPassword,
                        confirmedPasswordError = null
                    )
                }
            }

            is CreateNewPasswordIntent.ChangePasswordVisibility -> {
                _state.update { it.copy(showPassword = !intent.isVisible) }
            }

            CreateNewPasswordIntent.Submit -> submit()
        }
    }

    private fun submit() {
        val password = state.value.password
        val confirmedPassword = state.value.confirmedPassword

        val passwordResult = validatePasswordUseCase(password)
        val confirmedResult = validateConfirmedPasswordUseCase(password, confirmedPassword)

        val passwordError = (passwordResult as? Result.Error)?.error?.asUiText()
        val confirmedError = (confirmedResult as? Result.Error)?.error?.asUiText()

        if (passwordError != null || confirmedError != null) {
            _state.update {
                it.copy(
                    passwordError = passwordError,
                    confirmedPasswordError = confirmedError
                )
            }
            return
        }

        updatePassword(password)
    }

    private fun updatePassword(newPassword: String) {
        _state.update { it.copy(isProcessing = true, error = null) }
        viewModelScope.launch {
            when (val result = updatePasswordUseCase(newPassword)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(isProcessing = false, resetSucceeded = true)
                    }
                }

                is Result.Error -> {
                    _state.update {
                        it.copy(isProcessing = false, error = result.error.asUiText())
                    }
                }
            }
        }
    }

    fun resetError() {
        _state.update { it.copy(error = null) }
    }
}
