package com.shopping.shoply.cart.domain.rules

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("CartBadgeTest")
class CartBadgeTest {

    @ParameterizedTest
    @DisplayName("constructs a string badge from integer totalQuantity")
    @CsvSource(
        "-1, ",
        "0, ",
        "1, 1",
        "99, 99",
        "100, 99+"
    )
    fun `maps total number of notifications to a badge`(
        totalQuantity: Int,
        badge: String?
    ) {
        assertThat(cartBadge(totalQuantity)).isEqualTo(badge)
    }
}