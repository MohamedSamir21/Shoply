package com.shopping.shoply.catalog.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.shopping.shoply.catalog.data.mapper.toCategory
import com.shopping.shoply.catalog.data.mapper.toProduct
import com.shopping.shoply.catalog.data.mapper.toStore
import com.shopping.shoply.catalog.domain.CatalogRepository
import com.shopping.shoply.catalog.domain.model.Category
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.model.Store
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

    override suspend fun getProduct(id: String): Result<Product, DataError> =
        readDocument("products", id) { it.toProduct() }

    override suspend fun getStore(id: String): Result<Store, DataError> =
        readDocument("stores", id) { it.toStore() }

    override suspend fun getProductsInCategory(categoryId: String, limit: Int): Result<List<Product>, DataError> =
        productsWhere("categoryId", categoryId, limit)

    override suspend fun getProductsInStore(storeId: String): Result<List<Product>, DataError> =
        productsWhere("storeId", storeId, limit = Int.MAX_VALUE)

    override suspend fun getAllProducts(): Result<List<Product>, DataError> =
        readCollection("products") { it.toProduct() }

    override suspend fun getAllStores(): Result<List<Store>, DataError> =
        readCollection("stores") { it.toStore() }

    // A whole collection in display order. The catalog is small, so it's read in one go.
    private suspend fun <T> readCollection(
        collection: String,
        map: (DocumentSnapshot) -> T,
    ): Result<List<T>, DataError> = withContext(ioDispatcher) {
        try {
            withTimeout(TIMEOUT_MS.milliseconds) {
                val snapshot = firestore.collection(collection).orderBy("order").get().await()
                Result.Success(snapshot.documents.map(map))
            }
        } catch (e: Throwable) {
            Result.Error(e.toReadError())
        }
    }

    // Products whose [field] equals [value], in display order.
    private suspend fun productsWhere(field: String, value: String, limit: Int): Result<List<Product>, DataError> =
        withContext(ioDispatcher) {
            try {
                withTimeout(TIMEOUT_MS.milliseconds) {
                    // Sorted here: whereEqualTo + orderBy on another field would need a composite index.
                    val snapshot = firestore.collection("products")
                        .whereEqualTo(field, value)
                        .get().await()
                    val products = snapshot.documents
                        .sortedBy { it.getLong("order") ?: Long.MAX_VALUE }
                        .take(limit)
                        .map { it.toProduct() }
                    Result.Success(products)
                }
            } catch (e: Throwable) {
                Result.Error(e.toReadError())
            }
        }

    // A document that doesn't exist is DataError.Unknown.
    private suspend fun <T> readDocument(
        collection: String,
        id: String,
        map: (DocumentSnapshot) -> T,
    ): Result<T, DataError> = withContext(ioDispatcher) {
        try {
            withTimeout(TIMEOUT_MS.milliseconds) {
                val snapshot = firestore.collection(collection).document(id).get().await()
                if (snapshot.exists()) Result.Success(map(snapshot)) else Result.Error(DataError.Unknown)
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
