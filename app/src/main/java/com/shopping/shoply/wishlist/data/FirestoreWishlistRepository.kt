package com.shopping.shoply.wishlist.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.shopping.shoply.common.data.firestore.liveSnapshots
import com.shopping.shoply.common.data.firestore.writeForCurrentUser
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.wishlist.domain.WishlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Wishlist in `users/{uid}/wishlist/{productId}`: one document per saved product. */
class FirestoreWishlistRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : WishlistRepository {

    override fun observeIds(): Flow<Set<String>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptySet())
        return wishlist(uid).liveSnapshots().map { snapshot -> snapshot.documents.map { it.id }.toSet() }
    }

    override suspend fun add(productId: String): Result<Unit, DataError> = auth.writeForCurrentUser { uid ->
        wishlist(uid).document(productId).set(mapOf("addedAt" to FieldValue.serverTimestamp()))
    }

    override suspend fun remove(productId: String): Result<Unit, DataError> = auth.writeForCurrentUser { uid ->
        wishlist(uid).document(productId).delete()
    }

    private fun wishlist(uid: String) = firestore.collection("users").document(uid).collection("wishlist")
}
