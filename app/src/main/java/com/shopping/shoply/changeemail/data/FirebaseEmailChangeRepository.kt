package com.shopping.shoply.changeemail.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.shopping.shoply.changeemail.domain.EmailChangeRepository
import com.shopping.shoply.changeemail.domain.EmailChangeStatus
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * [EmailChangeRepository] on Firebase Auth's `verifyBeforeUpdateEmail`. The pending address is kept
 * in `users/{uid}.pendingEmail` so Edit Profile can show it.
 */
class FirebaseEmailChangeRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : EmailChangeRepository {

    override fun currentEmail(): String? = auth.currentUser?.email

    override suspend fun pendingEmail(): String? {
        val uid = auth.currentUser?.uid ?: return null
        return try {
            user(uid).get().await().getString(FIELD_PENDING_EMAIL)
                ?.takeIf { it.isNotBlank() && !it.equals(currentEmail(), ignoreCase = true) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun requestEmailChange(newEmail: String, password: String): Result<Unit, DataError> {
        val user = auth.currentUser ?: return Result.Error(DataError.Unknown)
        val currentEmail = user.email ?: return Result.Error(DataError.Unknown)
        return firebaseCall {
            user.reauthenticate(EmailAuthProvider.getCredential(currentEmail, password)).await()
            user.verifyBeforeUpdateEmail(newEmail).await()
            // Local-first: Edit Profile sees it at once, Firestore syncs it when online.
            user(user.uid).set(mapOf(FIELD_PENDING_EMAIL to newEmail), SetOptions.merge())
        }
    }

    override suspend fun resendEmailChangeLink(newEmail: String): Result<Unit, DataError> {
        val user = auth.currentUser ?: return Result.Error(DataError.Unknown)
        return firebaseCall { user.verifyBeforeUpdateEmail(newEmail).await() }
    }

    override fun cancelEmailChange() {
        val uid = auth.currentUser?.uid ?: return
        user(uid).set(mapOf(FIELD_PENDING_EMAIL to FieldValue.delete()), SetOptions.merge())
    }

    override suspend fun checkEmailChange(newEmail: String): Result<EmailChangeStatus, DataError> {
        val user = auth.currentUser ?: return Result.Success(EmailChangeStatus.SIGNED_OUT)
        return try {
            user.reload().await()
            if (user.email.equals(newEmail, ignoreCase = true)) {
                user(user.uid).set(
                    mapOf("email" to newEmail, FIELD_PENDING_EMAIL to FieldValue.delete()),
                    SetOptions.merge()
                )
                Result.Success(EmailChangeStatus.CONFIRMED)
            } else {
                Result.Success(EmailChangeStatus.WAITING)
            }
        } catch (e: FirebaseAuthInvalidUserException) {
            // Changing the email revokes the session, so the reload is refused (FR-038).
            auth.signOut()
            Result.Success(EmailChangeStatus.SIGNED_OUT)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.toEmailChangeError())
        }
    }

    private inline fun firebaseCall(block: () -> Unit): Result<Unit, DataError> =
        try {
            block()
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.toEmailChangeError())
        }

    private fun user(uid: String) = firestore.collection("users").document(uid)

    private companion object {
        const val FIELD_PENDING_EMAIL = "pendingEmail"
    }
}

/** Maps a Firebase failure to the errors Change Email shows (FR-035). */
internal fun Exception.toEmailChangeError(): DataError = when (this) {
    is FirebaseNetworkException -> DataError.Network
    is FirebaseTooManyRequestsException -> DataError.Authentication.TOO_MANY_REQUESTS
    is FirebaseAuthException -> emailChangeErrorFor(errorCode)
    else -> DataError.Unknown
}

/** Firebase Auth error codes to [DataError]s. Newer SDKs report a wrong password as INVALID_CREDENTIAL. */
internal fun emailChangeErrorFor(errorCode: String?): DataError = when (errorCode) {
    "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL" -> DataError.Authentication.WRONG_PASSWORD
    "ERROR_EMAIL_ALREADY_IN_USE" -> DataError.Authentication.EMAIL_ALREADY_IN_USE
    "ERROR_INVALID_EMAIL" -> DataError.Authentication.INVALID_EMAIL
    "ERROR_REQUIRES_RECENT_LOGIN" -> DataError.Authentication.REQUIRES_RECENT_LOGIN
    "ERROR_TOO_MANY_REQUESTS" -> DataError.Authentication.TOO_MANY_REQUESTS
    else -> DataError.Unknown
}
