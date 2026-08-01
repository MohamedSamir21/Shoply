package com.shopping.shoply.authentication.presentation.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.authentication.domain.model.SignInData
import com.shopping.shoply.authentication.domain.usecase.auth.SignInUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SignUpWithFacebookUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SignUpWithGoogleUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.CheckInputsFilledUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidateEmailUseCase
import com.shopping.shoply.authentication.presentation.utility.asUiText
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.errorOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * Backs the sign-in screen: validates the form, then signs in with email/password or continues
 * with Google/Facebook. Outcomes are delivered as one-shot [signInResult] events; field-level
 * validation errors live in [state].
 */
@HiltViewModel
class SignInViewModel @Inject constructor(
    private val continueWithFacebookUseCase: SignUpWithFacebookUseCase,
    private val continueWithGoogleUseCase: SignUpWithGoogleUseCase,
    private val checkInputsFilledUseCase: CheckInputsFilledUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val signInUseCase: SignInUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(SignInScreenState())
    val state = _state.asStateFlow()
    private val _signInResult = Channel<SignInScreenEvent.SignInResult>()
    val signInResult = _signInResult.receiveAsFlow()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }

    fun onIntent(intent: SignInScreenIntent) {
        when (intent) {
            is SignInScreenIntent.ContinueWithFacebook -> continueWithFacebook()

            SignInScreenIntent.ContinueWithGoogle -> continueWithGoogle()
            SignInScreenIntent.SignIn -> signIn()
            is SignInScreenIntent.Change -> {
                onChange(intent)
            }

            is SignInScreenIntent.Clear -> {
                onClear(intent)
            }
        }
    }

    /**
     * Called by the screen when it observes a one-shot [signInResult] failure: stores the message
     * back into [state] so the error survives recomposition until the user dismisses it.
     */
    fun onEvent(event: SignInScreenEvent.SignInResult.Failure) {
        _state.update {
            it.copy(
                signInError = event.uiText
            )
        }
    }

    private fun onClear(intent: SignInScreenIntent.Clear) {
        when (intent) {
            SignInScreenIntent.Clear.Email -> clearEmail()
            SignInScreenIntent.Clear.Password -> clearPassword()
        }
    }

    private fun clearPassword() {
        _state.update {
            it.copy(
                password = ""
            )
        }
    }

    private fun clearEmail() {
        _state.update {
            it.copy(
                email = ""
            )
        }
    }

    private fun onChange(intent: SignInScreenIntent.Change) {
        when (intent) {
            is SignInScreenIntent.Change.Email -> changeEmail(intent)
            is SignInScreenIntent.Change.Password -> changePassword(intent)
            is SignInScreenIntent.Change.PasswordVisibility -> changePasswordVisibility()
        }
    }

    private fun areInputsFilled(): Boolean {
        return checkInputsFilledUseCase(
            SignInData(
                email = state.value.email,
                password = state.value.password
            )
        )
    }

    private fun changePasswordVisibility() {
        _state.update {
            it.copy(
                showPassword = !state.value.showPassword
            )
        }
    }

    private fun changePassword(intent: SignInScreenIntent.Change.Password) {
        _state.update {
            it.copy(
                password = intent.password,
                enableSignIn = areInputsFilled()
            )
        }
    }

    private fun changeEmail(intent: SignInScreenIntent.Change.Email) {
        _state.update {
            it.copy(
                email = intent.email,
                enableSignIn = areInputsFilled()
            )
        }
    }

    private fun signIn() {
        if (hasInvalidEmail()) return

        _state.update {
            it.copy(
                isSigningIn = true,
                signInError = null
            )
        }
        viewModelScope.launch {
            val result = signInUseCase(
                email = state.value.email,
                password = state.value.password
            )
            when (result) {
                is Result.Success -> {
                    _signInResult.send(SignInScreenEvent.SignInResult.Success)
                }

                is Result.Error -> {
                    _signInResult.send(SignInScreenEvent.SignInResult.Failure(uiText = result.error.asUiText()))
                }
            }

            _state.update {
                it.copy(
                    isSigningIn = false
                )
            }
        }
    }

    private fun hasInvalidEmail(): Boolean {
        val validationResult = validateEmailUseCase(state.value.email)
        val hasError = validationResult.errorOrNull() != null
        if (hasError) {
            _state.update {
                it.copy(
                    emailValidationError = validationResult.errorOrNull()?.asUiText()
                )
            }
        }
        return hasError
    }

    private fun continueWithGoogle() {
        viewModelScope.launch(exceptionHandler) {
            when (val result = continueWithGoogleUseCase()) {
                is Result.Success -> {
                    _signInResult.send(SignInScreenEvent.SignInResult.Success)
                }

                is Result.Error -> {
                    _signInResult.send(SignInScreenEvent.SignInResult.Failure(result.error.asUiText()))
                }
            }
        }
    }

    private fun continueWithFacebook() {
        viewModelScope.launch {
            when (val result = continueWithFacebookUseCase()) {
                is Result.Success -> {
                    _signInResult.send(SignInScreenEvent.SignInResult.Success)
                }

                is Result.Error -> {
                    _signInResult.send(
                        SignInScreenEvent.SignInResult.Failure(
                            uiText = result.error.asUiText()
                        )
                    )
                }
            }
        }
    }
}