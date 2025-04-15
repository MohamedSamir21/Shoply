package com.shopping.shoply.authentication.domain.usecase.get_country_info

import android.util.Log
import com.shopping.shoply.authentication.domain.interfaces.repoistory.CountryInfoRepository
import com.shopping.shoply.authentication.domain.model.CountryInfo
import com.shopping.shoply.authentication.domain.model.DomainContext
import com.shopping.shoply.authentication.domain.model.ISOCountryCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "GetCountryInfoUseCase"

class GetCountryInfoUseCase @Inject constructor(
    private val countryInfoRepository: CountryInfoRepository
) {
    suspend operator fun invoke(
        domainContext: DomainContext
    ): Flow<List<CountryInfo>> = withContext(Dispatchers.IO) {
        val countriesInfoList = countryInfoRepository.getISOCountryCodes(domainContext).map {
            getCountryInfo(it.code)
        }
        Log.d(TAG, countriesInfoList.toString())
        return@withContext flow {
            emit(countriesInfoList)
        }
    }

    private suspend fun getCountryInfo(
        countryCode: String
    ): CountryInfo = withContext(Dispatchers.IO) {
        val isoCountryCode = ISOCountryCode(countryCode)
        val codeNumber = countryInfoRepository.getCountryCode(isoCountryCode)
        val countryName = countryInfoRepository.getCountryName(isoCountryCode)
        val format = countryInfoRepository.getExamplePhoneNumber(isoCountryCode)
        val flag = constructCountryFlagEmoji(isoCountryCode.code)
        return@withContext CountryInfo(
            code = "+$codeNumber",
            name = countryName,
            flag = flag,
            format = format
        )
    }

    private suspend fun constructCountryFlagEmoji(
        isoCountryCode: String
    ): String {
        return withContext(
            Dispatchers.IO
        ) {
            if (isoCountryCode.length != 2) return@withContext "?" // Ensure the ISO code is 2 letters, otherwise return ?
            val countryCodeUppercased = isoCountryCode.uppercase()
            val flagOffset = 0x1F1E6 // Base Unicode for 'A'
            val firstChar = Character.codePointAt(countryCodeUppercased, 0) - 'A'.code + flagOffset
            val secondChar = Character.codePointAt(countryCodeUppercased, 1) - 'A'.code + flagOffset
            String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
        }
    }
}