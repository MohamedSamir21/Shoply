package com.shopping.shoply.authentication.data.datasource

import android.os.Build
import android.util.Log
import androidx.core.net.toUri
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.shopping.shoply.BuildConfig
import com.shopping.shoply.authentication.domain.model.User
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.getAuthenticationDataError
import com.shopping.shoply.common.presentation.navigation.DeepLinks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import kotlin.coroutines.resume

private const val TAG = "FirebaseAuthDataSource"

class FirebaseAuthDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : IAuthDataSource {

    override suspend fun signUp(user: User): Result<Unit, DataError> {
        return try {
            withTimeout(20000L) {
                val authResult = auth.createUserWithEmailAndPassword(user.email, user.password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val firebaseUser = auth.currentUser
                            firebaseUser?.let {
                                sendEmailVerificationLink(it, it.uid)
                                saveAdditionalUserInfo(
                                    uid = it.uid,
                                    user = user
                                )
                            }
                        }
                    }.await()
                if (authResult.user != null) {
                    signIn(user)
                    Result.Success(Unit)
                } else {
                    Result.Error(DataError.Authentication.UNKNOWN)
                }
            }
        } catch (ex: FirebaseAuthException) {
            Result.Error(ex.errorCode.getAuthenticationDataError())
        } catch (ex: FirebaseNetworkException) {
            Result.Error(DataError.Network)
        } catch (ex: TimeoutCancellationException) {
            Result.Error(DataError.Network)
        } catch (ex: Exception) {
            Log.i(TAG, ex.message.toString())
            Result.Error(DataError.Unknown)
        }
    }

    private fun sendEmailVerificationLink(
        user: FirebaseUser,
        uid: String,
        onComplete: (Result<Unit, DataError>) -> Unit = {}
    ) {
        Log.i(TAG, "SendEmailVerification")
        val actionCodeSettings = createActionCodeSettings(uid)
        user.sendEmailVerification(actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(Result.Success(data = Unit))
                    Log.i(TAG, "sent a verification email successfully!!")
                } else {
                    onComplete(Result.Error(DataError.Unknown))
                    Log.i(TAG, task.exception?.message.toString())
                }
            }
    }

    private fun createActionCodeSettings(uid: String): ActionCodeSettings {
        return ActionCodeSettings.newBuilder()
            .setUrl(DeepLinks.VERIFICATION_RESULT_ROUTE.plus(uid))
            .setHandleCodeInApp(false)
            .setAndroidPackageName(
                BuildConfig.APPLICATION_ID,
                true,
                Build.VERSION_CODES.N.toString()
            )
            .build()
    }

    override suspend fun resendVerificationEmailLink(): Result<Unit, DataError> =
        withContext(Dispatchers.IO) {
            val user = auth.currentUser
            val uid = auth.uid

            if (user == null || uid == null) {
                return@withContext Result.Error(DataError.Unknown)
            }

            val isVerified = suspendCancellableCoroutine { continuation ->
                isUserVerified(
                    user = user,
                    onComplete = {
                        if (continuation.isActive) {
                            continuation.resume(it)
                        }
                    }
                )
            }

            return@withContext if (isVerified) Result.Error(DataError.Authentication.ALREADY_VERIFIED)
            else {
                suspendCancellableCoroutine { continuation ->
                    sendEmailVerificationLink(user, uid) {
                        if (continuation.isActive) {
                            continuation.resume(it)
                        }
                    }
                }
            }

        }

    override suspend fun verifyEmail(deepLink: String): Result<Unit, DataError> {
        return try {
            withTimeout(20000L) {
                Log.d(TAG, "verifyEmail() is called and user is : ${auth.currentUser}")
                val user = auth.currentUser ?: return@withTimeout Result.Error(DataError.Unknown)

                val isVerified = suspendCancellableCoroutine { continuation ->
                    isUserVerified(
                        user = user,
                        onComplete = {
                            if (continuation.isActive) {
                                continuation.resume(it)
                            }
                        }
                    )
                }
                return@withTimeout if (isVerified) {
                    Result.Error(error = DataError.Authentication.ALREADY_VERIFIED)
                } else {
                    val deepLinkUri = deepLink.toUri()
                    if (deepLinkUri.getQueryParameter("mode") == "verifyEmail") {
                        val oobCode = deepLinkUri.getQueryParameter("oobCode")
                        if (!oobCode.isNullOrEmpty()) {
                            return@withTimeout checkOOBCodeWithFirebase(user, oobCode)
                        }
                    }
                    Result.Error(DataError.Unknown)
                }
            }
        } catch (ex: TimeoutCancellationException) {
            Result.Error(DataError.Network)
        } catch (ex: Exception) {
            Result.Error(DataError.Unknown)
        }
    }

    private suspend fun checkOOBCodeWithFirebase(
        user: FirebaseUser,
        oobCode: String
    ): Result<Unit, DataError> =
        try {
            withContext(Dispatchers.IO) {
                withTimeout(20000L) {
                    suspendCancellableCoroutine { continuation ->
                        Log.i(TAG, "continuing...")
                        auth.applyActionCode(oobCode).addOnSuccessListener {
                            Log.d(TAG, "Email verification successful")
                            if (continuation.isActive) {
                                isUserVerified(
                                    user = user,
                                    onComplete = { isVerified ->
                                        if (continuation.isActive) {
                                            continuation.resume(
                                                if (isVerified) Result.Success(Unit) else Result.Error(
                                                    DataError.Authentication.UNKNOWN
                                                )
                                            )
                                        }
                                    }
                                )
                            }
                        }.addOnFailureListener {
                            Log.i(TAG, "Outside isActive: ${it.message.toString()}")

                            if (continuation.isActive) {
                                Log.i(TAG, "Inside isActive: ${it.message.toString()}")
                                continuation.resume(Result.Error(DataError.Authentication.NOT_VERIFIED))
                            }
                        }
                    }
                }
            }
        } catch (ex: TimeoutCancellationException) {
            Result.Error(DataError.Network)
        } catch (ex: Exception) {
            Result.Error(DataError.Unknown)
        }


    private fun isUserVerified(
        user: FirebaseUser?,
        onComplete: (Boolean) -> Unit
    ) {
        user?.apply {
            reload().addOnCompleteListener {
                Log.d(TAG, "Success: $isEmailVerified")
                onComplete(isEmailVerified)
            }.addOnFailureListener {
                Log.i(TAG, "Failure: ${it.message.toString()}")
                onComplete(false)
            }
        }
    }

    override suspend fun signIn(user: User): Result<Unit, DataError> {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                auth.signInWithEmailAndPassword(user.email, user.password)
                    .addOnCompleteListener {
                        Log.d(TAG, "User is signed in!!")
                        if (continuation.isActive) {
                            continuation.resume(Result.Success(Unit))
                        }
                    }.addOnFailureListener {
                        if (continuation.isActive) {
                            continuation.resume(Result.Error(DataError.Authentication.UNKNOWN))
                        }
                    }
            }
        }
    }

    private fun saveAdditionalUserInfo(
        uid: String,
        user: User
    ) {
        firestore.collection(USER_COLLECTION_NAME)
            .document(uid)
            .set(user)
    }

    companion object {
        const val USER_COLLECTION_NAME = "users"
    }
}