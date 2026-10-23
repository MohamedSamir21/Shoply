package com.shopping.shoply.catalog.data

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.shopping.shoply.catalog.data.mapper.toCategory
import com.shopping.shoply.catalog.data.mapper.toProduct
import com.shopping.shoply.catalog.domain.CatalogRepository
import com.shopping.shoply.catalog.domain.model.Category
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.common.data.firestore.toReadError
import com.shopping.shoply.common.di.IoDispatcher
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/**
 * Reads the `categories` and `products` collections. Offline, Firestore answers from its local
 * cache when it has one; with no cache the read times out as [DataError.Network].
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

    override suspend fun getProducts(ids: Set<String>): Result<List<Product>, DataError> = withContext(ioDispatcher) {
        if (ids.isEmpty()) return@withContext Result.Success(emptyList())
        try {
            withTimeout(TIMEOUT_MS.milliseconds) {
                // Firestore's "in" filter accepts at most 30 values, so ids are fetched in chunks of 30.
                val products = coroutineScope {
                    ids.chunked(MAX_IN_FILTER)
                        .map { chunk ->
                            async {
                                firestore.collection("products")
                                    .whereIn(FieldPath.documentId(), chunk)
                                    .get().await()
                                    .documents.map { it.toProduct() }
                            }
                        }
                        .awaitAll()
                        .flatten()
                }
                Result.Success(products)
            }
        } catch (e: Throwable) {
            Result.Error(e.toReadError())
        }
    }

    private companion object {
        const val TIMEOUT_MS = 5_000L
        const val MAX_IN_FILTER = 30
    }
}
