package com.shopping.shoply.search.domain

import kotlinx.coroutines.flow.Flow

/** The searches made on this device, newest first. Never fails: a broken store reads as empty. */
interface RecentSearchesRepository {

    fun observe(): Flow<List<String>>

    /** Replaces the whole list (see [com.shopping.shoply.search.domain.rules.withRecent]). */
    suspend fun save(queries: List<String>)
}
