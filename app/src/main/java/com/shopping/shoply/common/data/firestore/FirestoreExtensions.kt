package com.shopping.shoply.common.data.firestore

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.cancellation.CancellationException

// Live listeners that match the domain contract "never completes, never throws": a listener error
// (e.g. permission denied) is swallowed, so collectors keep the last value instead of crashing.
// Firestore serves cached data first, so these also work offline.

fun Query.liveSnapshots(): Flow<QuerySnapshot> = callbackFlow {
    val registration = addSnapshotListener { snapshot, _ -> snapshot?.let { trySend(it) } }
    awaitClose { registration.remove() }
}

fun DocumentReference.liveSnapshots(): Flow<DocumentSnapshot> = callbackFlow {
    val registration = addSnapshotListener { snapshot, _ -> snapshot?.let { trySend(it) } }
    awaitClose { registration.remove() }
}

/**
 * Runs a local-first write for the signed-in user. It does NOT wait for the server: Firestore queues
 * the write locally (listeners see it instantly) and syncs it when online. Waiting would hang offline.
 *
 * @return [DataError.Unknown] when nobody is signed in or the write is rejected locally.
 */
inline fun FirebaseAuth.writeForCurrentUser(write: (uid: String) -> Unit): Result<Unit, DataError> {
    val uid = currentUser?.uid ?: return Result.Error(DataError.Unknown)
    return try {
        write(uid)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DataError.Unknown)
    }
}

/** Maps a failed read to the domain error. Rethrows cancellation so coroutines cancel properly. */
fun Throwable.toReadError(): DataError = when (this) {
    is TimeoutCancellationException -> DataError.Network
    is CancellationException -> throw this
    is FirebaseFirestoreException ->
        if (code == FirebaseFirestoreException.Code.UNAVAILABLE) DataError.Network else DataError.Unknown
    else -> DataError.Unknown
}

fun DocumentSnapshot.int(field: String): Int = getLong(field)?.toInt() ?: 0
fun DocumentSnapshot.long(field: String): Long = getLong(field) ?: 0L
fun DocumentSnapshot.double(field: String): Double = getDouble(field) ?: 0.0
fun DocumentSnapshot.string(field: String): String = getString(field).orEmpty()
