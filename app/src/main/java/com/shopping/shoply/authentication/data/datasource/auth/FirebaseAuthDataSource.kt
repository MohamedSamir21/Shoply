package com.shopping.shoply.authentication.data.datasource.auth

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.shopping.shoply.authentication.data.dto.UserDto
import com.shopping.shoply.authentication.data.mapper.toUser
import com.shopping.shoply.authentication.data.mapper.toUserDto
import com.shopping.shoply.authentication.domain.model.SendOtpEvent
import com.shopping.shoply.authentication.domain.model.User
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.getAuthenticationDataError
import com.shopping.shoply.common.platform.ActivityProvider
import com.shopping.shoply.common.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TAG = "FirebaseAuthDataSource"

/**
 * Firebase-backed implementation of [AuthDataSource]: the single place the app talks to Firebase
 * Authentication and Cloud Firestore for authentication. It owns three cross-cutting concerns so the
 * layers above it stay clean:
 *
 * - **Result mapping.** Every method returns the domain [Result]/[DataError] type, never a raw Firebase
 *   exception. Firebase failures are caught and translated — the auth error code via
 *   [getAuthenticationDataError], a `FirebaseNetworkException` or a [TimeoutCancellationException] to
 *   [DataError.Network], and anything unrecognized to [DataError.Unknown].
 * - **Threading.** Suspending work is confined to an injected [ioDispatcher] (never a hard-coded
 *   `Dispatchers.IO`). That injection is also the seam that makes these methods deterministically
 *   testable.
 * - **Callback bridging.** Firebase's callback/`Task` APIs are adapted to suspend return values via
 *   [suspendCancellableCoroutine] and `Task.await()`, and phone verification is exposed as a
 *   [callbackFlow].
 *
 * The only retained state is [verificationId], captured during [sendOTPCode] and read back by
 * [verifyOTPCode] / [linkPhoneNumber]; every other operation is stateless.
 *
 * @property firestore Cloud Firestore, source of the profile fields not held on the auth user.
 * @property auth Firebase Authentication, owner of the signed-in user and credential operations.
 * @property activityProvider supplies the current `Activity` that phone verification requires, so no
 *   `Context` is threaded through the domain.
 * @property ioDispatcher the dispatcher all blocking / suspending work runs on.
 */
class FirebaseAuthDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val activityProvider: ActivityProvider,
    // The data source owns its threading via an injected dispatcher (no hardcoded Dispatchers.IO).
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AuthDataSource {

    /**
     * Creates a new email/password account, persists the extra profile fields to Firestore, and sends
     * a verification email.
     *
     * `createUserWithEmailAndPassword` already signs the user in, so no separate sign-in is performed
     * (doing so raced the in-flight verification email). The verification send is awaited, but its
     * failure does not fail the sign-up itself.
     *
     * @param user the profile to create; [User.email] supplies the account email and the remaining
     *   fields are stored in Firestore (never the password).
     * @param password the account password — used only to create the credential, never stored.
     * @return [Result.Success] once the account exists and the profile is saved; otherwise
     *   [Result.Error] with the mapped [DataError] (auth failure, network loss, or timeout).
     */
    override suspend fun signUp(user: User, password: String): Result<Unit, DataError> =
        withContext(ioDispatcher) {
            var createdUser: FirebaseUser? = null
            try {
                withTimeout(20000L) {
                    val authResult = auth.createUserWithEmailAndPassword(
                        user.email.toString(), password
                    ).await()
                    val firebaseUser = authResult.user
                        ?: return@withTimeout Result.Error(DataError.Authentication.UNKNOWN)
                    createdUser = firebaseUser
                    saveAdditionalUserInfo(uid = firebaseUser.uid, user = user)
                    // createUserWithEmailAndPassword already signs the user in, so we do
                    // NOT call signIn() again here — re-authenticating raced the in-flight
                    // sendEmailVerification() and dropped the first verification email.
                    // Await the send so it reliably goes out before we return (the user's
                    // sign-up still succeeds even if the email send itself fails).
                    suspendCancellableCoroutine { continuation ->
                        sendEmailVerificationLink(firebaseUser) {
                            if (continuation.isActive) continuation.resume(it)
                        }
                    }
                    Result.Success(Unit)
                }
            } catch (ex: FirebaseAuthException) {
                rollBackAccount(createdUser)
                Result.Error(ex.errorCode.getAuthenticationDataError())
            } catch (_: FirebaseNetworkException) {
                rollBackAccount(createdUser)
                Result.Error(DataError.Network)
            } catch (_: TimeoutCancellationException) {
                rollBackAccount(createdUser)
                Result.Error(DataError.Network)
            } catch (ex: Exception) {
                Log.i(TAG, ex.message.toString())
                rollBackAccount(createdUser)
                Result.Error(DataError.Unknown)
            }
        }

    private fun sendEmailVerificationLink(
        user: FirebaseUser,
        onComplete: (Result<Unit, DataError>) -> Unit = {}
    ) {
        // No ActionCodeSettings/continue URL: the email link opens Firebase's hosted
        // verification page directly. We intentionally avoid a continue URL because the
        // old one relied on Firebase Dynamic Links (page.link), which was shut down on
        // 2025-08-25 and now fails with "Dynamic Link Not Found". The app instead
        // re-checks status via refreshVerificationStatus() when the user returns.
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(Result.Success(data = Unit))
                } else {
                    onComplete(Result.Error(DataError.Unknown))
                }
            }
    }

    /**
     * Reloads the current user from Firebase and reports whether their email is now verified.
     *
     * Called when the user returns to the app after clicking the (web) verification link, since the
     * cached auth user does not refresh its verification flag on its own.
     *
     * @return [Result.Success] wrapping `true` if verified or `false` if not; [Result.Error] with
     *   [DataError.Authentication.UNKNOWN] when no user is signed in, or [DataError.Network] on timeout.
     */
    override suspend fun refreshVerificationStatus(): Result<Boolean, DataError> =
        withContext(ioDispatcher) {
            val user = auth.currentUser
                ?: return@withContext Result.Error(DataError.Authentication.UNKNOWN)
            try {
                withTimeout(20000L) {
                    val isVerified = suspendCancellableCoroutine { continuation ->
                        isUserVerified(
                            user = user,
                            onComplete = { if (continuation.isActive) continuation.resume(it) },
                            onError = {
                                if (continuation.isActive) continuation.resumeWithException(
                                    it
                                )
                            }
                        )
                    }
                    Result.Success(isVerified)
                }
            } catch (_: TimeoutCancellationException) {
                Result.Error(DataError.Network)
            } catch (_: Exception) {
                Result.Error(DataError.Unknown)
            }
        }

    /**
     * Resends the email-verification link to the signed-in user, unless they are already verified.
     *
     * @return [Result.Success] once a fresh link is sent; [Result.Error] with
     *   [DataError.Authentication.ALREADY_VERIFIED] if the email is already verified, or
     *   [DataError.Unknown] when no user is signed in or the send fails.
     */
    override suspend fun resendVerificationEmailLink(): Result<Unit, DataError> =
        withContext(ioDispatcher) {
            val user = auth.currentUser ?: return@withContext Result.Error(DataError.Unknown)

            try {
                val isVerified = suspendCancellableCoroutine { continuation ->
                    isUserVerified(
                        user = user,
                        onComplete = { if (continuation.isActive) continuation.resume(it) },
                        onError = { if (continuation.isActive) continuation.resumeWithException(it) }
                    )
                }

                if (isVerified) Result.Error(DataError.Authentication.ALREADY_VERIFIED)
                else {
                    suspendCancellableCoroutine { continuation ->
                        sendEmailVerificationLink(user) {
                            if (continuation.isActive) {
                                continuation.resume(it)
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                Result.Error(DataError.Unknown)
            }
        }

    private fun isUserVerified(
        user: FirebaseUser,
        onComplete: (Boolean) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        // addOnCompleteListener fires on both success and failure, so it — not a separate
        // failure listener — decides the outcome; a failed reload() must not be read as unverified.
        user.reload().addOnCompleteListener { task ->
            if (task.isSuccessful) onComplete(user.isEmailVerified)
            else onError(task.exception ?: RuntimeException("reload() failed"))
        }
    }

    /**
     * Signs a user in with email and password.
     *
     * @param email the account email.
     * @param password the account password.
     * @return [Result.Success] on success; [Result.Error] with
     *   [DataError.Authentication.INVALID_CREDENTIALS] when the credentials are rejected, or
     *   [DataError.Authentication.UNKNOWN] on any other failure.
     */
    override suspend fun signIn(email: String, password: String): Result<Unit, DataError> {
        return withContext(ioDispatcher) {
            suspendCancellableCoroutine { continuation ->
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            Log.d(TAG, "User is signed in!!")
                            if (continuation.isActive) {
                                continuation.resume(Result.Success(Unit))
                            }
                        } else {
                            Log.d(TAG, "Invalid Credentials!!")
                            if (continuation.isActive) {
                                continuation.resume(Result.Error(DataError.Authentication.INVALID_CREDENTIALS))
                            }
                        }
                    }.addOnFailureListener {
                        if (continuation.isActive) {
                            continuation.resume(Result.Error(DataError.Authentication.UNKNOWN))
                        }
                    }
            }
        }
    }

    private suspend fun saveAdditionalUserInfo(
        uid: String,
        user: User
    ) {
        firestore.collection(USER_COLLECTION_NAME)
            .document(uid)
            .set(user.toUserDto()) // persist the DTO — password is never written to Firestore
            .await() // await so a failed profile write fails the sign-up instead of being dropped
    }

    /**
     * Best-effort rollback of a just-created account so a failed [signUp] leaves nothing behind.
     *
     * If a step after account creation fails (e.g. the Firestore profile write), the account would
     * otherwise linger and the next attempt would hit `ERROR_EMAIL_ALREADY_IN_USE`. A delete failure
     * is swallowed so it never masks the original error that triggered the rollback.
     */
    private suspend fun rollBackAccount(user: FirebaseUser?) {
        if (user == null) return
        runCatching { user.delete().await() }
    }

    /**
     * Returns the signed-in user's profile, merging the Firebase auth user with the Firestore document.
     *
     * The auth user supplies email/phone from the sign-in provider; any gaps are filled from the
     * Firestore profile, which is also the only source of phone-verification state.
     *
     * @return [Result.Success] with the assembled [User]; [Result.Error] with
     *   [DataError.Authentication.UNKNOWN] when no user is signed in or neither email nor phone is
     *   known, [DataError.Network] when the profile read fails or times out, or [DataError.Unknown]
     *   on any other read failure.
     */
    override suspend fun getUserInfo(): Result<User, DataError> = withContext(ioDispatcher) {
        val currentFirebaseUser = auth.currentUser
            ?: return@withContext Result.Error(DataError.Authentication.UNKNOWN)
        try {
            var (email, phoneNumber) = getAuthMethod(currentFirebaseUser)
            val firestoreUser = getUserInfoFromFirestore(currentFirebaseUser)
            if (firestoreUser != null) { // Fill in with Info saved in Firestore
                if (email.isNullOrEmpty())
                    email = firestoreUser.email
                if (phoneNumber.isNullOrEmpty())
                    phoneNumber = firestoreUser.phoneNumber
            }
            if (email == null && phoneNumber == null) {
                return@withContext Result.Error(
                    error = DataError.Authentication.UNKNOWN
                )
            }
            Result.Success(
                User(
                    email = email,
                    phoneNumber = phoneNumber,
                    // Verification state only lives in Firestore, not on the Firebase auth user.
                    isPhoneVerified = firestoreUser?.isPhoneVerified ?: false
                )
            )
        } catch (_: FirebaseNetworkException) {
            Result.Error(DataError.Network)
        } catch (_: TimeoutCancellationException) {
            Result.Error(DataError.Network)
        } catch (ex: Exception) {
            Log.i(TAG, ex.message.toString())
            Result.Error(DataError.Unknown)
        }
    }

    private fun getAuthMethod(currentUser: FirebaseUser): Pair<String?, String?> {
        val email = currentUser.email
        val phoneNumber = currentUser.phoneNumber
        return Pair(email, phoneNumber)
    }

    // Throws on read failure; the caller maps it to a DataError.
    private suspend fun getUserInfoFromFirestore(currentUser: FirebaseUser): User? =
        withTimeout(5000L) {
            firestore.collection(USER_COLLECTION_NAME)
                .document(currentUser.uid)
                .get()
                .await()
                .toObject(UserDto::class.java)
                ?.toUser()
        }

    /**
     * Checks whether a user document with the given [email] exists in Firestore.
     *
     * @param email the email to look up.
     * @return [Result.Success] if a matching document exists; [Result.Error] with
     *   [DataError.Authentication.EMAIL_DOES_NOT_EXIST] if none matches, or [DataError.Unknown] on
     *   query failure or timeout.
     */
    override suspend fun checkIfEmailExists(email: String): Result<Unit, DataError> =
        withContext(ioDispatcher) {
            // suspend + await() instead of a callback, so callers just read a return value.
            val query =
                firestore.collection(USER_COLLECTION_NAME).whereEqualTo("email", email).limit(1)
            try {
                val documents = withTimeout(5000L) { query.get().await() }
                if (!documents.isEmpty) Result.Success(Unit)
                else Result.Error(DataError.Authentication.EMAIL_DOES_NOT_EXIST)
            } catch (_: Exception) {
                Result.Error(DataError.Unknown)
            }
        }

    /**
     * Triggers Firebase's password-reset email for [email].
     *
     * @param email the address to send the reset link to.
     * @return [Result.Success] once Firebase accepts the request; [Result.Error] with
     *   [DataError.Unknown] on failure.
     */
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit, DataError> =
        withContext(ioDispatcher) {
            try {
                auth.sendPasswordResetEmail(email).await()
                Log.i(TAG, "sendPasswordResetEmail() success!!")
                Result.Success(Unit)
            } catch (ex: Exception) {
                Log.d(TAG, "sendPasswordResetEmail(): ${ex.message.toString()}")
                Result.Error(DataError.Unknown)
            }
        }

    /**
     * Checks whether a **verified** phone number exists in Firestore, for phone-based recovery.
     *
     * A match counts as success only if the number was verified in an authenticated context, so an
     * unverified or mistyped number cannot be used to hijack account recovery.
     *
     * @param phoneNumber the E.164 number to look up.
     * @return [Result.Success] if a verified match exists; [Result.Error] with
     *   [DataError.Authentication.PHONE_NUMBER_DOES_NOT_EXIST] if none matches,
     *   [DataError.Authentication.PHONE_NUMBER_NOT_LINKED] if it exists but is unverified, or
     *   [DataError.Unknown] on failure.
     */
    override suspend fun checkIfPhoneNumberExists(phoneNumber: String): Result<Unit, DataError> =
        withContext(ioDispatcher) {
            Log.d(TAG, "The phone number: $phoneNumber")
            val query =
                firestore.collection(USER_COLLECTION_NAME).whereEqualTo("phoneNumber", phoneNumber)
                    .limit(1)
            try {
                val documents = withTimeout(5000L) { query.get().await() }
                val doc = documents.documents.firstOrNull()
                    ?: return@withContext Result.Error(DataError.Authentication.PHONE_NUMBER_DOES_NOT_EXIST)
                // The number exists, but recovery is only allowed once it was verified in an
                // authenticated context — otherwise an unverified/typo'd number could be abused.
                if (doc.getBoolean("phoneVerified") == true) Result.Success(Unit)
                else Result.Error(DataError.Authentication.PHONE_NUMBER_NOT_LINKED)
            } catch (_: Exception) {
                Result.Error(DataError.Unknown)
            }
        }

    /**
     * Flags the signed-in user's phone number as verified in Firestore (after a successful link).
     *
     * @return [Result.Success] on update; [Result.Error] with [DataError.Authentication.UNKNOWN] when
     *   no user is signed in, or [DataError.Network] on timeout.
     */
    override suspend fun markPhoneNumberVerified(): Result<Unit, DataError> =
        withContext(ioDispatcher) {
            val uid = auth.currentUser?.uid
                ?: return@withContext Result.Error(DataError.Authentication.UNKNOWN)
            try {
                withTimeout(5000L) {
                    firestore.collection(USER_COLLECTION_NAME)
                        .document(uid)
                        .update("phoneVerified", true)
                        .await()
                    Result.Success(Unit)
                }
            } catch (_: TimeoutCancellationException) {
                Result.Error(DataError.Network)
            } catch (ex: Exception) {
                Log.i(TAG, ex.message.toString())
                Result.Error(DataError.Unknown)
            }
        }

    /**
     * Starts phone-number verification and streams its progress as [SendOtpEvent]s.
     *
     * Collecting the returned flow begins verification. It emits [SendOtpEvent.CodeSent] once the SMS
     * code has been sent (and [verificationId] captured), or [SendOtpEvent.AutoVerified] when Firebase
     * instant-verifies / auto-retrieves the code — in which case the credential is either linked to the
     * current account or used to sign in, per [link].
     *
     * @param countryCode the country calling code, without a leading `+`.
     * @param phoneNumber the national number; combined with [countryCode] into E.164.
     * @param link when `true`, auto-verification links the phone to the current account; when `false`,
     *   it signs in with the phone credential.
     * @return a cold [Flow] that runs verification while collected and closes when the stream ends.
     */
    override fun sendOTPCode(
        countryCode: String,
        phoneNumber: String,
        link: Boolean
    ): Flow<SendOtpEvent> = callbackFlow {
        Log.d(TAG, "sendOTPCode is called!")
        Log.d(TAG, "The phone number is $phoneNumber")
        Log.d(TAG, "country code is $countryCode")

        // Activity comes from the provider, so no Context is threaded through the layers.
        val activity = activityProvider.current() ?: run {
            trySend(SendOtpEvent.AutoVerified(Result.Error(DataError.Unknown)))
            close()
            return@callbackFlow
        }
        // Callbacks live inside the flow — no shared mutable callback state on the class.
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "Verification completed!!")
                val onResult: (Result<Unit, DataError>) -> Unit =
                    { trySend(SendOtpEvent.AutoVerified(it)) }
                if (link) linkWithCredential(credential, onResult)
                else singInWithCredential(credential, onResult)
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                Log.e(TAG, " Error from sendOTPCode(): ${exception.message.toString()}")
                // Surface the failure so the caller stops "processing" instead of hanging.
                trySend(SendOtpEvent.AutoVerified(Result.Error(DataError.Unknown)))
            }

            override fun onCodeSent(
                id: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = id
                Log.d(TAG, "OTP sent and the verificationID is $verificationId")
                // Emit "code sent" only now that verificationId is set, so a caller that
                // navigates away on this event (popping its own back stack) can't race
                // ahead of verifyOTPCode and read a null verificationId.
                trySend(SendOtpEvent.CodeSent)
            }
        }
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+".plus(countryCode).plus(phoneNumber))
            .setCallbacks(callbacks)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        // Note: CodeSent is emitted from the onCodeSent callback above (once verificationId
        // is available), not here — emitting it eagerly raced the callback and left
        // verifyOTPCode with a null verificationId.
        awaitClose { }
    }

    /**
     * Completes phone verification by signing in with the SMS [code] against the stored
     * [verificationId] captured by [sendOTPCode].
     *
     * @param code the OTP the user entered.
     * @return [Result.Success] on successful sign-in; [Result.Error] with
     *   [DataError.Authentication.UNKNOWN] when no verification is in progress or the code is rejected.
     */
    override suspend fun verifyOTPCode(code: String): Result<Unit, DataError> =
        withContext(ioDispatcher) {
            Log.d(TAG, "Start verifying - verification id: $verificationId")
            val id = verificationId
                ?: return@withContext Result.Error(DataError.Authentication.UNKNOWN)
            val credential = PhoneAuthProvider.getCredential(id, code)
            // Bridge the callback-based credential sign-in into a suspend return value.
            Log.d(TAG, "Before verifying")
            suspendCancellableCoroutine { continuation ->
                singInWithCredential(credential) { result ->
                    if (continuation.isActive) continuation.resume(result)
                    Log.d(TAG, "After verifying")
                }
            }
        }

    /**
     * Updates the signed-in user's password.
     *
     * Normally reached right after the user re-authenticated via OTP, so the recent-login requirement
     * is already satisfied.
     *
     * @param newPassword the new password to set.
     * @return [Result.Success] on success; [Result.Error] with
     *   [DataError.Authentication.WEAK_PASSWORD] if rejected as too weak,
     *   [DataError.Authentication.INVALID_CREDENTIALS] if a recent login is required,
     *   [DataError.Network] on network loss or timeout, or another mapped [DataError] otherwise.
     */
    override suspend fun updatePassword(newPassword: String): Result<Unit, DataError> =
        withContext(ioDispatcher) {
            // Reached after the user re-authenticated via OTP, so the recent-login
            // requirement is normally already satisfied.
            val user = auth.currentUser
                ?: return@withContext Result.Error(DataError.Authentication.UNKNOWN)
            try {
                withTimeout(20000L) {
                    user.updatePassword(newPassword).await()
                    Result.Success(Unit)
                }
            } catch (_: FirebaseAuthWeakPasswordException) {
                Result.Error(DataError.Authentication.WEAK_PASSWORD)
            } catch (_: FirebaseAuthRecentLoginRequiredException) {
                Result.Error(DataError.Authentication.INVALID_CREDENTIALS)
            } catch (ex: FirebaseAuthException) {
                Result.Error(ex.errorCode.getAuthenticationDataError())
            } catch (_: FirebaseNetworkException) {
                Result.Error(DataError.Network)
            } catch (_: TimeoutCancellationException) {
                Result.Error(DataError.Network)
            } catch (ex: Exception) {
                Log.i(TAG, ex.message.toString())
                Result.Error(DataError.Unknown)
            }
        }

    private fun singInWithCredential(
        credential: PhoneAuthCredential,
        onComplete: (Result<Unit, DataError>) -> Unit
    ) {
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(Result.Success(Unit))
            } else {
                Log.d(TAG, task.exception?.message.toString())
                onComplete(Result.Error(DataError.Authentication.UNKNOWN))
            }
        }
    }

    /**
     * Links a verified phone credential onto the currently signed-in account (the sign-up flow), using
     * the SMS [code] and the stored [verificationId].
     *
     * @param code the OTP the user entered.
     * @return [Result.Success] on link; [Result.Error] with
     *   [DataError.Authentication.PHONE_NUMBER_ALREADY_IN_USE] if the number is already linked to
     *   another account, [DataError.Authentication.UNKNOWN] when no verification is in progress, or
     *   another mapped [DataError] otherwise.
     */
    override suspend fun linkPhoneNumber(code: String): Result<Unit, DataError> =
        withContext(ioDispatcher) {
            val id = verificationId
                ?: return@withContext Result.Error(DataError.Authentication.UNKNOWN)
            val credential = PhoneAuthProvider.getCredential(id, code)
            suspendCancellableCoroutine { continuation ->
                linkWithCredential(credential) { result ->
                    if (continuation.isActive) continuation.resume(result)
                }
            }
        }

    // Attaches the phone credential to the currently signed-in account (so one uid has both
    // email and phone providers). Surfaces the "already linked elsewhere" collision distinctly.
    private fun linkWithCredential(
        credential: PhoneAuthCredential,
        onComplete: (Result<Unit, DataError>) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onComplete(Result.Error(DataError.Authentication.UNKNOWN))
            return
        }
        user.linkWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(Result.Success(Unit))
            } else {
                Log.d(TAG, task.exception?.message.toString())
                val error = when (val ex = task.exception) {
                    is FirebaseAuthUserCollisionException -> DataError.Authentication.PHONE_NUMBER_ALREADY_IN_USE
                    is FirebaseAuthException -> ex.errorCode.getAuthenticationDataError()
                    else -> DataError.Authentication.UNKNOWN
                }
                onComplete(Result.Error(error))
            }
        }
    }

    // Only the verificationId outlives a single sendOTPCode call (read later by verifyOTPCode).
    // Exposed as internal solely so tests can seed it; @VisibleForTesting makes lint flag any
    // production access from outside this class.
    @VisibleForTesting
    internal var verificationId: String? = null

    companion object {
        const val USER_COLLECTION_NAME = "users"
    }
}