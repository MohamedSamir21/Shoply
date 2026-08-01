package com.shopping.shoply.authentication.data.datasource.countryinfo

import com.shopping.shoply.authentication.domain.model.ISOCountryCode
import java.util.Locale

class LocaleDataSource {
    fun getISOCountryCodes(): List<String> {
        val twoLettersIsoCountryCodes = Locale.getISOCountries()
        val notIL = { isoCode: String -> isoCode != "IL" }
        return twoLettersIsoCountryCodes.filter(notIL).toList()
    }

    fun getCountryName(isoCountryCode: ISOCountryCode): String {
        return Locale(
            "", isoCountryCode.symbol
        ).getDisplayCountry(Locale.ENGLISH)
    }
}