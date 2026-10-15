package com.shopping.shoply.authentication.domain.interfaces.repository

import com.shopping.shoply.authentication.domain.model.ISOCountryCode

interface CountryInfoRepository {
    // suspend: the data layer owns threading, so the domain never picks a dispatcher.
    suspend fun getISOCountryCodes(): List<ISOCountryCode>
    suspend fun getCountryCode(isoCountryCode: ISOCountryCode): Int
    suspend fun getCountryName(isoCountryCode: ISOCountryCode): String
    suspend fun getExamplePhoneNumber(isoCountryCode: ISOCountryCode): String
}