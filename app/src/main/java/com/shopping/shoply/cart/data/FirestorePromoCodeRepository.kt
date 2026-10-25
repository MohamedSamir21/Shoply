package com.shopping.shoply.cart.data

import com.google.firebase.firestore.FirebaseFirestore
import com.shopping.shoply.cart.domain.PromoCodeRepository
import com.shopping.shoply.common.data.firestore.int
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

/** Promo codes in `promoCodes/{CODE}` with `active` and `percentOff`. */
class FirestorePromoCodeRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PromoCodeRepository {

    override suspend fun percentOff(code: String): Result<Int?, DataError> = withContext(ioDispatcher) {
        // A blank code or one with "/" can't be a document id.
        if (code.isBlank() || '/' in code) return@withContext Result.Success(null)
        try {
            withTimeout(TIMEOUT_MS.milliseconds) {
                val snapshot = firestore.collection("promoCodes").document(code).get().await()
                val active = snapshot.exists() && snapshot.getBoolean("active") == true
                Result.Success(if (active) snapshot.int("percentOff") else null)
            }
        } catch (e: Throwable) {
            Result.Error(e.toReadError())
        }
    }

    private companion object {
        const val TIMEOUT_MS = 5_000L
    }
}
