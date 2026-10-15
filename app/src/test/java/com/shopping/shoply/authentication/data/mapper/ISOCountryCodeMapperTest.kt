package com.shopping.shoply.authentication.data.mapper

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.authentication.domain.model.ISOCountryCode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * Tests for the [ISOCountryCode] mappers: wrapping a raw region code
 * ([toDomainISOCountryCode]) and lifting a single code into a list ([toList]).
 */
@DisplayName("ISOCountryCodeMapper")
class ISOCountryCodeMapperTest {

    @ParameterizedTest(name = "wraps '{0}'")
    @ValueSource(strings = ["EG", ""])
    @DisplayName("wraps a region code into an ISOCountryCode")
    fun `wraps region code`(code: String) {
        val wrapped = code.toDomainISOCountryCode()

        assertThat(wrapped.symbol).isEqualTo(code)
    }

    @Test
    @DisplayName("lifts a single code into a one-element list")
    fun `wraps region code as a single list`() {
        val isoCountryCode = ISOCountryCode("EG")

        val result = isoCountryCode.toList()

        assertThat(result).containsExactly(isoCountryCode)
    }
}
