package com.shopping.shoply.authentication.data.datasource

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider
import com.shopping.shoply.BuildConfig
import com.shopping.shoply.common.di.IoDispatcher
import com.shopping.shoply.common.di.MainDispatcher
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.getAuthenticationDataError
import com.shopping.shoply.common.platform.ActivityProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Google "Sign in with Google", via the AndroidX Credential Manager, bridged to Firebase Auth.
 *
 * [signUpWithGoogle] runs the whole flow: it obtains a Google ID token through [credentialManager],
 * exchanges it for a Firebase [GoogleAuthProvider] credential, and signs the user in. Every failure
 * — a dismissed account picker, an unavailable credential, a malformed ID token, or a Firebase auth
 * or network error — is caught and mapped to a domain [Result]/[DataError]; a raw exception never
 * escapes to the caller.
 *
 * @property credentialManager issues the Google ID-token credential request and shows the picker.
 * @property firebaseAuth exchanges the Google credential for a signed-in Firebase user.
 * @property activityProvider supplies the current Activity the credential UI is shown on, so no
 *   `Context` is threaded through the domain.
 * @property mainDispatcher UI thread the credential picker is requested on.
 * @property ioDispatcher dispatcher the Firebase credential exchange runs on.
 */
class GoogleAuthDataSource @Inject constructor(
    private val credentialManager: CredentialManager,
    private val firebaseAuth: FirebaseAuth,
    private val activityProvider: ActivityProvider,
    @param:MainDispatcher private val mainDispatcher: CoroutineDispatcher,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Signs the user in with Google and returns the outcome as a domain [Result].
     *
     * @return [Result.Success] once Firebase accepts the Google credential; otherwise [Result.Error]
     *   with [DataError.Network] on a Firebase network failure, a specific [DataError.Authentication]
     *   mapped from the Firebase error code, or [DataError.Unknown] when no Activity is available,
     *   the user cancels the picker, or the credential is unavailable or not a Google ID token.
     */
    suspend fun signUpWithGoogle(): Result<Unit, DataError> {
        // Pull the current Activity from the provider instead of receiving a Context arg.
        val context = activityProvider.current() ?: return Result.Error(DataError.Unknown)
        return try {
            val googleIdOption = getGoogleIdOption()
            val credentialRequest = createCredentialRequest(googleIdOption)
            val credentialResponse = createCredentialResponse(context, credentialRequest)
            handleCredentialResponse(credentialResponse)
        } catch (_: GetCredentialCancellationException) {
            // User dismissed the account picker before choosing an account.
            Result.Error(DataError.Unknown)
        } catch (_: GoogleIdTokenParsingException) {
            Result.Error(DataError.Authentication.UNKNOWN)
        } catch (ex: FirebaseAuthException) {
            Result.Error(ex.errorCode.getAuthenticationDataError())
        } catch (_: FirebaseNetworkException) {
            Result.Error(DataError.Network)
        } catch (_: GetCredentialException) {
            // No credential available, provider misconfiguration, etc.
            Result.Error(DataError.Unknown)
        } catch (ex: Exception) {
            println(ex.message)
            Result.Error(DataError.Unknown)
        }
    }

    /**
     * Exchanges a Google ID-token credential from [credentialResponse] for a Firebase sign-in.
     *
     * @return [Result.Success] if Firebase returns a user; [Result.Error] with
     *   [DataError.Authentication.UNKNOWN] if the response is not a Google ID-token credential or
     *   Firebase returns no user. Throwing calls (token parsing, sign-in) are mapped by the caller.
     */
    private suspend fun handleCredentialResponse(
        credentialResponse: GetCredentialResponse
    ): Result<Unit, DataError> {
        val credential = credentialResponse.credential
        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            return Result.Error(DataError.Authentication.UNKNOWN)
        }
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
        val authCredential =
            GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
        val authResult = withContext(ioDispatcher) {
            firebaseAuth.signInWithCredential(authCredential).await()
        }
        return if (authResult?.user != null) Result.Success(Unit)
        else Result.Error(DataError.Authentication.UNKNOWN)
    }

    private fun getGoogleIdOption(): GetGoogleIdOption {
        return GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
            .setAutoSelectEnabled(false)
            .build()
    }

    private fun createCredentialRequest(googleIdOption: GetGoogleIdOption): GetCredentialRequest {
        return GetCredentialRequest.Builder()
            .addCredentialOption(
                googleIdOption
            ).build()
    }

    private suspend fun createCredentialResponse(
        context: Context,
        credentialRequest: GetCredentialRequest
    ): GetCredentialResponse = withContext(mainDispatcher) {
        // The credential picker is UI, so it is requested on the main thread.
        credentialManager.getCredential(context, credentialRequest)
    }
}