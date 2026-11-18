package com.shopping.shoply.editprofile.domain

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.common.domain.Result
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("DateOfBirthTest")
class DateOfBirthTest {
    // 2001-04-25T00:00:00Z and 2026-11-18T00:00:00Z
    private val april25th2001 = 988_156_800_000L
    private val today = 1_794_960_000_000L

    @Test
    @DisplayName("a past date is stored as yyyy-MM-dd")
    fun `a past date is stored as yyyy-MM-dd`() {
        assertThat(dateOfBirthFromPicker(april25th2001, nowUtcMillis = today))
            .isEqualTo(Result.Success<String, DateOfBirthError>("2001-04-25"))
    }

    @Test
    @DisplayName("today is allowed")
    fun `today is allowed`() {
        assertThat(dateOfBirthFromPicker(today, nowUtcMillis = today))
            .isEqualTo(Result.Success<String, DateOfBirthError>("2026-11-18"))
    }

    @Test
    @DisplayName("a future date is rejected")
    fun `a future date is rejected`() {
        val tomorrow = today + 24 * 60 * 60 * 1000L

        assertThat(dateOfBirthFromPicker(tomorrow, nowUtcMillis = today))
            .isEqualTo(Result.Error<String, DateOfBirthError>(DateOfBirthError.IN_FUTURE))
    }

    @Test
    @DisplayName("a stored date opens the calendar on that day")
    fun `a stored date opens the calendar on that day`() {
        assertThat(parseDateOfBirth("2001-04-25")).isEqualTo(april25th2001)
    }

    @Test
    @DisplayName("text that isn't a yyyy-MM-dd date is ignored")
    fun `text that isn't a yyyy-MM-dd date is ignored`() {
        assertThat(parseDateOfBirth("abc")).isNull()
        assertThat(parseDateOfBirth("2001-13-40")).isNull()
    }
}
