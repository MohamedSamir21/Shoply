package com.shopping.shoply.authentication.data.repository

import android.util.Log
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.shopping.shoply.authentication.data.datasource.country_info.LocaleDataSource
import com.shopping.shoply.authentication.data.datasource.country_info.TelephonyManagerDataSource
import com.shopping.shoply.authentication.data.mappers.toDomainISOCountryCode
import com.shopping.shoply.authentication.data.mappers.toList
import com.shopping.shoply.authentication.domain.interfaces.repoistory.CountryInfoRepository
import com.shopping.shoply.authentication.domain.model.DomainContext
import com.shopping.shoply.authentication.domain.model.ISOCountryCode
import com.shopping.shoply.authentication.presentation.model.ActivityContext
import javax.inject.Inject

private const val TAG = "CountryInfoRepositoryImpl"

class CountryInfoRepositoryImpl @Inject constructor(
    private val telephonyManagerDataSource: TelephonyManagerDataSource,
    private val localeDataSource: LocaleDataSource,
    private val phoneNumberUtil: PhoneNumberUtil
) : CountryInfoRepository {

    override fun getISOCountryCodes(
        domainContext: DomainContext
    ): List<ISOCountryCode> {
        val activityContext = domainContext as ActivityContext
        val isoCountryCode = telephonyManagerDataSource.getISOCountryCode(activityContext.context)
        val domainTelephonyISOCountryCode = isoCountryCode?.toDomainISOCountryCode()?.toList()
        if (!domainTelephonyISOCountryCode.isNullOrEmpty())
            return domainTelephonyISOCountryCode
        val domainISOCountryCodes = localeDataSource.getISOCountryCodes()
            .map { it.toDomainISOCountryCode() }
        return domainISOCountryCodes
    }

    override fun getCountryCode(isoCountryCode: ISOCountryCode): Int {
        return phoneNumberUtil.getCountryCodeForRegion(isoCountryCode.code.uppercase())
    }

    override fun getCountryName(isoCountryCode: ISOCountryCode): String {
        return localeDataSource.getCountryName(isoCountryCode)
    }

    override fun getExamplePhoneNumber(
        isoCountryCode: ISOCountryCode
    ): String {
        val examplePhoneNumber = phoneNumberUtil.getExampleNumberForType(
            isoCountryCode.code.uppercase(),
            PhoneNumberUtil.PhoneNumberType.MOBILE
        ) ?: return ""
        val formatedPhoneNumber = phoneNumberUtil.format(
            examplePhoneNumber,
            PhoneNumberUtil.PhoneNumberFormat.NATIONAL
        )
        return formatedPhoneNumber
    }
}