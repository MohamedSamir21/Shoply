package com.shopping.shoply.authentication.data.repository

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.shopping.shoply.authentication.data.datasource.countryinfo.LocaleDataSource
import com.shopping.shoply.authentication.data.datasource.countryinfo.TelephonyManagerDataSource
import com.shopping.shoply.authentication.data.mapper.toDomainISOCountryCode
import com.shopping.shoply.authentication.data.mapper.toList
import com.shopping.shoply.authentication.domain.interfaces.repository.CountryInfoRepository
import com.shopping.shoply.authentication.domain.model.ISOCountryCode
import com.shopping.shoply.common.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Resolves the country information the sign-up phone-number picker needs — the selectable regions plus
 * each region's calling code, display name, and an example number.
 *
 * Region detection prefers the device's SIM/network country and falls back to the full JDK locale list
 * when that is unavailable; phone-number facts come from libphonenumber. All work is confined to
 * [defaultDispatcher], keeping the domain free of Android and threading concerns.
 *
 * @property telephonyManagerDataSource reports the device's SIM/network region, if any.
 * @property localeDataSource supplies the JDK's ISO regions and their display names.
 * @property phoneNumberUtil resolves calling codes and example numbers per region.
 */
class CountryInfoRepositoryImpl @Inject constructor(
    private val telephonyManagerDataSource: TelephonyManagerDataSource,
    private val localeDataSource: LocaleDataSource,
    private val phoneNumberUtil: PhoneNumberUtil,
    // Threading is owned here in the data layer (injected dispatcher), not in the domain.
    // Default (CPU-bound) rather than IO: the heavy cost is parsing libphonenumber metadata.
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : CountryInfoRepository {

    /**
     * Returns the selectable regions, preferring the single region the device's SIM/network reports and
     * falling back to every ISO region the JDK knows when the device reports none.
     */
    override suspend fun getISOCountryCodes(): List<ISOCountryCode> = withContext(defaultDispatcher) {
        // networkCountryIso is "" (not null) when the region is unavailable, so guard on the source
        // string — the mapped list is always size 1 and would never look empty.
        val isoCountryCode = telephonyManagerDataSource.getISOCountryCode()
        if (!isoCountryCode.isNullOrBlank())
            return@withContext isoCountryCode.toDomainISOCountryCode().toList()
        localeDataSource.getISOCountryCodes()
            .map { it.toDomainISOCountryCode() }
    }

    /** Returns the international calling code for [isoCountryCode] (e.g. `EG` -> `20`). */
    override suspend fun getCountryCode(isoCountryCode: ISOCountryCode): Int =
        withContext(defaultDispatcher) {
            phoneNumberUtil.getCountryCodeForRegion(isoCountryCode.symbol.uppercase())
        }

    /** Returns the English display name for [isoCountryCode] (e.g `EG` -> `Egypt`). */
    override suspend fun getCountryName(isoCountryCode: ISOCountryCode): String =
        withContext(defaultDispatcher) {
            localeDataSource.getCountryName(isoCountryCode)
        }

    /** Returns a formatted example national mobile number for [isoCountryCode], or `""` if none exists. */
    override suspend fun getExamplePhoneNumber(
        isoCountryCode: ISOCountryCode
    ): String = withContext(defaultDispatcher) {
        val examplePhoneNumber = phoneNumberUtil.getExampleNumberForType(
            isoCountryCode.symbol.uppercase(),
            PhoneNumberUtil.PhoneNumberType.MOBILE
        ) ?: return@withContext ""
        phoneNumberUtil.format(
            examplePhoneNumber,
            PhoneNumberUtil.PhoneNumberFormat.NATIONAL
        )
    }
}