package com.shopping.shoply.changeemail.data

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.shopping.shoply.changeemail.domain.EmailChangeRepository
import com.shopping.shoply.changeemail.domain.EmailChangeStatus
import com.shopping.shoply.common.data.firebase.toAccountChangeError
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
        return firebaseCall {
            // Fresh email first: if another device changed it, the cached one would fail the
            // password check and read as "wrong password".
            user.reload().await()
            val currentEmail = user.email ?: throw IllegalStateException("Account has no email")
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
        val user = auth.currentUser ?: return Result.Success(EmailChangeStatus.SESSION_ENDED)
        return try {
            user.reload().await()
            if (user.email.equals(newEmail, ignoreCase = true)) {
                saveConfirmedEmail(user.uid, newEmail)
                Result.Success(EmailChangeStatus.CONFIRMED)
            } else {
                Result.Success(EmailChangeStatus.WAITING)
            }
        } catch (e: FirebaseAuthInvalidUserException) {
            // The session ended (FR-038). That may or may not be this change, so nothing is saved:
            // the profile picks up the real email after the next sign-in (FR-034).
            auth.signOut()
            Result.Success(EmailChangeStatus.SESSION_ENDED)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.toAccountChangeError())
        }
    }

    private inline fun firebaseCall(block: () -> Unit): Result<Unit, DataError> =
        try {
            block()
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.toAccountChangeError())
        }

    private fun saveConfirmedEmail(uid: String, email: String) =
        user(uid).set(mapOf("email" to email, FIELD_PENDING_EMAIL to FieldValue.delete()), SetOptions.merge())

    private fun user(uid: String) = firestore.collection("users").document(uid)

    private companion object {
        const val FIELD_PENDING_EMAIL = "pendingEmail"
    }
}
