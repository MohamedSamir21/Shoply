package com.shopping.shoply.authentication.presentation.signup

import com.shopping.shoply.authentication.domain.model.CountryInfo

sealed class SignUpScreenIntent {
    sealed class Country : SignUpScreenIntent() {
        data object ToggleMenu : Country()
        data class ChangeKeyword(val keyword: String) : Country()
        data class Select(val countryInfo: CountryInfo) : Country()
    }

    sealed class Change : SignUpScreenIntent() {
        data class Username(val username: String) : Change()
        data class Email(val email: String) : Change()
        data class PhoneNumber(val phoneNumber: String) : Change()
        data class Password(val password: String) : Change()
        data class ConfirmedPassword(val confirmedPassword: String) : Change()
        data class PasswordVisibility(val show: Boolean) : Change()
    }

    sealed class Clear : SignUpScreenIntent() {
        data object Username : Clear()
        data object Email : Clear()
        data object PhoneNumber : Clear()
        data object Password : Clear()
        data object ConfirmedPassword : Clear()
    }

    sealed class SignUp : SignUpScreenIntent() {
        data object SubmitForm : SignUp()
        // No SDK objects here — the data layer injects the Facebook/Google collaborators it needs.
        data object Google : SignUp()
        data object Facebook : SignUp()
    }

    data object SignIn: SignUpScreenIntent()
}