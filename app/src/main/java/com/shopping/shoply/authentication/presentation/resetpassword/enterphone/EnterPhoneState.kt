package com.shopping.shoply.authentication.presentation.resetpassword.enterphone

import com.shopping.shoply.authentication.domain.model.CountryInfo
import com.shopping.shoply.authentication.presentation.utility.UiText

data class EnterPhoneState(
    val selectedCountryInfo: CountryInfo = CountryInfo("", "", "", ""),
    val countriesInfo: List<CountryInfo> = emptyList(),
    val countriesInfoByKeyword: List<CountryInfo> = emptyList(),
    val keyword: String = "",
    val phoneNumber: String = "",
    val showCountryInfoMenu: Boolean = false,
    val isCountryCodeSearchButtonClickable: Boolean = true,
    val error: UiText? = null,
    val isProcessing: Boolean = false
)