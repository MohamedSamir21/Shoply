package com.shopping.shoply.search.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.shopping.shoply.search.domain.RecentSearchesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

private val Context.searchDataStore by preferencesDataStore(name = "search")
private val RECENT_KEY = stringPreferencesKey("recent")

/** Recent searches in a Preferences DataStore on the device, one query per line. */
class DataStoreRecentSearchesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : RecentSearchesRepository {

    override fun observe(): Flow<List<String>> = context.searchDataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences -> preferences[RECENT_KEY]?.split('\n')?.filter { it.isNotBlank() }.orEmpty() }

    override suspend fun save(queries: List<String>) {
        try {
            context.searchDataStore.edit { it[RECENT_KEY] = queries.joinToString("\n") }
        } catch (_: IOException) {
            // Recent searches are a convenience; a failed write is dropped.
        }
    }
}
