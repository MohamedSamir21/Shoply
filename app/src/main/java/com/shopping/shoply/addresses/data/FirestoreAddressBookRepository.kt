package com.shopping.shoply.addresses.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.shopping.shoply.addresses.domain.AddressBookRepository
import com.shopping.shoply.common.data.firestore.writeForCurrentUser
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.profile.domain.model.DeliveryAddress
import javax.inject.Inject

/** `users/{uid}/addresses/{id}`, the same fields [com.shopping.shoply.profile.data.FirestoreProfileRepository] reads. */
class FirestoreAddressBookRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) : AddressBookRepository {

    override suspend fun save(address: DeliveryAddress, demoteIds: List<String>): Result<String, DataError> {
        var savedId = address.id
        val result = auth.writeForCurrentUser { uid ->
            val addresses = addresses(uid)
            val document = if (address.id.isBlank()) addresses.document() else addresses.document(address.id)
            savedId = document.id
            firestore.batch().apply {
                set(document, address.toFirestore())
                demoteIds.forEach { update(addresses.document(it), "primary", false) }
            }.commit()
        }
        return when (result) {
            is Result.Success -> Result.Success(savedId)
            is Result.Error -> Result.Error(result.error)
        }
    }

    override suspend fun delete(id: String, promoteId: String?): Result<Unit, DataError> =
        auth.writeForCurrentUser { uid ->
            val addresses = addresses(uid)
            firestore.batch().apply {
                delete(addresses.document(id))
                if (promoteId != null) update(addresses.document(promoteId), "primary", true)
            }.commit()
        }

    private fun DeliveryAddress.toFirestore() = mapOf(
        "label" to label,
        "recipientName" to recipientName,
        "line1" to line1,
        "line2" to line2,
        "phone" to phone,
        "primary" to primary,
    )

    private fun addresses(uid: String) = firestore.collection("users").document(uid).collection("addresses")
}
