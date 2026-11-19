package com.shopping.shoply.editprofile.domain

import com.shopping.shoply.common.domain.Error
import com.shopping.shoply.common.domain.Result
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class DateOfBirthError : Error { IN_FUTURE }

private val UTC: TimeZone = TimeZone.getTimeZone("UTC")

/**
 * The date picked in the calendar as a `yyyy-MM-dd` string, or [DateOfBirthError.IN_FUTURE] (spec FR-021).
 * Material's date picker reports a day as UTC midnight, so it is compared with [todayUtcMidnight]
 * from [todayAsUtcMidnight], never with the current time.
 */
fun dateOfBirthFromPicker(pickedUtcMillis: Long, todayUtcMidnight: Long): Result<String, DateOfBirthError> =
    if (pickedUtcMillis > todayUtcMidnight) {
        Result.Error(DateOfBirthError.IN_FUTURE)
    } else {
        Result.Success(isoDateFormat().format(Date(pickedUtcMillis)))
    }

/**
 * The user's local calendar date today, as UTC midnight: the same form the date picker uses.
 * Comparing with this, not the current instant, keeps "today" correct in every time zone.
 */
fun todayAsUtcMidnight(nowMillis: Long, zone: TimeZone = TimeZone.getDefault()): Long {
    val local = Calendar.getInstance(zone).apply { timeInMillis = nowMillis }
    return Calendar.getInstance(UTC).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

/** UTC midnight of a stored `yyyy-MM-dd` date, so the calendar opens on it; null when it isn't one. */
fun parseDateOfBirth(isoDate: String): Long? =
    try {
        isoDateFormat().apply { isLenient = false }.parse(isoDate)?.time
    } catch (e: ParseException) {
        null
    }

// SimpleDateFormat instead of java.time: minSdk is 24 and the app doesn't enable desugaring.
private fun isoDateFormat() = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = UTC }
