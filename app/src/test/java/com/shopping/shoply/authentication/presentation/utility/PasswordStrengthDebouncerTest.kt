package com.shopping.shoply.authentication.presentation.utility

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.authentication.domain.model.PasswordStrength
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("PasswordStrengthDebouncer")
class PasswordStrengthDebouncerTest {

    @Test
    @DisplayName("emits NONE immediately for an empty password, without measuring")
    fun `emits NONE immediately for an empty password, without measuring`() = runTest {
        var passwordStrength = PasswordStrength.FAIR
        val sut = PasswordStrengthDebouncer(
            scope = this,
            // Fails loudly if the debouncer ever schedules a measurement for empty input —
            // otherwise a measured NONE would be indistinguishable from the short-circuit NONE.
            measureStrength = { error("must not measure an empty password") },
            onStrengthChanged = { passwordStrength = it }
        )

        sut.measure("")
        advanceUntilIdle()

        assertThat(passwordStrength).isEqualTo(PasswordStrength.NONE)
    }

    @Test
    @DisplayName("emits the measured strength once the debounce window elapses")
    fun `emits the measured strength once the debounce window elapses`() = runTest {
        var passwordStrength = PasswordStrength.NONE
        val sut = PasswordStrengthDebouncer(
            scope = this,
            measureStrength = { _ -> PasswordStrength.GOOD },
            onStrengthChanged = { passwordStrength = it }
        )

        sut.measure("abc123")
        advanceUntilIdle()

        assertThat(passwordStrength).isEqualTo(PasswordStrength.GOOD)
    }

    @Test
    @DisplayName("measures only the last password when a new one arrives inside the debounce window")
    fun `measures only the last password when a new one arrives inside the debounce window`() =
        runTest {
            var passwordStrength = PasswordStrength.NONE
            var callsCount = 0
            val sut = PasswordStrengthDebouncer(
                scope = this,
                // Argument-sensitive: measuring the stale password would surface as WEAK, so the
                // assertion pins WHICH keystroke survived, not just that one emission happened.
                measureStrength = { password ->
                    if (password == "abc123") PasswordStrength.GOOD else PasswordStrength.WEAK
                },
                onStrengthChanged = { strength ->
                    callsCount++
                    passwordStrength = strength
                }
            )

            sut.measure("abc12")
            advanceTimeBy(100)
            sut.measure("abc123")
            advanceUntilIdle()

            assertThat(passwordStrength).isEqualTo(PasswordStrength.GOOD)
            assertThat(callsCount).isEqualTo(1)
        }

    @Test
    @DisplayName("clearing the password mid-debounce cancels the pending measurement")
    fun `clearing the password mid-debounce cancels the pending measurement`() = runTest {
        var passwordStrength = PasswordStrength.FAIR
        val sut = PasswordStrengthDebouncer(
            scope = this,
            measureStrength = { _ -> PasswordStrength.GOOD },
            onStrengthChanged = { passwordStrength = it }
        )

        sut.measure("abc123")
        advanceTimeBy(100)
        sut.measure("") // must kill the pending measurement AND emit NONE

        advanceUntilIdle() // if cancellation broke, the old job fires here and emits GOOD

        assertThat(passwordStrength).isEqualTo(PasswordStrength.NONE)
    }

    @Test
    @DisplayName("waits exactly 200ms before measuring")
    fun `waits exactly 200ms before measuring`() = runTest {
        var passwordStrength = PasswordStrength.NONE
        val sut = PasswordStrengthDebouncer(
            scope = this,
            measureStrength = { _ -> PasswordStrength.GOOD },
            onStrengthChanged = { passwordStrength = it }
        )

        sut.measure("abc123")

        // advanceTimeBy stops just short of its target, so t=199 leaves the measurement
        // (scheduled at t=200) unrun; the next 1ms + runCurrent() lands exactly on it.
        advanceTimeBy(199)
        runCurrent()
        assertThat(passwordStrength).isEqualTo(PasswordStrength.NONE)

        advanceTimeBy(1)
        runCurrent()
        assertThat(passwordStrength).isEqualTo(PasswordStrength.GOOD)
    }
}
