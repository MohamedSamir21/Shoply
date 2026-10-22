package com.shopping.shoply.help.domain.rules

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.testutil.sampleFaqs
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("FilterFaqsTest")
class FilterFaqsTest {
    @Test
    @DisplayName("returns all FAQs when the query is blank")
    fun `returns all FAQs when the query is blank`() {

        assertThat(filterFaqs(sampleFaqs, "")).isEqualTo(sampleFaqs)
        assertThat(filterFaqs(sampleFaqs, " ")).isEqualTo(sampleFaqs)
    }

    @Test
    @DisplayName("returns all FAQs whose question or answer contains query")
    fun `returns all FAQs whose question or answer contains query`() {
        val query = "order"
        val filteredFaqs = filterFaqs(sampleFaqs, query)
        assertThat(filteredFaqs).containsExactly(sampleFaqs[0], sampleFaqs[2]).inOrder()
    }

    @Test
    @DisplayName("returns all FAQs matching query ignoring case")
    fun `returns all FAQs matching query ignoring case`() {
        val query = "refund"
        val lowerCasedFilteredFaqs = filterFaqs(sampleFaqs, query)
        val upperCasedFilteredFaqs = filterFaqs(sampleFaqs, query.uppercase())
        assertThat(lowerCasedFilteredFaqs).containsExactly(sampleFaqs[1])
        assertThat(upperCasedFilteredFaqs).containsExactly(sampleFaqs[1])
    }

    @Test
    @DisplayName("ensures that the filter trims the query")
    fun `ensures that the filter trims the query`() {
        val query = "  Cash  "
        val filteredFaqs = filterFaqs(sampleFaqs, query)
        assertThat(filteredFaqs).containsExactly(sampleFaqs[3])
    }

    @Test
    @DisplayName("ensures the the filter returns empty if there's no match")
    fun `ensures the the filter returns empty if there's no match`() {
        val queryWithNoMatch = "crypto"
        val filteredFaqs = filterFaqs(sampleFaqs, queryWithNoMatch)
        assertThat(filteredFaqs).isEmpty()
    }
}