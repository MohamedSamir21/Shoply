package com.shopping.shoply.checkout.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shopping.shoply.checkout.domain.PaymentRepository
import com.shopping.shoply.checkout.domain.model.CardBrand
import com.shopping.shoply.checkout.domain.model.PaymentCard
import com.shopping.shoply.common.data.firestore.liveSnapshots
import com.shopping.shoply.common.data.firestore.string
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Saved cards in `users/{uid}/paymentMethods`: brand ("Visa"), last4, expiry ("08/28"), primary. */
class FirestorePaymentRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : PaymentRepository {

    override fun observeCards(): Flow<List<PaymentCard>> {
        val uid = auth.currentUser?.uid ?: return flowOf(emptyList())
        return firestore.collection("users").document(uid).collection("paymentMethods").liveSnapshots()
            .map { snapshot ->
                snapshot.documents.map {
                    PaymentCard(
                        id = it.id,
                        brand = brandOf(it.string("brand")),
                        last4 = it.string("last4"),
                        expiry = it.string("expiry"),
                        primary = it.getBoolean("primary") == true,
                    )
                }.sortedByDescending { it.primary }
            }
    }

    private fun brandOf(name: String) = when (name.lowercase()) {
        "visa" -> CardBrand.VISA
        "mastercard" -> CardBrand.MASTERCARD
        else -> CardBrand.OTHER
    }
}
