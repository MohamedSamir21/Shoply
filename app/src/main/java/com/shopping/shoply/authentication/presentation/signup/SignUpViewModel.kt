package com.shopping.shoply.authentication.presentation.signup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shopping.shoply.R
import com.shopping.shoply.authentication.domain.model.User
import com.shopping.shoply.authentication.domain.usecase.auth.CheckIfPhoneNumberExistsUseCase
import com.shopping.shoply.authentication.presentation.utility.UiText
import com.shopping.shoply.authentication.domain.usecase.validate.CheckInputsFilledUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SignUpUseCase
import com.shopping.shoply.authentication.domain.usecase.countryinfo.GetCountryInfoUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SignUpWithFacebookUseCase
import com.shopping.shoply.authentication.domain.usecase.auth.SignUpWithGoogleUseCase
import com.shopping.shoply.authentication.domain.model.SignUpData
import com.shopping.shoply.authentication.domain.usecase.validate.MeasurePasswordStrengthUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidateConfirmedPasswordUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidateEmailUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidatePasswordUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidatePhoneUseCase
import com.shopping.shoply.authentication.domain.usecase.validate.ValidateUsernameUseCase
import com.shopping.shoply.authentication.presentation.utility.PasswordStrengthDebouncer
import com.shopping.shoply.authentication.presentation.utility.asUiText
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.errorOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

private const val TAG = "SignUpViewModel"

