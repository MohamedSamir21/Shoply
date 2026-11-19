package com.shopping.shoply.editprofile.domain

import com.google.common.truth.Truth.assertThat
import com.shopping.shoply.common.domain.Result
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.TimeZone

@DisplayName("DateOfBirthTest")
class DateOfBirthTest {
    // All values are UTC instants.
    private val april25th2001 = 988_156_800_000L        // 2001-04-25T00:00Z
    private val november18th = 1_794_960_000_000L       // 2026-11-18T00:00Z
    private val november19th = 1_795_046_400_000L       // 2026-11-19T00:00Z
    private val november18th2300Utc = 1_795_042_800_000L // 2026-11-18T23:00Z
    private val november19th0100Utc = 1_795_050_000_000L // 2026-11-19T01:00Z

    @Test
    @DisplayName("a past date is stored as yyyy-MM-dd")
    fun `a past date is stored as yyyy-MM-dd`() {
        assertThat(dateOfBirthFromPicker(april25th2001, todayUtcMidnight = november18th))
            .isEqualTo(Result.Success<String, DateOfBirthError>("2001-04-25"))
    }

    @Test
    @DisplayName("today is allowed")
    fun `today is allowed`() {
        assertThat(dateOfBirthFromPicker(november18th, todayUtcMidnight = november18th))
            .isEqualTo(Result.Success<String, DateOfBirthError>("2026-11-18"))
    }

    @Test
    @DisplayName("tomorrow is rejected")
    fun `tomorrow is rejected`() {
        assertThat(dateOfBirthFromPicker(november19th, todayUtcMidnight = november18th))
            .isEqualTo(Result.Error<String, DateOfBirthError>(DateOfBirthError.IN_FUTURE))
    }

    @Test
    @DisplayName("just after midnight in Cairo, today is already the new day")
    fun `just after midnight in Cairo, today is already the new day`() {
        // 01:00 on Nov 19 in Cairo (UTC+2) is still Nov 18 in UTC.
        val today = todayAsUtcMidnight(november18th2300Utc, TimeZone.getTimeZone("Africa/Cairo"))

        assertThat(today).isEqualTo(november19th)
    }

    @Test
    @DisplayName("in the evening in New York, tomorrow is not yet today")
    fun `in the evening in New York, tomorrow is not yet today`() {
        // 20:00 on Nov 18 in New York (UTC-5) is already Nov 19 in UTC.
        val today = todayAsUtcMidnight(november19th0100Utc, TimeZone.getTimeZone("America/New_York"))

        assertThat(today).isEqualTo(november18th)
        assertThat(dateOfBirthFromPicker(november19th, today))
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
