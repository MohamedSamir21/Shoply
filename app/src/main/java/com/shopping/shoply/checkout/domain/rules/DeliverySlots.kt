package com.shopping.shoply.checkout.domain.rules

import java.util.Calendar

/** Standard delivery windows start at these hours and last 2 hours (10 AM – 10 PM). */
val SLOT_START_HOURS = listOf(10, 12, 14, 16, 18, 20)

/** How many days the user can pick from. */
const val DELIVERY_DAY_COUNT = 4

private const val HOUR_MS = 60 * 60 * 1000L

/** A delivery day; [startMillis] is its midnight. */
data class DeliveryDay(val startMillis: Long, val slots: List<DeliverySlot>)

/** A 2-hour window from [startHour]; [full] ones can't be picked. */
data class DeliverySlot(val startHour: Int, val full: Boolean)

/**
 * The next [DELIVERY_DAY_COUNT] delivery days from [nowMillis], on the device's calendar.
 *
 * - A slot is full when it starts less than 1 hour from now (so only today's slots can be full).
 * - When every slot today is full, the days start tomorrow.
 */
fun deliveryDays(nowMillis: Long): List<DeliveryDay> {
    val day = Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val days = mutableListOf<DeliveryDay>()
    while (days.size < DELIVERY_DAY_COUNT) {
        val start = day.timeInMillis
        val slots = SLOT_START_HOURS.map { hour ->
            val slotStart = (day.clone() as Calendar).apply { set(Calendar.HOUR_OF_DAY, hour) }.timeInMillis
            DeliverySlot(hour, full = slotStart - nowMillis < HOUR_MS)
        }
        if (slots.any { !it.full }) days += DeliveryDay(start, slots)
        day.add(Calendar.DAY_OF_MONTH, 1)
    }
    return days
}
