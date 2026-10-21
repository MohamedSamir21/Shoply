package com.shopping.shoply.notifications.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shopping.shoply.common.data.firestore.liveSnapshots
import com.shopping.shoply.notifications.domain.NotificationsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Notifications in `users/{uid}/notifications`; counts the ones with `read == false`. */
class FirestoreNotificationsRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : NotificationsRepository {

    override fun observeUnreadCount(): Flow<Int> {
        val uid = auth.currentUser?.uid ?: return flowOf(0)
        return firestore.collection("users").document(uid).collection("notifications")
            .whereEqualTo("read", false)
            .liveSnapshots()
            .map { it.size() }
    }
}
