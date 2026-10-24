package com.shopping.shoply.common.domain.time

/**
 * Epoch millis of today's midnight on the device. Injected instead of read directly so tests can
 * pick "today".
 */
fun interface StartOfDayProvider {
    fun startOfTodayMillis(): Long
}
