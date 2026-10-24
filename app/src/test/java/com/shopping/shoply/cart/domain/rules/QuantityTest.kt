package com.shopping.shoply.cart.domain.rules

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("QuantityTest")
class QuantityTest {

    @ParameterizedTest
    @DisplayName("keeps the quantity within 1..20")
    @CsvSource(
        "-3, 1",
        "0, 1",
        "1, 1",
        "7, 7",
        "20, 20",
        "21, 20",
    )
    fun `keeps the quantity within 1 to 20`(quantity: Int, expected: Int) {
        assertThat(clampQuantity(quantity)).isEqualTo(expected)
    }
}
