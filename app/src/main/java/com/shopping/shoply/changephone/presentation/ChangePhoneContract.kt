package com.shopping.shoply.changephone.presentation

import com.shopping.shoply.authentication.domain.model.CountryInfo
import com.shopping.shoply.authentication.presentation.utility.UiText

enum class ChangePhoneStep { ENTER, CODE, UPDATED }

/**
 * [currentPhone] is null when the user is adding their first number. [sentTo] is the E.164 number
 * the code went to; [needsPassword] is true for accounts that sign in with a password (FR-045).
 */
data class ChangePhoneState(
    val step: ChangePhoneStep = ChangePhoneStep.ENTER,
    val currentPhone: String? = null,
    val needsPassword: Boolean = false,
    val countries: List<CountryInfo> = emptyList(),
    val shownCountries: List<CountryInfo> = emptyList(),
    val selectedCountry: CountryInfo? = null,
    val countryKeyword: String = "",
    val showCountryMenu: Boolean = false,
    val number: String = "",
    val password: String = "",
    val code: String = "",
    val numberError: UiText? = null,
    val passwordError: UiText? = null,
    val codeError: UiText? = null,
    val isBusy: Boolean = false,
    val resendSecondsLeft: Int = 0,
    val sentTo: String = ""
) {
    val isAdding: Boolean get() = currentPhone == null

    // Data class toString would print the password, number and code into logs or crash reports.
    override fun toString() = "ChangePhoneState(step=$step, isBusy=$isBusy)"
}

sealed interface ChangePhoneIntent {
    data class ChangeNumber(val number: String) : ChangePhoneIntent {
        override fun toString() = "ChangeNumber"
    }

    data class ChangePassword(val password: String) : ChangePhoneIntent {
        override fun toString() = "ChangePassword"
    }

    data class ChangeCode(val code: String) : ChangePhoneIntent {
        override fun toString() = "ChangeCode"
    }

    data object ToggleCountryMenu : ChangePhoneIntent
    data class SearchCountry(val keyword: String) : ChangePhoneIntent
    data class SelectCountry(val country: CountryInfo) : ChangePhoneIntent
    data object SendCode : ChangePhoneIntent
    data object Verify : ChangePhoneIntent
    data object ResendCode : ChangePhoneIntent
    data object UseDifferentNumber : ChangePhoneIntent
}

sealed interface ChangePhoneEvent {
    data class Message(val text: UiText) : ChangePhoneEvent
}
