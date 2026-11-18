package com.shopping.shoply.editprofile.domain

import com.shopping.shoply.common.domain.Error
import com.shopping.shoply.common.domain.Result
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class DateOfBirthError : Error { IN_FUTURE }

/**
 * The date picked in the calendar (UTC midnight, as Material's date picker reports it) as a
 * `yyyy-MM-dd` string, or [DateOfBirthError.IN_FUTURE] (spec FR-021).
 */
fun dateOfBirthFromPicker(pickedUtcMillis: Long, nowUtcMillis: Long): Result<String, DateOfBirthError> =
    if (pickedUtcMillis > nowUtcMillis) {
        Result.Error(DateOfBirthError.IN_FUTURE)
    } else {
        Result.Success(isoDateFormat().format(Date(pickedUtcMillis)))
    }

/** UTC midnight of a stored `yyyy-MM-dd` date, so the calendar opens on it; null when it isn't one. */
fun parseDateOfBirth(isoDate: String): Long? =
    try {
        isoDateFormat().apply { isLenient = false }.parse(isoDate)?.time
    } catch (e: ParseException) {
        null
    }

// SimpleDateFormat instead of java.time: minSdk is 24 and the app doesn't enable desugaring.
private fun isoDateFormat() = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
