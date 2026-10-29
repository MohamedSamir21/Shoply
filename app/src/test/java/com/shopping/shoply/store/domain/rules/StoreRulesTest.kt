package com.shopping.shoply.store.domain.rules

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.testutil.aProduct
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("StoreRulesTest")
class StoreRulesTest {
    val plain = aProduct(id = "p1", rating = 4.1)
    val discounted = aProduct(id = "p2", rating = 4.9, priceCents = 900, oldPriceCents = 1200)
    val sameOldPrice = aProduct(id = "p3", rating = 4.5, priceCents = 900, oldPriceCents = 900)
    val products = listOf(plain, discounted, sameOldPrice)

    @Test
    @DisplayName("storeProducts: All as given, Top Rated highest first, Offers only discounted")
    fun `storeProducts per tab`() {
        assertThat(storeProducts(products, StoreTab.ALL)).containsExactly(plain, discounted, sameOldPrice).inOrder()
        assertThat(storeProducts(products, StoreTab.TOP_RATED)).containsExactly(discounted, sameOldPrice, plain).inOrder()
        assertThat(storeProducts(products, StoreTab.OFFERS)).containsExactly(discounted)
    }

    @Test
    @DisplayName("openingStatus: open inside the hours; closed before opening and at the closing minute")
    fun `openingStatus open and closed`() {
        val opens = 10 * 60
        val closes = 21 * 60

        assertThat(openingStatus(opens, closes, nowMinutes = opens)).isEqualTo(OpeningStatus.OpenUntil(closes))
        assertThat(openingStatus(opens, closes, nowMinutes = 15 * 60)).isEqualTo(OpeningStatus.OpenUntil(closes))
        assertThat(openingStatus(opens, closes, nowMinutes = 8 * 60)).isEqualTo(OpeningStatus.ClosedOpensAt(opens))
        assertThat(openingStatus(opens, closes, nowMinutes = closes)).isEqualTo(OpeningStatus.ClosedOpensAt(opens))
    }
}
