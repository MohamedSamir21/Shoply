package com.shopping.shoply.cart.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.shopping.shoply.cart.domain.CartRepository
import com.shopping.shoply.catalog.domain.model.Product
import com.shopping.shoply.catalog.domain.model.Variant
import com.shopping.shoply.common.data.firestore.int
import com.shopping.shoply.common.data.firestore.liveSnapshots
import com.shopping.shoply.common.data.firestore.writeForCurrentUser
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Cart in `users/{uid}/cart/{productId_color_size}`: one document per product variant. */
class FirestoreCartRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : CartRepository {

    override fun observeTotalQuantity(): Flow<Int> {
        val uid = auth.currentUser?.uid ?: return flowOf(0)
        return cart(uid).liveSnapshots().map { snapshot -> snapshot.documents.sumOf { it.int("quantity") } }
    }

    // The variant is part of the document id, so adding the same variant again lands on the same
    // document. merge + increment then adds to its quantity atomically, even offline.
    override suspend fun add(product: Product, variant: Variant, quantity: Int): Result<Unit, DataError> =
        auth.writeForCurrentUser { uid ->
            cart(uid).document(itemId(product.id, variant)).set(
                mapOf(
                    "productId" to product.id,
                    "name" to product.name,
                    "icon" to product.icon.name.lowercase(),
                    "storeId" to product.storeId,
                    "priceCents" to product.priceCents,
                    "color" to variant.color,
                    "size" to variant.size,
                    "quantity" to FieldValue.increment(quantity.toLong()),
                    "addedAt" to FieldValue.serverTimestamp(),
                ),
                SetOptions.merge(),
            )
        }

    private fun cart(uid: String) = firestore.collection("users").document(uid).collection("cart")

    // e.g. "p2_Oat_M". "/" would split the Firestore path, so it's replaced.
    private fun itemId(productId: String, variant: Variant) =
        listOfNotNull(productId, variant.color, variant.size).joinToString("_").replace("/", "-")
}
