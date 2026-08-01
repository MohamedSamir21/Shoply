package com.shopping.shoply.authentication.data.repository

import com.shopping.shoply.authentication.data.datasource.auth.FacebookAuthDataSource
import com.shopping.shoply.authentication.data.datasource.auth.GoogleAuthDataSource
import com.shopping.shoply.authentication.data.datasource.auth.AuthDataSource
import com.shopping.shoply.authentication.domain.interfaces.repository.AuthRepository
import com.shopping.shoply.authentication.domain.model.User
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val googleAuthDataSource: GoogleAuthDataSource,
    private val facebookAuthDataSource: FacebookAuthDataSource
) : AuthRepository {

    // Pure delegator: each data source owns its own threading, so the repository
    // doesn't pick dispatchers.
    override suspend fun signUp(user: User, password: String) =
        authDataSource.signUp(user, password)


    override suspend fun signIn(email: String, password: String) =
        authDataSource.signIn(email, password)


    override suspend fun signUpWithGoogle() = googleAuthDataSource.signUpWithGoogle()


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

}