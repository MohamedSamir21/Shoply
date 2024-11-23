package com.shopping.shoply.authentication.data.datasource

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.shopping.shoply.BuildConfig
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class GoogleAuthClient @Inject constructor(
    private val credentialManager: CredentialManager,
    private val firebaseAuth: FirebaseAuth
) {

    suspend fun signUpWithGoogle(context: Context): Result<Unit, DataError> {
        val googleIdOption = getGoogleIdOption()
        val credentialRequest = createCredentialRequest(googleIdOption)
        val credentialResponse = createCredentialResponse(
            context,
            credentialRequest
        )
        return handleCredentialResponse(credentialResponse)
    }

    private suspend fun handleCredentialResponse(
        credentialResponse: GetCredentialResponse
    ): Result<Unit, DataError> {
        return when (val credential = credentialResponse.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        val authCredential =
                            GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                        val authResult =
                            firebaseAuth.signInWithCredential(authCredential).await()

                        if (authResult?.user != null)
                            return Result.Success(Unit)
                    } catch (ex: GoogleIdTokenParsingException) {
                        return Result.Error(DataError.Authentication.UNKNOWN)
                    } catch (ex: GetCredentialCancellationException) {
                        return Result.Error(DataError.Unknown)
                    } catch (ex: FirebaseNetworkException) {
                        return Result.Error(DataError.Network)
                    } catch (ex: Exception) {
                        return Result.Error(DataError.Unknown)
                    }
                }
                Result.Error(DataError.Authentication.UNKNOWN)
            }

            else -> Result.Error(DataError.Authentication.UNKNOWN)
        }
    }

    private fun getGoogleIdOption(): GetGoogleIdOption {
        return GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.WEB_CLIENT_ID)
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
    ): GetCredentialResponse {
        return credentialManager.getCredential(
            context,
            credentialRequest
        )
    }
}