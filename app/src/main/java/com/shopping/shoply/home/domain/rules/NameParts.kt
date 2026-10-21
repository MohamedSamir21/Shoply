package com.shopping.shoply.home.domain.rules

fun nameParts(name: String): Pair<String, String?> {
    if (name.isBlank()) return "there" to null
    val trimmedName = name.trim()
    val firstName = trimmedName.substringBefore(" ")
    val firstCharFromFirstName = firstName.first().uppercase()
    if (trimmedName.count { it == ' ' } == 0) return firstName to firstCharFromFirstName
    val firstCharFromSecondName = trimmedName.substringAfter(" ").trim().first()
    val shortcut = firstCharFromFirstName + firstCharFromSecondName.uppercase()
    return firstName to shortcut
}