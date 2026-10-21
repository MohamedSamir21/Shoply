package com.shopping.shoply.home.data

import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.shopping.shoply.catalog.data.mapper.toCategory
import com.shopping.shoply.catalog.data.mapper.toProduct
import com.shopping.shoply.catalog.data.mapper.toStore
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.common.data.firestore.toReadError
import com.shopping.shoply.common.di.IoDispatcher
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.home.data.mapper.toPromo
import com.shopping.shoply.home.domain.model.HomeFeed
import com.shopping.shoply.home.domain.repository.HomeRepository
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
 * Reads the Home feed from Firestore: 4 reads in parallel, then the popular and recommended products
 * by id. Offline, Firestore answers from its local cache when it has one, so a feed seen before
 * still loads; with no cache the read fails with [DataError.Network].
 */
class FirestoreHomeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : HomeRepository {

    override suspend fun getFeed(): Result<HomeFeed, DataError> = withContext(ioDispatcher) {
        try {
            withTimeout(TIMEOUT_MS.milliseconds) {
                coroutineScope {
                    // Small collections: read all, filter in code, so no composite index is needed.
                    val promos = async { firestore.collection("promos").orderBy("order").get().await() }
                    val categories = async { firestore.collection("categories").orderBy("order").get().await() }
                    val stores = async { firestore.collection("stores").orderBy("order").get().await() }
                    val sections = async { firestore.collection("homeSections").document("main").get().await() }

                    val sectionsDoc = sections.await()
                    @Suppress("UNCHECKED_CAST")
                    val popularIds = sectionsDoc.get("popularProductIds") as? List<String> ?: emptyList()
                    @Suppress("UNCHECKED_CAST")
                    val recommendedIds = sectionsDoc.get("recommendedProductIds") as? List<String> ?: emptyList()
                    val productsById = productsByIds((popularIds + recommendedIds).distinct())

                    Result.Success(
                        HomeFeed(
                            promos = promos.await().documents
                                .filter { it.getBoolean("active") == true }
                                .mapNotNull { it.toPromo() },
                            categories = categories.await().documents.map { it.toCategory() },
                            featuredStores = stores.await().documents
                                .filter { it.getBoolean("featured") == true }
                                .map { it.toStore() },
                            // Keep the order of the id lists; ids with no product are skipped.
                            popular = popularIds.mapNotNull { productsById[it] },
                            recommended = recommendedIds.mapNotNull { productsById[it] },
                        )
                    )
                }
            }
        } catch (e: Throwable) {
            Result.Error(e.toReadError())
        }
    }

    // Firestore's "in" filter accepts at most 30 values, so ids are fetched in chunks of 30.
    private suspend fun productsByIds(ids: List<String>): Map<String, Product> = coroutineScope {
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
            .associateBy { it.id }
    }

    private companion object {
        const val TIMEOUT_MS = 5_000L
        const val MAX_IN_FILTER = 30
    }
}
