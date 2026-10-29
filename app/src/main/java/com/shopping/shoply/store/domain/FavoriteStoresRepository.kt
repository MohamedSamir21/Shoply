package com.shopping.shoply.store.domain

import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import kotlinx.coroutines.flow.Flow

/** The signed-in user's favorite stores. */
interface FavoriteStoresRepository {

    /**
     * Ids of the favorite stores. Emits the current set right away, then on every change. Never
     * completes and never throws: when offline it keeps the last known set.
     */
    fun observeIds(): Flow<Set<String>>

    /** Adds [storeId] to the favorites when [favorite] is true, removes it otherwise. Repeating is a no-op success. */
    suspend fun setFavorite(storeId: String, favorite: Boolean): Result<Unit, DataError>
}
