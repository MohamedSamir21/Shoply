package com.shopping.shoply.common.presentation.util

fun hidePartOfEmailWithStars(email: String): String {
    if (email.isEmpty())
        return ""
    val usernamePart = email.substringBefore("@")
    val domainPart = email.substringAfter("@")
    val userNamePartWithHalfStars =
        usernamePart.replaceRange(
            0,
            usernamePart.length / 2,
            "*".repeat(usernamePart.length / 2)
        )
    val emailWithStars = userNamePartWithHalfStars.plus("@").plus(domainPart)
    return emailWithStars
}

fun hidePartOfPhoneNumberWithStars(phoneNumber: String): String {
    if (phoneNumber.isEmpty())
        return ""
    return phoneNumber.replaceRange(
        0,
        phoneNumber.length / 2,
        "*".repeat(phoneNumber.length / 2)
    )
}

/**
 * Masks a phone number for the OTP screen, keeping only the last [visibleCount]
 * digits and prefixing a '+' with [countryCode] after, e.g. `15551234590` -> `+1********90`. Mirrors the
 * handoff design's `+1 *** *** **90` masked number.
 */
fun maskPhoneNumberKeepingLast(
    countryCode: String,
    phoneNumber: String,
    visibleCount: Int = 2
): String {
    val digits = phoneNumber.filter(Char::isDigit)
    if (digits.isEmpty())
        return ""
    if (digits.length <= visibleCount)
        return "+$digits"
    val masked = "*".repeat(digits.length - visibleCount)
    return "+$countryCode$masked${digits.takeLast(visibleCount)}"
}