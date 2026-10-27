package com.shopping.shoply.checkout.domain.rules

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.Calendar

@DisplayName("DeliverySlotsTest")
class DeliverySlotsTest {

    private fun at(day: Int, hour: Int, minute: Int = 0): Long = Calendar.getInstance().apply {
        clear()
        set(2026, Calendar.OCTOBER, day, hour, minute)
    }.timeInMillis

    @Test
    @DisplayName("gives today and the next 3 days, each with the 6 windows from 10 AM")
    fun `gives today and the next 3 days, each with the 6 windows from 10 AM`() {
        val days = deliveryDays(at(27, 8))

        assertThat(days.map { it.startMillis }).containsExactly(at(27, 0), at(28, 0), at(29, 0), at(30, 0)).inOrder()
        assertThat(days[0].slots.map { it.startHour }).containsExactly(10, 12, 14, 16, 18, 20).inOrder()
        assertThat(days.flatMap { it.slots }.none { it.full }).isTrue()
    }

    @Test
    @DisplayName("today's slots starting less than 1 hour from now are full")
    fun `today's slots starting less than 1 hour from now are full`() {
        val days = deliveryDays(at(27, 11, 30))

        assertThat(days[0].slots.filter { it.full }.map { it.startHour }).containsExactly(10, 12)
        assertThat(days[1].slots.none { it.full }).isTrue()
    }

    @Test
    @DisplayName("a slot exactly 1 hour away is still free")
    fun `a slot exactly 1 hour away is still free`() {
        val days = deliveryDays(at(27, 19))

        assertThat(days[0].startMillis).isEqualTo(at(27, 0))
        assertThat(days[0].slots.filterNot { it.full }.map { it.startHour }).containsExactly(20)
    }

    @Test
    @DisplayName("when every slot today is full, the 4 days start tomorrow")
    fun `when every slot today is full, the 4 days start tomorrow`() {
        val days = deliveryDays(at(27, 19, 1))

        assertThat(days.map { it.startMillis }).containsExactly(at(28, 0), at(29, 0), at(30, 0), at(31, 0)).inOrder()
    }
}