/**
 * Backs the sign-up screen: per-field validation, debounced password-strength feedback, and
 * account creation via email/password, Google, or Facebook, with outcomes delivered as one-shot
 * [signUpResult] events. Country data for the phone field loads eagerly in `init`, so constructing
 * this ViewModel already touches [viewModelScope].
 */
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
    private val signUpWithFacebookUseCase: SignUpWithFacebookUseCase,
    private val getCountryInfoUseCase: GetCountryInfoUseCase,
    private val checkIfPhoneNumberExistsUseCase: CheckIfPhoneNumberExistsUseCase,
    private val measurePasswordStrengthUseCase: MeasurePasswordStrengthUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(SignUpScreenState())
    val state = _state.asStateFlow()

    private val passwordStrengthDebouncer by lazy {
        PasswordStrengthDebouncer(
            scope = viewModelScope,
            measureStrength = { password -> measurePasswordStrengthUseCase(password) }
        ) { strength -> _state.value = state.value.copy(passwordStrength = strength) }
    }

    init {
        loadCountriesInfo()
    }

    private val _signUpResult =
        Channel<SignUpScreenEvent.SignUpResult>()
    val signUpResult = _signUpResult.receiveAsFlow()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
    }

    fun onIntent(intent: SignUpScreenIntent) {
        when (intent) {

            is SignUpScreenIntent.Country -> {
                onCountry(intent)
            }

            is SignUpScreenIntent.Change -> {
                onChange(intent)
            }

            is SignUpScreenIntent.Clear -> {
                onClear(intent)
            }

            is SignUpScreenIntent.SignUp -> {
                onSignUp(intent)
            }

            else -> Unit
        }
    }

    private fun onSignUp(event: SignUpScreenIntent.SignUp) {
        when (event) {
            SignUpScreenIntent.SignUp.SubmitForm -> {
                signUp()
            }

            SignUpScreenIntent.SignUp.Google -> {
                signUpWithGoogle()
            }

            is SignUpScreenIntent.SignUp.Facebook -> {
                signUpWithFacebook()
            }
        }
    }

    /**
     * Called by the screen when it observes a one-shot [signUpResult] failure: stores the message
     * back into [state] so the error survives recomposition until the user dismisses it.
     */
    fun onEvent(event: SignUpScreenEvent.SignUpResult.Failure) {
        _state.value = state.value.copy(
            signUpError = event.uiText
        )
    }

    private fun onCountry(intent: SignUpScreenIntent.Country) {
        when (intent) {
            SignUpScreenIntent.Country.ToggleMenu -> {
                _state.value = state.value.copy(
                    showCountryInfoMenu = !state.value.showCountryInfoMenu
                )
            }

            is SignUpScreenIntent.Country.ChangeKeyword -> {
                _state.value = state.value.copy(countryKeyword = intent.keyword)
                searchForCountry()
            }

            is SignUpScreenIntent.Country.Select -> {
                _state.value = state.value.copy(
                    selectedCountryInfo = intent.countryInfo,
                    showCountryInfoMenu = false
                )
            }
        }
    }

    private fun loadCountriesInfo() {
        viewModelScope.launch {
            val countries = getCountryInfoUseCase()
            if (countries.isNotEmpty()) {
                _state.value = state.value.copy(
                    selectedCountryInfo = countries.first(),
                    countriesInfo = countries,
                    countriesInfoByKeyword = countries,
                    isCountryCodeSearchButtonClickable = countries.size > 1
                )
            }
        }
    }

    private fun searchForCountry() {
        val keyword = state.value.countryKeyword.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.US) else it.toString()
        }
        _state.value = state.value.copy(
            countriesInfoByKeyword = if (keyword.isBlank()) {
                state.value.countriesInfo
            } else {
                state.value.countriesInfo.filter { it.name.startsWith(keyword) }
            }
        )
    }

    private fun onClear(event: SignUpScreenIntent.Clear) {
        val currentForm = state.value.form
        when (event) {
            SignUpScreenIntent.Clear.Username -> {
                _state.value = state.value.copy(
                    form = currentForm.copy(username = "")
                )
            }

            SignUpScreenIntent.Clear.Email -> {
                _state.value = state.value.copy(
                    form = currentForm.copy(email = "")
                )
            }

            SignUpScreenIntent.Clear.PhoneNumber -> {
                _state.value = state.value.copy(
                    form = currentForm.copy(phoneNumber = "")
                )
            }

            SignUpScreenIntent.Clear.Password -> {
                _state.value = state.value.copy(
                    form = currentForm.copy(password = "")
                )
                passwordStrengthDebouncer.measure("")
            }

            SignUpScreenIntent.Clear.ConfirmedPassword -> {
                _state.value = state.value.copy(
                    form = currentForm.copy(confirmedPassword = "")
                )
            }
        }
    }

    private fun onChange(event: SignUpScreenIntent.Change) {
        val currentForm = state.value.form

        when (event) {
            is SignUpScreenIntent.Change.Username -> {
                _state.value = state.value.copy(
                    form = currentForm.copy(username = event.username)
                )
            }

            is SignUpScreenIntent.Change.Email -> {
                _state.value = state.value.copy(
                    form = currentForm.copy(email = event.email)
                )
            }

            is SignUpScreenIntent.Change.PhoneNumber -> {
                _state.value = state.value.copy(
                    form = currentForm.copy(phoneNumber = event.phoneNumber)
                )
            }

            is SignUpScreenIntent.Change.Password -> {
                _state.value = state.value.copy(
                    form = currentForm.copy(password = event.password)
                )
                passwordStrengthDebouncer.measure(event.password)
            }

            is SignUpScreenIntent.Change.ConfirmedPassword -> {
                _state.value = state.value.copy(
                    form = currentForm.copy(confirmedPassword = event.confirmedPassword)
                )
            }

            is SignUpScreenIntent.Change.PasswordVisibility -> {
                _state.value = state.value.copy(
                    showPassword = !event.show
                )
            }
        }
        _state.value = state.value.copy(
            enableSignUp = areInputsFilled()
        )
    }

    private fun signUpWithGoogle() {
        viewModelScope.launch(exceptionHandler) {
            when (val result = signUpWithGoogleUseCase()) {
                is Result.Success -> {
                    _signUpResult.send(SignUpScreenEvent.SignUpResult.Success.Verified)
                }

                is Result.Error -> {
                    _signUpResult.send(SignUpScreenEvent.SignUpResult.Failure(result.error.asUiText()))
                }
            }
        }
    }

    private fun signUpWithFacebook() {
        viewModelScope.launch {
            when (val result = signUpWithFacebookUseCase()) {
                is Result.Success -> {
                    _signUpResult.send(SignUpScreenEvent.SignUpResult.Success.Verified)
                }

                is Result.Error -> {
                    _signUpResult.send(SignUpScreenEvent.SignUpResult.Failure(result.error.asUiText()))
                }
            }
        }
    }

    private fun areInputsFilled(): Boolean {
        return state.value.let {
            checkInputsFilledUseCase(
                SignUpData(
                    username = it.form.username,
                    email = it.form.email,
                    phoneNumber = it.form.phoneNumber,
                    password = it.form.password,
                    confirmedPassword = it.form.confirmedPassword
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
            val form = state.value.form
            // Map the UI form -> domain entity (+ separate password) at the boundary.
            // Combine the picked calling code with the national number into E.164 for storage.
            // CountryInfo.code already carries a '+', so normalize to a single leading '+'.
            val e164PhoneNumber =
                "+" + (state.value.selectedCountryInfo.codeNumber + form.phoneNumber).filter(Char::isDigit)

            // One phone = one account. Reject up front if it's already registered, so we don't
            // create an account that then fails to link the phone (credential-already-in-use).
            if (checkIfPhoneNumberExistsUseCase(e164PhoneNumber) is Result.Success) {
                _state.value = state.value.copy(
                    isSigningUp = false,
                    userError = UserError(
                        phoneNumberError = UiText.StringResource(R.string.phone_already_registered)
                    )
                )
                return@launch
            }

            val user = User(
                username = form.username,
                email = form.email,
                phoneNumber = e164PhoneNumber
            )
            when (val result = signUpUseCase(user, form.password)) {
                is Result.Success -> {
                    _signUpResult.send(SignUpScreenEvent.SignUpResult.Success.NotVerified)
                }

                is Result.Error -> {
                    val error = result.error
                    _signUpResult.send(SignUpScreenEvent.SignUpResult.Failure(uiText = error.asUiText()))
                }
            }
            _state.value = state.value.copy(
                isSigningUp = false
            )
        }
    }

    private fun hasInvalidInputs(): Boolean {
        return state.value.let {
            val usernameValidation = validateUsernameUseCase(it.form.username)
            val emailValidation = validateEmailUseCase(it.form.email)
            // Validate the E.164 form (calling code + national number) — matching how the number is
            // stored and what LibphonenumberPhoneValidator expects. CountryInfo.code already carries a '+'.
            val e164PhoneNumber =
                "+" + (it.selectedCountryInfo.codeNumber + it.form.phoneNumber).filter(Char::isDigit)
            val phoneNumberValidation = validatePhoneUseCase(e164PhoneNumber)
            val passwordValidation = validatePasswordUseCase(it.form.password)
            val confirmedPasswordValidation =
                validateConfirmedPasswordUseCase(it.form.password, it.form.confirmedPassword)
            val validations = listOf(
                usernameValidation,
                emailValidation,
                phoneNumberValidation,
                passwordValidation,
                confirmedPasswordValidation
            )
            Log.d(TAG, passwordValidation.errorOrNull().toString())
            val hasError = validations.any { validation -> validation.errorOrNull() != null }

            if (hasError) {
                _state.value = it.copy(
                    userError = UserError(
                        usernameError = usernameValidation.errorOrNull()?.asUiText(),
                        emailError = emailValidation.errorOrNull()?.asUiText(),
                        phoneNumberError = phoneNumberValidation.errorOrNull()?.asUiText(),
                        passwordError = passwordValidation.errorOrNull()?.asUiText(),
                        passwordConfirmedError = confirmedPasswordValidation.errorOrNull()
                            ?.asUiText()
                    )
                )
            }
            hasError
        }
    }
}