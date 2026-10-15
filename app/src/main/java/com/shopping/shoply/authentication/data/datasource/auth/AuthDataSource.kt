package com.shopping.shoply.authentication.data.datasource.auth

import com.shopping.shoply.authentication.domain.model.SendOtpEvent
import com.shopping.shoply.authentication.domain.model.User
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import kotlinx.coroutines.flow.Flow

interface AuthDataSource {
    suspend fun signUp(user: User, password: String): Result<Unit, DataError>
    suspend fun signIn(email: String, password: String): Result<Unit, DataError>
    suspend fun resendVerificationEmailLink(): Result<Unit, DataError>
    // Reloads the user and reports whether the email is verified (Option A: status re-check).
    suspend fun refreshVerificationStatus(): Result<Boolean, DataError>
    suspend fun getUserInfo(): Result<User, DataError>
    suspend fun checkIfEmailExists(email: String): Result<Unit, DataError>
    suspend fun checkIfPhoneNumberExists(phoneNumber: String): Result<Unit, DataError>
    // Flags the current user's phone as verified in Firestore (after a successful link).
    suspend fun markPhoneNumberVerified(): Result<Unit, DataError>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit, DataError>
    fun sendOTPCode(
        countryCode: String,
        phoneNumber: String,
        // When true, auto-verification links the phone to the current user instead of signing in.
        link: Boolean
    ): Flow<SendOtpEvent>
    suspend fun verifyOTPCode(code: String): Result<Unit, DataError>
    // Links the verified phone credential onto the currently signed-in account (sign-up flow).
    suspend fun linkPhoneNumber(code: String): Result<Unit, DataError>
    suspend fun updatePassword(newPassword: String): Result<Unit, DataError>
}