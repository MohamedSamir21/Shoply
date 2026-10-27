package com.shopping.shoply.profile.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.shopping.shoply.common.data.firestore.liveSnapshots
import com.shopping.shoply.common.data.firestore.string
import com.shopping.shoply.profile.domain.ProfileRepository
import com.shopping.shoply.profile.domain.model.Account
import com.shopping.shoply.profile.domain.model.DeliveryAddress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Name, email and phone from the existing `users/{uid}` profile document; addresses from `users/{uid}/addresses`. */
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
            .map { snapshot -> snapshot.documents.firstOrNull()?.toAddress() }
    }

    // Sorted here, not in the query, so no composite index is needed.
    override fun observeAddresses(): Flow<List<DeliveryAddress>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return user(uid).collection("addresses").liveSnapshots().map { snapshot ->
            snapshot.documents.map { it.toAddress() }.sortedByDescending { it.primary }
        }
    }

    override fun observeAccount(): Flow<Account> {
        val uid = auth.currentUser?.uid ?: return flowOf(Account(name = "", email = "", phone = null))
        return user(uid).liveSnapshots().map {
            Account(
                name = it.string("username"),
                email = it.string("email"),
                phone = it.string("phoneNumber").ifBlank { null },
            )
        }
    }

    private fun DocumentSnapshot.toAddress() = DeliveryAddress(
        label = string("label"),
        line1 = string("line1"),
        id = id,
        recipientName = string("recipientName"),
        line2 = string("line2"),
        phone = string("phone"),
        primary = getBoolean("primary") == true,
    )

    private fun user(uid: String) = firestore.collection("users").document(uid)
}
