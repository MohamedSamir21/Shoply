package com.shopping.shoply.authentication.domain.usecase.countryinfo

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.authentication.domain.interfaces.repository.CountryInfoRepository
import com.shopping.shoply.authentication.domain.model.CountryInfo
import com.shopping.shoply.authentication.domain.model.ISOCountryCode
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("GetCountryInfoUseCase")
class GetCountryInfoUseCaseTest {

    private val countryInfoRepository: CountryInfoRepository = mockk()
    private val getCountryInfo = GetCountryInfoUseCase(countryInfoRepository)

    @Test
    @DisplayName("assembles each region's flag, calling code, and example number")
    fun `assembles country info for every region the repository reports`() = runTest {
        val egypt = ISOCountryCode("EG")
        coEvery { countryInfoRepository.getISOCountryCodes() } returns listOf(egypt)
        coEvery { countryInfoRepository.getCountryCode(egypt) } returns 20
        coEvery { countryInfoRepository.getCountryName(egypt) } returns "Egypt"
        coEvery { countryInfoRepository.getExamplePhoneNumber(egypt) } returns "01001234567"

        val result = getCountryInfo()

        assertThat(result).containsExactly(
            CountryInfo(codeNumber = "+20", name = "Egypt", flag = "🇪🇬", format = "01001234567")
        )
    }

    @Test
    @DisplayName("falls back to a question-mark flag for a malformed region code")
    fun `falls back to a question-mark flag for a malformed region code`() = runTest {
        val malformed = ISOCountryCode("EGY")
        coEvery { countryInfoRepository.getISOCountryCodes() } returns listOf(malformed)
        coEvery { countryInfoRepository.getCountryCode(malformed) } returns 20
        coEvery { countryInfoRepository.getCountryName(malformed) } returns "Egypt"
        coEvery { countryInfoRepository.getExamplePhoneNumber(malformed) } returns "01001234567"

        val result = getCountryInfo()

        assertThat(result.single().flag).isEqualTo("?")
    }
}
