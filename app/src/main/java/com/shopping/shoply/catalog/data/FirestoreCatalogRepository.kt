package com.shopping.shoply.catalog.data

import com.google.firebase.firestore.FirebaseFirestore
import com.shopping.shoply.catalog.data.mapper.toCategory
import com.shopping.shoply.catalog.domain.CatalogRepository
import com.shopping.shoply.catalog.domain.model.Category
import com.shopping.shoply.common.data.firestore.toReadError
import com.shopping.shoply.common.di.IoDispatcher
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/**
 * Reads the `categories` collection, ordered by its `order` field. Offline, Firestore answers from
 * its local cache when it has one; with no cache the read times out as [DataError.Network].
 */
class FirestoreCatalogRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CatalogRepository {

    override suspend fun getCategories(): Result<List<Category>, DataError> = withContext(ioDispatcher) {
        try {
            withTimeout(TIMEOUT_MS.milliseconds) {
                val snapshot = firestore.collection("categories").orderBy("order").get().await()
                Result.Success(snapshot.documents.map { it.toCategory() })
            }
        } catch (e: Throwable) {
            Result.Error(e.toReadError())
        }
    }

    private companion object {
        const val TIMEOUT_MS = 5_000L
    }
}
