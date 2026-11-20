package com.shopping.shoply.changephone.data

import com.google.firebase.FirebaseException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.shopping.shoply.changephone.domain.PhoneChangeRepository
import com.shopping.shoply.changephone.domain.PhoneCodeEvent
import com.shopping.shoply.common.data.firebase.toAccountChangeError
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.platform.ActivityProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [PhoneChangeRepository] on Firebase phone auth. Keeps the verification id between [sendCode]
 * and [verifyCode], so it is a singleton. Phone numbers are never logged.
 */
@Singleton
class FirebasePhoneChangeRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val activityProvider: ActivityProvider
) : PhoneChangeRepository {
    private var verificationId: String? = null
    private var codeSentTo: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    /** Bumped on every send, so callbacks from an older request (another number) are ignored. */
    private var activeRequest = 0

    override fun currentPhone(): String? = auth.currentUser?.phoneNumber?.ifBlank { null }

    override fun needsPassword(): Boolean =
        auth.currentUser?.providerData.orEmpty().any { it.providerId == EmailAuthProvider.PROVIDER_ID }

    override suspend fun confirmPassword(password: String): Result<Unit, DataError> {
        val user = auth.currentUser ?: return Result.Error(DataError.Unknown)
        return try {
            user.reload().await()
            val email = user.email ?: return Result.Error(DataError.Unknown)
            user.reauthenticate(EmailAuthProvider.getCredential(email, password)).await()
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Error(e.toAccountChangeError())
        }
    }

    override fun sendCode(e164: String): Flow<PhoneCodeEvent> = callbackFlow {
        val request = ++activeRequest
        if (codeSentTo != e164) {
            // A different number: an earlier code must not be able to link it.
            verificationId = null
            codeSentTo = null
            resendToken = null
        }
        val activity = activityProvider.current() ?: run {
            trySend(PhoneCodeEvent.Finished(Result.Error(DataError.Unknown)))
            close()
            return@callbackFlow
        }
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                if (request != activeRequest) return
                // Android read the SMS (or skipped it): link straight away, no code to type.
                launch {
                    trySend(PhoneCodeEvent.Finished(link(credential, e164)))
                    close()
                }
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                if (request != activeRequest) return
                trySend(PhoneCodeEvent.Finished(Result.Error(exception.toAccountChangeError())))
                close()
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                if (request != activeRequest) return
                verificationId = id
                codeSentTo = e164
                resendToken = token
                trySend(PhoneCodeEvent.CodeSent)
            }
        }
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(e164)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
        // A resend to the same number reuses Firebase's token, so it isn't treated as a new request.
        resendToken?.takeIf { codeSentTo == e164 }?.let(options::setForceResendingToken)
        PhoneAuthProvider.verifyPhoneNumber(options.build())
        awaitClose { }
    }

    override suspend fun verifyCode(code: String): Result<Unit, DataError> {
        val id = verificationId ?: return Result.Error(DataError.Authentication.CODE_EXPIRED)
        val number = codeSentTo ?: return Result.Error(DataError.Authentication.CODE_EXPIRED)
        return link(PhoneAuthProvider.getCredential(id, code), number)
    }

    /** Replaces the linked number, or links the first one, then saves it to the profile (FR-042). */
    private suspend fun link(credential: PhoneAuthCredential, e164: String): Result<Unit, DataError> {
        val user = auth.currentUser ?: return Result.Error(DataError.Unknown)
        return try {
            val hasPhone = user.providerData.any { it.providerId == PhoneAuthProvider.PROVIDER_ID }
            if (hasPhone) user.updatePhoneNumber(credential).await() else user.linkWithCredential(credential).await()
            verificationId = null
            codeSentTo = null
            resendToken = null
            // Awaited (linking just needed the network): password reset by phone reads this
            // document, so it must not keep the old number while the account has the new one.
            firestore.collection("users").document(user.uid)
                .set(mapOf("phoneNumber" to e164, "phoneVerified" to true), SetOptions.merge())
                .await()
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // A rejected phone credential is a bad code here, never a "wrong password".
            when (val error = e.toAccountChangeError()) {
                DataError.Authentication.WRONG_PASSWORD -> Result.Error(DataError.Authentication.INVALID_CODE)
                else -> Result.Error(error)
            }
        }
    }
}
