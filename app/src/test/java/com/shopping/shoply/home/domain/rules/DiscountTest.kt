package com.shopping.shoply.home.domain.rules

import com.google.common.truth.Truth
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("DiscountTest")
class DiscountTest {

    @Test
    @DisplayName("no discount should return null")
    fun `no old price means no discount`() {
        Truth.assertThat(discountPercent(8000L, null)).isNull()
    }

    @Test
    @DisplayName("20 percent off")
    fun `20 percent off`() {
        Truth.assertThat(discountPercent(8000L, 10000L)).isEqualTo(20)
    }

    @Test
    @DisplayName("18 percent off")
    fun `18 percent off`() {
        Truth.assertThat(discountPercent(8900, 10900)).isEqualTo(18)
    }

    @Test
    @DisplayName("new price equals to old price -> returns null")
    fun `gives null when new price equals to old price`() {
        Truth.assertThat(discountPercent(8900, 8900)).isNull()
    }

    @Test
    @DisplayName("new price is greater than the old price -> returns null")
    fun `gives null when new price is greater than the old price`() {
        Truth.assertThat(discountPercent(10900, 8900)).isNull()
    }

    @Test
    @DisplayName("checks for rounding is done correctly")
    fun `checks for rounding is done correctly`() {
        Truth.assertThat(discountPercent(199, 300)).isEqualTo(34)
    }
}