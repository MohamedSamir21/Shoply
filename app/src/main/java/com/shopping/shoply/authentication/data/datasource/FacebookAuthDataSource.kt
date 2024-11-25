package com.shopping.shoply.authentication.data.datasource

import android.util.Log
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import javax.inject.Inject

private const val TAG = "FacebookAuthDataSource"

class FacebookAuthDataSource @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    fun signUpWithFacebook(
        loginManager: LoginManager,
        callbackManager: CallbackManager,
        onComplete: (Result<Unit, DataError>) -> Unit
    ) {
        Log.i(TAG, "signUpWithFacebook()")

        loginManager.registerCallback(
            callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onCancel() {
                    Log.i(TAG, "onCancel()")
                }

                override fun onError(error: FacebookException) {
                    Log.i(TAG, "onError()")
                    onComplete(Result.Error(DataError.Authentication.UNKNOWN))
                }

                override fun onSuccess(result: LoginResult) {
                    Log.i(TAG, "onSuccess()")
                    handleFacebookAccessToken(result.accessToken) {
                        onComplete(it)
                    }
                }
            }
        )
    }

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
                    onComplete(Result.Error(DataError.Authentication.UNKNOWN))
                }
            }
    }
}