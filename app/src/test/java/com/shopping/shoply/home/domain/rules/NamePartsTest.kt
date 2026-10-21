package com.shopping.shoply.home.domain.rules

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("NamePartsTest")
class NamePartsTest {

    @ParameterizedTest
    @DisplayName("Returns a pair of first name and shortcut")
    @CsvSource(
        "Ahmed Mansour, Ahmed, AM",
        "ahmed mansour ali, ahmed, AM",
        "'', 'there', ",
        "Ahmed, Ahmed, A",
        "'  ahmed   mansour ', ahmed, AM"
    )
    fun `construct a pair of first name and shortcut from passed name`(
        name: String,
        firstName: String,
        shortcut: String?
    ) {
        assertThat(nameParts(name)).isEqualTo(firstName to shortcut)
    }
}