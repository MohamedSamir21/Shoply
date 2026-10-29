package com.shopping.shoply.search.domain.rules

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.catalog.domain.rules.PriceRange
import com.shopping.shoply.catalog.domain.rules.ProductFilters
import com.shopping.shoply.catalog.domain.rules.ProductSort
import com.shopping.shoply.catalog.domain.rules.applyFilters
import com.shopping.shoply.testutil.aProduct
import com.shopping.shoply.testutil.aStore
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("SearchRulesTest")
class SearchRulesTest {
    val headphones = aProduct(id = "p1", name = "Wireless Headphones", storeId = "volt", priceCents = 8900, rating = 4.6)
    val fern = aProduct(id = "p2", name = "Monstera", storeId = "fern", categoryId = "home", subcategory = "Plant", priceCents = 2500, rating = 4.9)
    val mug = aProduct(id = "p3", name = "Stoneware Mug", storeId = "nest", categoryId = "home", keywords = "coffee cup", priceCents = 1800, rating = 4.2)
    val products = listOf(headphones, fern, mug)
    val storeNames = mapOf("volt" to "Volt Electronics", "fern" to "Fern & Co.", "nest" to "Nest")

    @Test
    @DisplayName("matchProducts: name, subcategory, keywords and store name, any case, trailing s ignored")
    fun `matchProducts fields`() {
        assertThat(matchProducts(products, storeNames, "HEADPHONES")).containsExactly(headphones)
        assertThat(matchProducts(products, storeNames, "plants")).containsExactly(fern)
        assertThat(matchProducts(products, storeNames, "coffee")).containsExactly(mug)
        assertThat(matchProducts(products, storeNames, "volt")).containsExactly(headphones)
        assertThat(matchProducts(products, storeNames, "  ")).isEmpty()
    }

    @Test
    @DisplayName("matchStores: name or category")
    fun `matchStores name or category`() {
        val volt = aStore(id = "volt", name = "Volt Electronics", category = "Electronics")
        val fernStore = aStore(id = "fern", name = "Fern & Co.", category = "Plants")

        assertThat(matchStores(listOf(volt, fernStore), "plant")).containsExactly(fernStore)
        assertThat(matchStores(listOf(volt, fernStore), "volt")).containsExactly(volt)
    }

    @Test
    @DisplayName("withRecent: newest first, no case-insensitive duplicates, at most 5")
    fun `withRecent order, duplicates, cap`() {
        assertThat(withRecent(listOf("lamp", "Mug"), "mug")).containsExactly("mug", "lamp").inOrder()
        assertThat(withRecent(listOf("a", "b", "c", "d", "e"), "f")).containsExactly("f", "a", "b", "c", "d").inOrder()
        assertThat(withRecent(listOf("a"), "  ")).containsExactly("a")
    }

    @Test
    @DisplayName("applyFilters: price bounds, rating, category, free delivery")
    fun `applyFilters each filter`() {
        assertThat(applyFilters(products, ProductFilters(price = PriceRange.UNDER_25), ProductSort.RELEVANCE, emptySet())).containsExactly(mug)
        assertThat(applyFilters(products, ProductFilters(price = PriceRange.FROM_25_TO_50), ProductSort.RELEVANCE, emptySet())).containsExactly(fern)
        assertThat(applyFilters(products, ProductFilters(price = PriceRange.FROM_50_TO_100), ProductSort.RELEVANCE, emptySet())).containsExactly(headphones)
        assertThat(applyFilters(products, ProductFilters(minRating = 4.5), ProductSort.RELEVANCE, emptySet())).containsExactly(headphones, fern)
        assertThat(applyFilters(products, ProductFilters(categoryIds = setOf("home")), ProductSort.RELEVANCE, emptySet())).containsExactly(fern, mug)
        assertThat(applyFilters(products, ProductFilters(freeDeliveryOnly = true), ProductSort.RELEVANCE, setOf("volt"))).containsExactly(headphones)
    }

    @Test
    @DisplayName("applyFilters: sort orders; Relevance keeps the given order")
    fun `applyFilters sort`() {
        assertThat(applyFilters(products, ProductFilters(), ProductSort.RELEVANCE, emptySet())).containsExactly(headphones, fern, mug).inOrder()
        assertThat(applyFilters(products, ProductFilters(), ProductSort.PRICE_LOW_HIGH, emptySet())).containsExactly(mug, fern, headphones).inOrder()
        assertThat(applyFilters(products, ProductFilters(), ProductSort.PRICE_HIGH_LOW, emptySet())).containsExactly(headphones, fern, mug).inOrder()
        assertThat(applyFilters(products, ProductFilters(), ProductSort.TOP_RATED, emptySet())).containsExactly(fern, headphones, mug).inOrder()
    }
}
