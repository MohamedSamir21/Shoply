package com.shopping.shoply.authentication.data.datasource

import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.getAuthenticationDataError
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Facebook Login, bridged to Firebase Auth.
 *
 * [signUpWithFacebook] adapts the Facebook SDK's listener API to a single suspend result, so the callback
 * never leaks up through the repository and use case. Every outcome — success, a user-cancelled login, an
 * SDK error, or a Firebase failure — is mapped to a domain [Result]/[DataError].
 *
 * The SDK objects are injected rather than passed in from the UI: [callbackManager] must be the *same*
 * instance the login button builds its `ActivityResultContract` with, which sharing a singleton
 * guarantees (see `FacebookModule`).
 *
 * @property loginManager registers the login callback the Facebook UI flow reports back to.
 * @property callbackManager routes the login Activity's result to that callback.
 * @property firebaseAuth exchanges the Facebook access token for a signed-in Firebase user.
 */
class FacebookAuthDataSource @Inject constructor(
    private val loginManager: LoginManager,
    private val callbackManager: CallbackManager,
    private val firebaseAuth: FirebaseAuth
) {

    /**
     * Runs the Facebook login flow and exchanges its access token for a Firebase sign-in.
     *
     * Suspends until the Facebook UI reports back through the registered callback. The callback is always
     * unregistered before returning — including on cancellation — so repeated attempts do not stack up
     * callbacks on the singleton [loginManager].
     *
     * @return [Result.Success] once Firebase accepts the Facebook credential; otherwise [Result.Error]
     *   with [DataError.Network] on a network failure, a specific [DataError.Authentication] mapped from
     *   the Firebase error code, or [DataError.Unknown] when the user cancels the login.
     */
    suspend fun signUpWithFacebook(): Result<Unit, DataError> =
        suspendCancellableCoroutine { continuation ->
            // A cancelled coroutine must not leave its callback registered on the singleton LoginManager.
            continuation.invokeOnCancellation { loginManager.unregisterCallback(callbackManager) }

            loginManager.registerCallback(
                callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onCancel() {
                        resumeWith(Result.Error(DataError.Unknown))
                    }

                    override fun onError(error: FacebookException) {
                        resumeWith(Result.Error(DataError.Authentication.UNKNOWN))
                    }

                    override fun onSuccess(result: LoginResult) {
                        handleFacebookAccessToken(result.accessToken) { resumeWith(it) }
                    }

                    private fun resumeWith(result: Result<Unit, DataError>) {
                        loginManager.unregisterCallback(callbackManager)
                        if (continuation.isActive) continuation.resume(result)
                    }
                }
            )
        }

    /**
     * Signs in to Firebase with the Facebook [accessToken] and reports the mapped outcome to [onComplete].
     */
    private fun handleFacebookAccessToken(
        accessToken: AccessToken,
        onComplete: (Result<Unit, DataError>) -> Unit
    ) {
        val authCredential = FacebookAuthProvider.getCredential(accessToken.token)
        firebaseAuth.signInWithCredential(authCredential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(Result.Success(Unit))
                } else {
                    onComplete(Result.Error(task.exception.toDataError()))
                }
            }
    }

    private fun Exception?.toDataError(): DataError = when (this) {
        is FirebaseAuthException -> errorCode.getAuthenticationDataError()
        is FirebaseNetworkException -> DataError.Network
        else -> DataError.Authentication.UNKNOWN
    }
}
