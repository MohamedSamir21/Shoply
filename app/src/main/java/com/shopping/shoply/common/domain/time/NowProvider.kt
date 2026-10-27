package com.shopping.shoply.common.domain.time

/** The current time in epoch millis. Injected instead of read directly so tests can pick "now". */
fun interface NowProvider {
    fun nowMillis(): Long
}
