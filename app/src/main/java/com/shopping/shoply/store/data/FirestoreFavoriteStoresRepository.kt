package com.shopping.shoply.store.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.shopping.shoply.common.data.firestore.liveSnapshots
import com.shopping.shoply.common.data.firestore.writeForCurrentUser
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.store.domain.FavoriteStoresRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Favorite stores in `users/{uid}/favoriteStores/{storeId}`: one document per favorite. */
class FirestoreFavoriteStoresRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : FavoriteStoresRepository {

    override fun observeIds(): Flow<Set<String>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptySet())
        return favorites(uid).liveSnapshots().map { snapshot -> snapshot.documents.map { it.id }.toSet() }
    }

    override suspend fun setFavorite(storeId: String, favorite: Boolean): Result<Unit, DataError> =
        auth.writeForCurrentUser { uid ->
            val document = favorites(uid).document(storeId)
            if (favorite) document.set(mapOf("addedAt" to FieldValue.serverTimestamp())) else document.delete()
        }

    private fun favorites(uid: String) = firestore.collection("users").document(uid).collection("favoriteStores")
}
