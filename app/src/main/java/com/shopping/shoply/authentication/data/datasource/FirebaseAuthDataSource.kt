package com.shopping.shoply.authentication.data.datasource

import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestore
import com.shopping.shoply.authentication.domain.model.User
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import com.shopping.shoply.common.domain.getAuthenticationDataError
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TAG = "FirebaseAuthDataSource"

class FirebaseAuthDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : IAuthDataSource {

    override suspend fun signUp(
        user: User
    ): Result<Unit, DataError> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(user.email, user.password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = auth.currentUser
                        firebaseUser?.let {
                            saveAdditionalUserInfo(
                                uid = it.uid,
                                user = user
                            )
                        }
                    }
                }.await()
            if (authResult.user != null) {
                Result.Success(Unit)
            } else {
                Result.Error(DataError.Authentication.UNKNOWN)
            }
        } catch (ex: FirebaseAuthException) {
            Result.Error(ex.errorCode.getAuthenticationDataError())
        } catch (ex: FirebaseNetworkException) {
            Result.Error(DataError.Network)
        } catch (ex: Exception) {
            Log.i(TAG, ex.message.toString())
            Result.Error(DataError.Unknown)
        }
    }

    override suspend fun signIn(user: User): Result<Unit, DataError> {
        TODO("Not yet implemented")
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