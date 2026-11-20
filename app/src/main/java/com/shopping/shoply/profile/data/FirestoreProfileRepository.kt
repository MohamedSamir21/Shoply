package com.shopping.shoply.profile.data

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.shopping.shoply.common.data.firestore.liveSnapshots
import com.shopping.shoply.common.data.firestore.string
import com.shopping.shoply.common.data.firestore.writeForCurrentUser
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.profile.domain.ProfileRepository
import com.shopping.shoply.profile.domain.model.Account
import com.shopping.shoply.profile.domain.model.DeliveryAddress
import com.shopping.shoply.profile.domain.model.UserInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Name, email and phone from the existing `users/{uid}` profile document; addresses from `users/{uid}/addresses`. */
class FirestoreProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val photoEncoder: PhotoEncoder
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
        val uid =
            auth.currentUser?.uid ?: return flowOf(
                Account(
                    name = "",
                    email = "",
                    phone = null,
                    photoUrl = null,
                    dateOfBirth = null
                )
            )
        return user(uid).liveSnapshots().map { doc ->
            // Firebase Auth owns the sign-in email; the document follows it after a change (FR-034).
            val authEmail = auth.currentUser?.email
            val docEmail = doc.string("email")
            if (authEmail != null && !authEmail.equals(docEmail, ignoreCase = true)) {
                user(uid).set(mapOf("email" to authEmail), SetOptions.merge())
            }
            Account(
                name = doc.string("username"),
                email = authEmail ?: docEmail,
                phone = doc.string("phoneNumber").ifBlank { null },
                photoUrl = doc.string("photoUrl").ifBlank { null },
                customPhoto = doc.string(ProfilePhoto.FIELD_BASE64).ifBlank { null }
                    ?.let(photoEncoder::decode),
                dateOfBirth = doc.string("dateOfBirth").ifBlank { null },
                emailVerified = auth.currentUser?.isEmailVerified == true,
                phoneVerified = auth.currentUser?.providerData.orEmpty()
                    .any { it.providerId == PhoneAuthProvider.PROVIDER_ID },
                pendingEmail = doc.string("pendingEmail").ifBlank { null }
                    ?.takeUnless { it.equals(authEmail, ignoreCase = true) },
                canChangeEmail = auth.currentUser?.providerData.orEmpty()
                    .any { it.providerId == EmailAuthProvider.PROVIDER_ID }
            )
        }
    }

    override suspend fun updateUserInfo(userInfo: UserInfo): Result<Unit, DataError> {
        val photoFields = userInfo.pickedPhotoUri?.let { uri ->
            val base64 = photoEncoder.encode(uri) ?: return Result.Error(DataError.Unknown)
            when (val fields = ProfilePhoto.fields(base64)) {
                is Result.Success -> fields.data
                is Result.Error -> return Result.Error(fields.error)
            }
        }
        val updates = profileUpdates(userInfo.name, userInfo.dateOfBirth, photoFields) +
            ("updatedAt" to FieldValue.serverTimestamp())
        return auth.writeForCurrentUser { uid ->
            user(uid).set(updates, SetOptions.merge())
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
