package com.shopping.shoply.settings.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shopping.shoply.settings.domain.SettingsRepository
import com.shopping.shoply.settings.domain.model.AppSettings
import com.shopping.shoply.settings.domain.model.NotificationType
import com.shopping.shoply.settings.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

private val Context.settingsDataStore by preferencesDataStore(name = "settings")
private val THEME_KEY = stringPreferencesKey("theme")
private fun notificationKey(type: NotificationType) = booleanPreferencesKey("notify_${type.name.lowercase()}")

/** Settings in a Preferences DataStore on the device. Unknown or missing values read as the defaults. */
class DataStoreSettingsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : SettingsRepository {

    override fun observe(): Flow<AppSettings> = context.settingsDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences ->
            AppSettings(
                theme = ThemeMode.entries.firstOrNull { it.name == preferences[THEME_KEY] } ?: ThemeMode.SYSTEM,
                notifications = NotificationType.entries.associateWith { type ->
                    preferences[notificationKey(type)] ?: AppSettings.DEFAULT_NOTIFICATIONS.getValue(type)
                },
            )
        }

    override suspend fun setTheme(mode: ThemeMode) = write { it[THEME_KEY] = mode.name }

    override suspend fun setNotification(type: NotificationType, enabled: Boolean) = write { it[notificationKey(type)] = enabled }

    private suspend fun write(change: (androidx.datastore.preferences.core.MutablePreferences) -> Unit) {
        try {
            context.settingsDataStore.edit { change(it) }
        } catch (_: IOException) {
            // A failed write keeps the old value, which the screen then shows again.
        }
    }
}
