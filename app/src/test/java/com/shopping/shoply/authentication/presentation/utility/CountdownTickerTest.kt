package com.shopping.shoply.authentication.presentation.utility

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("CountdownTicker")
class CountdownTickerTest {

    @Test
    @DisplayName("ticks down second by second")
    fun `ticks down second by second`() = runTest {
        var remaining: Long = 60000
        val sut = CountdownTicker(
            scope = this,
            durationMillis = 60000,
            intervalMillis = 1000,
            onTick = { remaining = it },
            onFinish = {}
        )

        sut.start()

        // No tick fires at start — the first onTick only lands after the first interval.
        runCurrent()
        assertThat(remaining).isEqualTo(60000)

        advanceTimeBy(1000)
        runCurrent()
        assertThat(remaining).isEqualTo(59000)

        advanceTimeBy(1000)
        runCurrent()
        assertThat(remaining).isEqualTo(58000)
    }

    @Test
    @DisplayName("triggers the onFinish when the countdown finishes")
    fun `triggers the onFinish when the countdown finishes`() = runTest {
        var remaining: Long = 60000
        var callsCount = 0
        val onFinished = { callsCount += 1 }
        val sut = CountdownTicker(
            scope = this,
            durationMillis = 60000,
            intervalMillis = 1000,
            onTick = { remaining = it },
            onFinish = onFinished
        )

        sut.start()
        advanceTimeBy(59000)
        runCurrent()
        assertThat(remaining).isEqualTo(1000)
        assertThat(callsCount).isEqualTo(0)

        advanceTimeBy(1000)
        runCurrent()
        assertThat(callsCount).isEqualTo(1)
        // onFinish REPLACES the final tick — onTick(0) must never fire.
        assertThat(remaining).isEqualTo(1000)
    }

    @Test
    @DisplayName("restarts the countdown when the user starts the ticker again")
    fun `restarts the countdown when the user starts the ticker again`() = runTest {
        var remaining: Long = 60000
        var callsCount = 0
        val onFinished = { callsCount += 1 }
        val sut = CountdownTicker(
            scope = this,
            durationMillis = 60000,
            intervalMillis = 1000,
            onTick = { remaining = it },
            onFinish = onFinished
        )

        sut.start()
        advanceTimeBy(30000)
        runCurrent()
        assertThat(remaining).isEqualTo(30000)

        sut.start()

        // A fresh 60s run: the first tick after the restart reads 59000, not 29000.
        advanceTimeBy(1000)
        runCurrent()
        assertThat(remaining).isEqualTo(59000)

        // And only ONE finish ever fires — a lingering, uncancelled first run would fire twice.
        advanceUntilIdle()
        assertThat(callsCount).isEqualTo(1)
    }
}