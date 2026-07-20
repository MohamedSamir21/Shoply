package com.shopping.shoply.authentication.data.repository

import com.google.common.truth.Truth.assertThat
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.shopping.shoply.authentication.data.datasource.country_info.LocaleDataSource
import com.shopping.shoply.authentication.data.datasource.country_info.TelephonyManagerDataSource
import com.shopping.shoply.authentication.domain.model.ISOCountryCode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("CountryInfoRepositoryImpl")
class CountryInfoRepositoryImplTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val telephonyManagerDataSource: TelephonyManagerDataSource = mockk()
    private val localeDataSource: LocaleDataSource = mockk()

    // Real, pure, deterministic library — mocking it would only test the mock.
    private val phoneNumberUtil: PhoneNumberUtil = PhoneNumberUtil.getInstance()

    private val sut = CountryInfoRepositoryImpl(
        telephonyManagerDataSource = telephonyManagerDataSource,
        localeDataSource = localeDataSource,
        phoneNumberUtil = phoneNumberUtil,
        defaultDispatcher = testDispatcher
    )

    @Test
    @DisplayName("prefers the device's SIM/network region and does not consult the locale list")
    fun `prefers the device region over the locale list`() = runTest(testDispatcher) {
        every { telephonyManagerDataSource.getISOCountryCode() } returns "eg"

        val result = sut.getISOCountryCodes()

        assertThat(result).containsExactly(ISOCountryCode("eg"))
        verify(exactly = 0) { localeDataSource.getISOCountryCodes() }
    }

    @Test
    @DisplayName("falls back to the locale list when the device reports no region")
    fun `falls back to the locale list when device region is null`() = runTest(testDispatcher) {
        every { telephonyManagerDataSource.getISOCountryCode() } returns null
        every { localeDataSource.getISOCountryCodes() } returns listOf("US", "EG")

        val result = sut.getISOCountryCodes()

        assertThat(result).containsExactly(ISOCountryCode("US"), ISOCountryCode("EG")).inOrder()
    }

    @Test
    @DisplayName("falls back to the locale list when the device region is blank")
    fun `falls back to the locale list when device region is blank`() = runTest(testDispatcher) {
        // networkCountryIso returns "" (not null) when unavailable — the bug the guard now handles.
        every { telephonyManagerDataSource.getISOCountryCode() } returns ""
        every { localeDataSource.getISOCountryCodes() } returns listOf("US")

        val result = sut.getISOCountryCodes()

        assertThat(result).containsExactly(ISOCountryCode("US"))
    }

    @Test
    @DisplayName("resolves the international calling code, uppercasing the region")
    fun `resolves the international calling code`() = runTest(testDispatcher) {
        // Lowercase input proves the .uppercase() the repository adds before the lib call.
        val result = sut.getCountryCode(ISOCountryCode("eg"))

        assertThat(result).isEqualTo(20)
    }

    @Test
    @DisplayName("returns a non-empty example number for a valid region")
    fun `returns a non-empty example number for a valid region`() = runTest(testDispatcher) {
        val result = sut.getExamplePhoneNumber(ISOCountryCode("us"))

        // Don't assert the exact number (library-version dependent) — just that formatting produced one.
        assertThat(result).isNotEmpty()
    }
}
