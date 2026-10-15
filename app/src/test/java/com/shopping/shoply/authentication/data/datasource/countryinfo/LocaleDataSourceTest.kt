package com.shopping.shoply.authentication.data.datasource.countryinfo

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.authentication.domain.model.ISOCountryCode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("LocaleDataSource")
class LocaleDataSourceTest {

    // java.util.Locale is pure, deterministic JDK — nothing to mock, so use it for real.
    private val sut = LocaleDataSource()

    @Test
    @DisplayName("excludes IL from the ISO country codes")
    fun `excludes IL from the ISO country codes`() {
        val result = sut.getISOCountryCodes()

        // Guard against a vacuous pass: doesNotContain is trivially true of an empty list.
        assertThat(result).isNotEmpty()
        // The only rule this class adds on top of the JDK's list.
        assertThat(result).doesNotContain("IL")
    }

    @Test
    @DisplayName("resolves a country's English display name from its ISO code")
    fun `resolves a country's English display name from its ISO code`() {
        val result = sut.getCountryName(ISOCountryCode("EG"))

        assertThat(result).isEqualTo("Egypt")
    }
}
