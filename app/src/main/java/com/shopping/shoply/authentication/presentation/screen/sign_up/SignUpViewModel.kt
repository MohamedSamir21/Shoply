package com.shopping.shoply.authentication.presentation.screen.sign_up

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.shopping.shoply.authentication.domain.model.User
import com.shopping.shoply.authentication.domain.usecase.CheckInputsFilledUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SignUpUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SignUpWithFacebookUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SignUpWithGoogleUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidateConfirmedPasswordUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidateEmailUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidatePasswordUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidatePhoneUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidateUsernameUseCase
import com.shopping.shoply.authentication.presentation.screen.sign_up.model.ActivityContext
import com.shopping.shoply.authentication.presentation.screen.sign_up.model.FBLoginManger
import com.shopping.shoply.authentication.presentation.utility.asUiText
import com.shopping.shoply.common.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SignUpViewModel"

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val checkInputsFilledUseCase: CheckInputsFilledUseCase,
    private val validateUsernameUseCase: ValidateUsernameUseCase,
    private val validateEmailUseCase: ValidateEmailUseCase,
    private val validatePhoneUseCase: ValidatePhoneUseCase,
    private val validatePasswordUseCase: ValidatePasswordUseCase,
    private val validateConfirmedPasswordUseCase: ValidateConfirmedPasswordUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val signUpWithGoogleUseCase: SignUpWithGoogleUseCase,
    private val signUpWithFacebookUseCase: SignUpWithFacebookUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(SignUpScreenState())
    val state = _state.asStateFlow()
    private val _signUpResult =
        Channel<SignUpScreenEvent.SignUp.Result>()
    val signUpResult = _signUpResult.receiveAsFlow()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }

    fun onEvent(event: SignUpScreenEvent) {
        when (event) {

            is SignUpScreenEvent.Change -> {
                onChange(event)
            }

            is SignUpScreenEvent.Clear -> {
                onClear(event)
            }

            is SignUpScreenEvent.SignUp -> {
                onSignUp(event)
            }
        }
    }

    private fun onSignUp(event: SignUpScreenEvent.SignUp) {
        when (event) {
            SignUpScreenEvent.SignUp.SubmitForm -> {
                signUp()
            }

            is SignUpScreenEvent.SignUp.Google -> {
                signUpWithGoogle(event.activityContext)
            }

            is SignUpScreenEvent.SignUp.Facebook -> {
                signUpWithFacebook(
                    event.loginManager,
                    event.callbackManager
                )
            }

            is SignUpScreenEvent.SignUp.Result -> {
                onResult(event)
            }
        }
    }

    private fun onResult(event: SignUpScreenEvent.SignUp.Result) {
        when (event) {
            is SignUpScreenEvent.SignUp.Result.Success -> Unit
            is SignUpScreenEvent.SignUp.Result.Failure -> {
                _state.value = state.value.copy(
                    signUpError = event.uiText
                )
            }
        }
    }

    private fun onClear(event: SignUpScreenEvent.Clear) {
        val currentUser = state.value.user
        when (event) {
            SignUpScreenEvent.Clear.Username -> {
                _state.value = state.value.copy(
                    user = currentUser.copy(username = "")
                )
            }

            SignUpScreenEvent.Clear.Email -> {
                _state.value = state.value.copy(
                    user = currentUser.copy(email = "")
                )
            }

            SignUpScreenEvent.Clear.PhoneNumber -> {
                _state.value = state.value.copy(
                    user = currentUser.copy(phoneNumber = "")
                )
            }

            SignUpScreenEvent.Clear.Password -> {
                _state.value = state.value.copy(
                    user = currentUser.copy(password = "")
                )
            }

            SignUpScreenEvent.Clear.ConfirmedPassword -> {
                _state.value = state.value.copy(
                    user = currentUser.copy(confirmedPassword = "")
                )
            }
        }
    }

    private fun onChange(event: SignUpScreenEvent.Change) {
        val currentUser = state.value.user

        when (event) {
            is SignUpScreenEvent.Change.Username -> {
                _state.value = state.value.copy(
                    user = currentUser.copy(username = event.username)
                )
            }

            is SignUpScreenEvent.Change.Email -> {
                _state.value = state.value.copy(
                    user = currentUser.copy(email = event.email)
                )
            }

            is SignUpScreenEvent.Change.PhoneNumber -> {
                _state.value = state.value.copy(
                    user = currentUser.copy(phoneNumber = event.phoneNumber)
                )
            }

            is SignUpScreenEvent.Change.Password -> {
                _state.value = state.value.copy(
                    user = currentUser.copy(password = event.password)
                )
            }

            is SignUpScreenEvent.Change.ConfirmedPassword -> {
                _state.value = state.value.copy(
                    user = currentUser.copy(confirmedPassword = event.confirmedPassword)
                )
            }

            is SignUpScreenEvent.Change.PasswordVisibility -> {
                _state.value = state.value.copy(
                    showPassword = event.show
                )
            }
        }
        _state.value = state.value.copy(
            enableSignUp = areInputsFilled()
        )
    }

    private fun signUpWithGoogle(activityContext: ActivityContext) {
        viewModelScope.launch(exceptionHandler) {
            when (val result = signUpWithGoogleUseCase(activityContext)) {
                is Result.Success -> {
                    _signUpResult.send(SignUpScreenEvent.SignUp.Result.Success)
                }

                is Result.Error -> {
                    _signUpResult.send(SignUpScreenEvent.SignUp.Result.Failure(result.error.asUiText()))
                }
            }
        }
    }

    private fun signUpWithFacebook(
        loginManager: LoginManager,
        callbackManager: CallbackManager
    ) {
        viewModelScope.launch {
            Log.i(TAG, "signUpWithFacebook()")
            signUpWithFacebookUseCase(
                facebookLoginManger = FBLoginManger(loginManager),
                callbackManager = callbackManager,
                onComplete = { result ->
                    viewModelScope.launch {
                        when (result) {
                            is Result.Success -> {
                                _signUpResult.send(SignUpScreenEvent.SignUp.Result.Success)
                            }

                            is Result.Error -> {
                                _signUpResult.send(SignUpScreenEvent.SignUp.Result.Failure(result.error.asUiText()))
                            }
                        }
                    }
                })
        }
    }

    private fun areInputsFilled(): Boolean {
        return state.value.let {
            checkInputsFilledUseCase(
                User(
                    username = it.user.username,
                    email = it.user.email,
                    phoneNumber = it.user.phoneNumber,
                    password = it.user.password,
                    confirmedPassword = it.user.confirmedPassword
                )
            )
        }
    }

    private fun signUp() {
        if (hasInvalidInputs()) return

        _state.value = state.value.copy(
            isSigningUp = true,
            userError = UserError(),
            signUpError = null
        )
        viewModelScope.launch {
            when (val result = signUpUseCase(state.value.user)) {
                is Result.Success -> {
                    _signUpResult.send(SignUpScreenEvent.SignUp.Result.Success)
                }

                is Result.Error -> {
                    val error = result.error
                    _signUpResult.send(SignUpScreenEvent.SignUp.Result.Failure(uiText = error.asUiText()))
                }
            }
            _state.value = state.value.copy(
                isSigningUp = false
            )
        }
    }

    private fun hasInvalidInputs(): Boolean {
        return state.value.let {
            val usernameValidation = validateUsernameUseCase(it.user.username)
            val emailValidation = validateEmailUseCase(it.user.email)
            val phoneNumberValidation = validatePhoneUseCase(it.user.phoneNumber)
            val passwordValidation = validatePasswordUseCase(it.user.password)
            val confirmedPasswordValidation =
                validateConfirmedPasswordUseCase(it.user.password, it.user.confirmedPassword)
            val validations = listOf(
                usernameValidation,
                emailValidation,
                phoneNumberValidation,
                passwordValidation,
                confirmedPasswordValidation
            )
            Log.d(TAG, passwordValidation.error.toString())
            val hasError = validations.any { validation -> validation.error != null }

            if (hasError) {
                _state.value = it.copy(
                    userError = UserError(
                        usernameError = usernameValidation.error,
                        emailError = emailValidation.error,
                        phoneNumberError = phoneNumberValidation.error,
                        passwordError = passwordValidation.error,
                        passwordConfirmedError = confirmedPasswordValidation.error
                    )
                )
            }
            hasError
        }
    }
}