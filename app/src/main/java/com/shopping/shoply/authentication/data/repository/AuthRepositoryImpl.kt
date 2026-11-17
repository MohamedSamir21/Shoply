package com.shopping.shoply.authentication.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.shopping.shoply.authentication.data.datasource.auth.FacebookAuthDataSource
import com.shopping.shoply.authentication.data.datasource.auth.GoogleAuthDataSource
import com.shopping.shoply.authentication.data.datasource.auth.AuthDataSource
import com.shopping.shoply.authentication.domain.interfaces.repository.AuthRepository
import com.shopping.shoply.authentication.domain.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val googleAuthDataSource: GoogleAuthDataSource,
    private val facebookAuthDataSource: FacebookAuthDataSource,
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    // Pure delegator: each data source owns its own threading, so the repository
    // doesn't pick dispatchers.
    override suspend fun signUp(user: User, password: String) =
        authDataSource.signUp(user, password)


    override suspend fun signIn(email: String, password: String) =
        authDataSource.signIn(email, password)


    override suspend fun signUpWithGoogle() = googleAuthDataSource.signUpWithGoogle().apply {
        firebaseAuth.currentUser?.let { user ->
            syncProfileFromGoogle(user)
        }
    }


    override suspend fun signUpWithFacebook() = facebookAuthDataSource.signUpWithFacebook()


    override suspend fun resendVerificationEmailLink() =
        authDataSource.resendVerificationEmailLink()


    override suspend fun refreshVerificationStatus() = authDataSource.refreshVerificationStatus()


    override suspend fun getUserInfo() = authDataSource.getUserInfo()

    override suspend fun checkIfEmailExists(email: String) =
        authDataSource.checkIfEmailExists(email)


    override suspend fun sendPasswordResetEmail(email: String) =
        authDataSource.sendPasswordResetEmail(email)


    override suspend fun checkIfPhoneNumberExists(phoneNumber: String) =
        authDataSource.checkIfPhoneNumberExists(phoneNumber)


    override suspend fun markPhoneNumberVerified() = authDataSource.markPhoneNumberVerified()


    override fun sendOTPCode(
        countryCode: String,
        phoneNumber: String,
        link: Boolean
    ) = authDataSource.sendOTPCode(
        countryCode = countryCode,
        phoneNumber = phoneNumber,
        link = link
    )

    override suspend fun verifyOTPCode(code: String) = authDataSource.verifyOTPCode(code)


    override suspend fun linkPhoneNumber(code: String) = authDataSource.linkPhoneNumber(code)


    override suspend fun updatePassword(newPassword: String) =
        authDataSource.updatePassword(newPassword)

    /** There's an invariant that this method only changes the username and photoUrl if the user
     * doesn't update their user info from Edit Profile.*/
    private suspend fun syncProfileFromGoogle(user: FirebaseUser) {
        val google =
            user.providerData.firstOrNull { userInfo -> userInfo.providerId == "google.com" }
                ?: return
        val doc = firestore.collection("users").document(user.uid)
        val current = doc.get().await()

        val updates = mutableMapOf<String, Any?>("updatedAt" to FieldValue.serverTimestamp())

        if (current.getString("nameSource") != "custom") {
            updates["username"] = google.displayName; updates["nameSource"] = "google"
        }

        if (current.getString("photoSource") != "custom") {
            updates["photoUrl"] = google.photoUrl; updates["photoSource"] = "google"
        }
        doc.set(updates, SetOptions.merge()).await()
    }

}