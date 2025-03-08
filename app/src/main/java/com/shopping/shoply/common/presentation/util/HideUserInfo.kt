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