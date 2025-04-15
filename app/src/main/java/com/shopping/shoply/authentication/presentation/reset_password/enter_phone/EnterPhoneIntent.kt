package com.shopping.shoply.authentication.presentation.reset_password.enter_phone

import com.shopping.shoply.authentication.domain.model.CountryInfo

interface EnterPhoneIntent {
    data object ToggleCountryInfoMenuVisibility: EnterPhoneIntent
    data class ChangeSearchKeyword(val keyword: String): EnterPhoneIntent
    data class SelectCountryInfo(val countryInfo: CountryInfo): EnterPhoneIntent
}