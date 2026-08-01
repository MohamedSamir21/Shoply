package com.shopping.shoply.authentication.presentation.signup

import com.shopping.shoply.authentication.domain.model.CountryInfo
import com.shopping.shoply.authentication.domain.model.PasswordStrength
import com.shopping.shoply.authentication.presentation.utility.UiText

data class SignUpScreenState(
    val form: SignUpForm = SignUpForm(),
    val userError: UserError = UserError(),
    val showPassword: Boolean = false,
    // Live strength feedback for the meter under the password field; NONE renders nothing.
    val passwordStrength: PasswordStrength = PasswordStrength.NONE,
    val enableSignUp: Boolean = false,
    val isSigningUp: Boolean = false,
    val signUpError: UiText? = null,
    // Country calling-code selection, combined with the national number into an E.164 phone.
    val selectedCountryInfo: CountryInfo = CountryInfo("", "", "", ""),
    val countriesInfo: List<CountryInfo> = emptyList(),
    val countriesInfoByKeyword: List<CountryInfo> = emptyList(),
    val countryKeyword: String = "",
    val showCountryInfoMenu: Boolean = false,
    val isCountryCodeSearchButtonClickable: Boolean = true
)

// Errors held as UiText (resolved to a String by the screen), so the ViewModel never
// hardcodes user-facing text.
data class UserError(
    val usernameError: UiText? = null,
    val emailError: UiText? = null,
    val phoneNumberError: UiText? = null,
    val passwordError: UiText? = null,
    val passwordConfirmedError: UiText? = null
)