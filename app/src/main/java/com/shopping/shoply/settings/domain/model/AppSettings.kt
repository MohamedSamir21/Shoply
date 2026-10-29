package com.shopping.shoply.settings.domain.model

enum class ThemeMode { LIGHT, DARK, SYSTEM }

enum class NotificationType { ORDER_UPDATES, OFFERS, PRICE_DROPS }

/** The Settings screen's choices. Defaults match the design: offers off, the rest on. */
data class AppSettings(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val notifications: Map<NotificationType, Boolean> = DEFAULT_NOTIFICATIONS,
) {
    fun isOn(type: NotificationType): Boolean = notifications[type] ?: DEFAULT_NOTIFICATIONS.getValue(type)

    companion object {
        val DEFAULT_NOTIFICATIONS = mapOf(
            NotificationType.ORDER_UPDATES to true,
            NotificationType.OFFERS to false,
            NotificationType.PRICE_DROPS to true,
        )
    }
}
