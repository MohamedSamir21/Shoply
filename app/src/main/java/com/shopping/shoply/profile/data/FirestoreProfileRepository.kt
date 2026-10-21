package com.shopping.shoply.profile.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shopping.shoply.common.data.firestore.liveSnapshots
import com.shopping.shoply.common.data.firestore.string
import com.shopping.shoply.profile.domain.ProfileRepository
import com.shopping.shoply.profile.domain.model.DeliveryAddress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Name from the existing `users/{uid}` profile document; addresses from `users/{uid}/addresses`. */
class FirestoreProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : ProfileRepository {

    override fun observeDisplayName(): Flow<String> {
        val uid = auth.currentUser?.uid ?: return flowOf("")
        return user(uid).liveSnapshots().map { it.string("username") }
    }

    override fun observePrimaryAddress(): Flow<DeliveryAddress?> {
        val uid = auth.currentUser?.uid ?: return flowOf(null)
        return user(uid).collection("addresses")
            .whereEqualTo("primary", true)
            .limit(1)
            .liveSnapshots()
            .map { snapshot ->
                snapshot.documents.firstOrNull()?.let { DeliveryAddress(it.string("label"), it.string("line1")) }
            }
    }

    private fun user(uid: String) = firestore.collection("users").document(uid)
}
