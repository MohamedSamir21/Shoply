package com.shopping.shoply.settings.domain

import com.shopping.shoply.settings.domain.model.AppSettings
import com.shopping.shoply.settings.domain.model.NotificationType
import com.shopping.shoply.settings.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/** App preferences kept on this device. Never fails: an unreadable store reads as the defaults. */
interface SettingsRepository {

    /** Emits the current settings right away, then on every change. */
    fun observe(): Flow<AppSettings>

    suspend fun setTheme(mode: ThemeMode)

    suspend fun setNotification(type: NotificationType, enabled: Boolean)
}
