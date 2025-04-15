package com.shopping.shoply.authentication.domain.interfaces.repoistory

import com.shopping.shoply.authentication.domain.model.DomainContext
import com.shopping.shoply.authentication.domain.model.ISOCountryCode

interface CountryInfoRepository {
    fun getISOCountryCodes(domainContext: DomainContext): List<ISOCountryCode>
    fun getCountryCode(isoCountryCode: ISOCountryCode): Int
    fun getCountryName(isoCountryCode: ISOCountryCode): String
    fun getExamplePhoneNumber(isoCountryCode: ISOCountryCode): String
}