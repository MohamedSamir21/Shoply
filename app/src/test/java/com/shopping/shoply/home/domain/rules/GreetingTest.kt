package com.shopping.shoply.home.domain.rules

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("GreetingTest")
class GreetingTest {

    @ParameterizedTest
    @DisplayName("Shows greeting for the hour")
    @CsvSource(
        "5, MORNING",
        "11, MORNING",
        "12, AFTERNOON",
        "16, AFTERNOON",
        "17, EVENING",
        "23, EVENING",
        "0, EVENING",
        "4, EVENING",
        "-2, NONE",
        "24, NONE"
    )
    fun `Shows greeting for the hour`(
        hour: Int,
        greeting: String
    ) {
        assertThat(greetingFor(hour).name).isEqualTo(greeting)
    }
}