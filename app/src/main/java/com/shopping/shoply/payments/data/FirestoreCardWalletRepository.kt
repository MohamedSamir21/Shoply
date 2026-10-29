package com.shopping.shoply.payments.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shopping.shoply.checkout.domain.model.CardBrand
import com.shopping.shoply.checkout.domain.model.PaymentCard
import com.shopping.shoply.common.data.firestore.writeForCurrentUser
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.payments.domain.CardWalletRepository
import javax.inject.Inject

/** `users/{uid}/paymentMethods/{id}`: brand ("Visa"), last4, expiry ("08/28"), primary — as the checkout reads them. */
class FirestoreCardWalletRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : CardWalletRepository {

    override suspend fun add(card: PaymentCard, demoteIds: List<String>): Result<String, DataError> {
        var newId = ""
        val result = auth.writeForCurrentUser { uid ->
            val cards = cards(uid)
            val document = cards.document()
            newId = document.id
            firestore.batch().apply {
                set(
                    document,
                    mapOf("brand" to card.brand.storedName(), "last4" to card.last4, "expiry" to card.expiry, "primary" to card.primary),
                )
                demoteIds.forEach { update(cards.document(it), "primary", false) }
            }.commit()
        }
        return when (result) {
            is Result.Success -> Result.Success(newId)
            is Result.Error -> Result.Error(result.error)
        }
    }

    override suspend fun setDefault(id: String, demoteIds: List<String>): Result<Unit, DataError> =
        auth.writeForCurrentUser { uid ->
            val cards = cards(uid)
            firestore.batch().apply {
                update(cards.document(id), "primary", true)
                demoteIds.forEach { update(cards.document(it), "primary", false) }
            }.commit()
        }

    override suspend fun remove(id: String, promoteId: String?): Result<Unit, DataError> =
        auth.writeForCurrentUser { uid ->
            val cards = cards(uid)
            firestore.batch().apply {
                delete(cards.document(id))
                if (promoteId != null) update(cards.document(promoteId), "primary", true)
            }.commit()
        }

    private fun CardBrand.storedName() = when (this) {
        CardBrand.VISA -> "Visa"
        CardBrand.MASTERCARD -> "Mastercard"
        CardBrand.OTHER -> "Card"
    }

    private fun cards(uid: String) = firestore.collection("users").document(uid).collection("paymentMethods")
}
