package com.shopping.shoply.wishlist.domain

import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import kotlinx.coroutines.flow.Flow

/** The signed-in user's saved products. */
interface WishlistRepository {

    /**
     * Ids of the saved products. Emits the current set right away, then on every change (from any
     * screen or device). Never completes and never throws: when offline it keeps the last known set.
     */
    fun observeIds(): Flow<Set<String>>

    /** Saves [productId]. Saving one that's already saved is a no-op success. */
    suspend fun add(productId: String): Result<Unit, DataError>

    /** Un-saves [productId]. Removing one that isn't saved is a no-op success. */
    suspend fun remove(productId: String): Result<Unit, DataError>
}
