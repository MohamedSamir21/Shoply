package com.shopping.shoply.notifications.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.shopping.shoply.common.data.firestore.liveSnapshots
import com.shopping.shoply.common.data.firestore.string
import com.shopping.shoply.common.data.firestore.toReadError
import com.shopping.shoply.common.data.firestore.writeForCurrentUser
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.notifications.domain.NotificationsRepository
import com.shopping.shoply.notifications.domain.model.Notification
import com.shopping.shoply.notifications.domain.model.NotificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/**
 * Notifications in `users/{uid}/notifications`. Fields: `type` (e.g. "ORDER_ON_THE_WAY"), `title`,
 * `body`, `createdAt` (timestamp), `read`, `targetId` (optional).
 */
class FirestoreNotificationsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : NotificationsRepository {

    override fun observeUnreadCount(): Flow<Int> {
        val uid = auth.currentUser?.uid ?: return flowOf(0)
        return notifications(uid)
            .whereEqualTo("read", false)
            .liveSnapshots()
            .map { it.size() }
    }

    override fun observeNotifications(): Flow<List<Notification>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return notifications(uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .liveSnapshots()
            .map { snapshot -> snapshot.documents.map { it.toNotification() } }
    }

    override suspend fun markRead(id: String): Result<Unit, DataError> = auth.writeForCurrentUser { uid ->
        notifications(uid).document(id).update("read", true)
    }

    // Reads the unread ones (cache first when offline), then flips them all in one local-first batch.
    override suspend fun markAllRead(): Result<Unit, DataError> {
        val uid = auth.currentUser?.uid ?: return Result.Error(DataError.Unknown)
        val unread = try {
            withTimeout(TIMEOUT_MS.milliseconds) {
                notifications(uid).whereEqualTo("read", false).get().await()
            }
        } catch (e: Throwable) {
            return Result.Error(e.toReadError())
        }
        return auth.writeForCurrentUser {
            firestore.batch().apply {
                unread.documents.forEach { update(it.reference, "read", true) }
            }.commit()
        }
    }

    private fun notifications(uid: String) =
        firestore.collection("users").document(uid).collection("notifications")

    private fun DocumentSnapshot.toNotification() = Notification(
        id = id,
        type = NotificationType.entries.firstOrNull { it.name.equals(getString("type"), ignoreCase = true) }
            ?: NotificationType.GENERAL,
        title = string("title"),
        body = string("body"),
        createdAtMillis = getTimestamp("createdAt")?.toDate()?.time ?: 0L,
        read = getBoolean("read") ?: false,
        targetId = getString("targetId"),
    )

    private companion object {
        const val TIMEOUT_MS = 5_000L
    }
}
