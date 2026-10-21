package com.shopping.shoply.common.domain.time

/**
 * The current hour of the day (0–23) on the device. Injected instead of read directly so tests
 * can pick the time (e.g. `CurrentHourProvider { 9 }` for "Good morning").
 */
fun interface CurrentHourProvider {
    fun currentHour(): Int
}
