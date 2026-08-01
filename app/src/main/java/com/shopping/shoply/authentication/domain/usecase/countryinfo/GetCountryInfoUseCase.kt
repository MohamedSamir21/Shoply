package com.shopping.shoply.authentication.domain.usecase.countryinfo

import com.shopping.shoply.authentication.domain.interfaces.repository.CountryInfoRepository
import com.shopping.shoply.authentication.domain.model.CountryInfo
import com.shopping.shoply.authentication.domain.model.ISOCountryCode
import javax.inject.Inject

// No android.util.Log here: domain stays framework-free so it can be unit-tested on the JVM.
// No dispatcher selection either — threading is owned by the data layer (the repository),
// so this use case just orchestrates and stays thread-agnostic.
class GetCountryInfoUseCase @Inject constructor(
    private val countryInfoRepository: CountryInfoRepository
) {
    suspend operator fun invoke(): List<CountryInfo> =
        countryInfoRepository.getISOCountryCodes().map { getCountryInfo(it.symbol) }

    private suspend fun getCountryInfo(
        countrySymbol: String
    ): CountryInfo {
        val isoCountryCode = ISOCountryCode(countrySymbol)
        val codeNumber = countryInfoRepository.getCountryCode(isoCountryCode)
        val countryName = countryInfoRepository.getCountryName(isoCountryCode)
        val format = countryInfoRepository.getExamplePhoneNumber(isoCountryCode)
        val flag = constructCountryFlagEmoji(isoCountryCode.symbol)
        return CountryInfo(
            codeNumber = "+$codeNumber",
            name = countryName,
            flag = flag,
            format = format
        )
    }

    // Pure CPU string math — no I/O and no dispatcher concern.
    private fun constructCountryFlagEmoji(
        isoCountryCode: String
    ): String {
        if (isoCountryCode.length != 2) return "?" // Ensure the ISO code is 2 letters, otherwise return ?
        val countryCodeUppercased = isoCountryCode.uppercase()
        val flagOffset = 0x1F1E6 // Base Unicode for 'A'
        val firstChar = Character.codePointAt(countryCodeUppercased, 0) - 'A'.code + flagOffset
        val secondChar = Character.codePointAt(countryCodeUppercased, 1) - 'A'.code + flagOffset
        return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
    }
}