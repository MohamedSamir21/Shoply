package com.shopping.shoply.settings.domain.rules

import com.shopping.shoply.settings.domain.model.ThemeMode

/** Whether to draw the dark theme: [ThemeMode.SYSTEM] follows the device. */
fun isDarkTheme(mode: ThemeMode, systemDark: Boolean): Boolean = when (mode) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> systemDark
}

/** "ahmed@gmail.com" → "**med@gmail.com": hides all but the last 3 characters before the @. */
fun maskEmail(email: String): String {
    val local = email.substringBefore('@', missingDelimiterValue = "")
    if (local.isEmpty()) return email
    val shown = local.takeLast(3)
    return "*".repeat(local.length - shown.length) + shown + "@" + email.substringAfter('@')
}
