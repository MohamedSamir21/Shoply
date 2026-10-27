package com.shopping.shoply.cart.domain.rules

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.testutil.aCartItem
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@DisplayName("CartTotalsTest")
class CartTotalsTest {
    val fees = mapOf("a" to 199L, "b" to 299L)

    @Test
    @DisplayName("an empty cart is all zeros")
    fun `an empty cart is all zeros`() {
        assertThat(cartTotals(emptyList(), fees, percentOff = 10)).isEqualTo(CartTotals())
    }

    @Test
    @DisplayName("subtotal is price times quantity, and itemCount sums the quantities")
    fun `subtotal is price times quantity, and itemCount sums the quantities`() {
        val items = listOf(aCartItem(priceCents = 500, quantity = 3), aCartItem(priceCents = 1200, quantity = 1))

        val totals = cartTotals(items, emptyMap(), percentOff = null)

        assertThat(totals.subtotalCents).isEqualTo(2700)
        assertThat(totals.itemCount).isEqualTo(4)
    }

    @Test
    @DisplayName("below $35, each distinct store's fee is charged once")
    fun `below 35, each distinct store's fee is charged once`() {
        val items = listOf(
            aCartItem(storeId = "a", priceCents = 500),
            aCartItem(storeId = "a", priceCents = 500),
            aCartItem(storeId = "b", priceCents = 500),
        )

        assertThat(cartTotals(items, fees, percentOff = null).deliveryCents).isEqualTo(199 + 299)
    }

    @Test
    @DisplayName("a store with an unknown fee counts as free")
    fun `a store with an unknown fee counts as free`() {
        val items = listOf(aCartItem(storeId = "a"), aCartItem(storeId = "unknown"))

        assertThat(cartTotals(items, fees, percentOff = null).deliveryCents).isEqualTo(199)
    }

    @ParameterizedTest
    @DisplayName("delivery is free from $35")
    @CsvSource(
        "3499, 199",
        "3500, 0",
        "9000, 0",
    )
    fun `delivery is free from 35`(subtotal: Long, expectedDelivery: Long) {
        val items = listOf(aCartItem(storeId = "a", priceCents = subtotal))

        assertThat(cartTotals(items, fees, percentOff = null).deliveryCents).isEqualTo(expectedDelivery)
    }

    @ParameterizedTest
    @DisplayName("the discount is the percent of the subtotal, rounded to the nearest cent")
    @CsvSource(
        "33700, 10, 3370",
        "1005, 10, 101",
        "1004, 10, 100",
        "999, 15, 150",
    )
    fun `the discount is rounded to the nearest cent`(subtotal: Long, percentOff: Int, expected: Long) {
        val items = listOf(aCartItem(priceCents = subtotal))

        assertThat(cartTotals(items, emptyMap(), percentOff).discountCents).isEqualTo(expected)
    }

    @Test
    @DisplayName("total is subtotal plus delivery minus discount")
    fun `total is subtotal plus delivery minus discount`() {
        val items = listOf(aCartItem(storeId = "a", priceCents = 2000))

        val totals = cartTotals(items, fees, percentOff = 10)

        assertThat(totals.totalCents).isEqualTo(2000 + 199 - 200)
    }

    @Test
    @DisplayName("the express fee is added to delivery, even when delivery is otherwise free")
    fun `the express fee is added to delivery, even when delivery is otherwise free`() {
        val below = listOf(aCartItem(storeId = "a", priceCents = 2000))
        val over = listOf(aCartItem(storeId = "a", priceCents = 4000))

        assertThat(cartTotals(below, fees, null, EXPRESS_FEE_CENTS).deliveryCents).isEqualTo(199 + 499)
        assertThat(cartTotals(over, fees, null, EXPRESS_FEE_CENTS).totalCents).isEqualTo(4000 + 499)
    }

    @Test
    @DisplayName("total is never below 0")
    fun `total is never below 0`() {
        val items = listOf(aCartItem(priceCents = 2000))

        assertThat(cartTotals(items, emptyMap(), percentOff = 150).totalCents).isEqualTo(0)
    }
}
