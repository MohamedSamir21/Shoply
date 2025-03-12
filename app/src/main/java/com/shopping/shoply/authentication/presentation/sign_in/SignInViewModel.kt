package com.shopping.shoply.authentication.presentation.sign_in

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.shopping.shoply.authentication.domain.model.User
import com.shopping.shoply.authentication.domain.usecase.validate.CheckInputsFilledUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SignInUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SignUpWithFacebookUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SignUpWithGoogleUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidateEmailUseCase
import com.shopping.shoply.authentication.presentation.model.ActivityContext
import com.shopping.shoply.authentication.presentation.model.FBLoginManger
import com.shopping.shoply.authentication.presentation.utility.asUiText
import com.shopping.shoply.common.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SignInViewModel"

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
            is SignInScreenIntent.ContinueWithFacebook -> continueWithFacebook(
                loginManager = intent.loginManager,
                callbackManager = intent.callbackManager
            )

            is SignInScreenIntent.ContinueWithGoogle -> continueWithGoogle(intent.context)
            SignInScreenIntent.SignIn -> signIn()
            is SignInScreenIntent.Change -> {
                onChange(intent)
            }

            is SignInScreenIntent.Clear -> {
                onClear(intent)
            }
        }
    }

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
            is SignInScreenIntent.Change.PasswordVisibility -> changePasswordVisibility(intent)
        }
        _state.update {
            it.copy(
                enableSignIn = areInputsFilled()
            )
        }
    }

    private fun areInputsFilled(): Boolean {
        return checkInputsFilledUseCase(
            email = state.value.email,
            password = state.value.password
        )
    }

    private fun changePasswordVisibility(intent: SignInScreenIntent.Change.PasswordVisibility) {
        Log.d(TAG, "Before: " + state.value.showPassword.toString())
        _state.update {
            it.copy(
                showPassword = !intent.show
            )
        }
        Log.d(TAG, "After: " + state.value.showPassword.toString())
    }

    private fun changePassword(intent: SignInScreenIntent.Change.Password) {
        _state.update {
            it.copy(
                password = intent.password
            )
        }
    }

    private fun changeEmail(intent: SignInScreenIntent.Change.Email) {
        _state.update {
            it.copy(
                email = intent.email
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
        Log.d(TAG, "isSigningIn: ${_state.value.isSigningIn}")
        viewModelScope.launch {
            val result = signInUseCase(
                User(
                    email = state.value.email,
                    password = state.value.password
                )
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
        val hasError = validationResult.error != null
        Log.d(TAG, "hasError: $hasError")
        if (hasError) {
            _state.update {
                it.copy(
                    emailValidationError = validationResult.error
                )
            }
        }
        Log.d(TAG, "email error : ${_state.value.emailValidationError}")
        return hasError
    }

    private fun continueWithGoogle(context: Context) {
        viewModelScope.launch(exceptionHandler) {
            when (val result = continueWithGoogleUseCase(ActivityContext(context))) {
                is Result.Success -> {
                    _signInResult.send(SignInScreenEvent.SignInResult.Success)
                }

                is Result.Error -> {
                    _signInResult.send(SignInScreenEvent.SignInResult.Failure(result.error.asUiText()))
                }
            }
        }
    }

    private fun continueWithFacebook(
        loginManager: LoginManager,
        callbackManager: CallbackManager
    ) {
        viewModelScope.launch {
            continueWithFacebookUseCase(
                facebookLoginManger = FBLoginManger(loginManager),
                callbackManager = callbackManager,
                onComplete = { result ->
                    when (result) {
                        is Result.Success -> {
                            viewModelScope.launch {
                                _signInResult.send(SignInScreenEvent.SignInResult.Success)
                            }
                        }

                        is Result.Error -> {
                            viewModelScope.launch {
                                _signInResult.send(
                                    SignInScreenEvent.SignInResult.Failure(
                                        uiText = result.error.asUiText()
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