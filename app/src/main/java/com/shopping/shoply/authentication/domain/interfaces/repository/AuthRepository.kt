package com.shopping.shoply.authentication.domain.interfaces.repository

import com.shopping.shoply.authentication.domain.model.SendOtpEvent
import com.shopping.shoply.authentication.domain.model.User
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    // Profile entity + password as a separate transient credential (not stored on User).
    suspend fun signUp(user: User, password: String): Result<Unit, DataError>
    suspend fun signIn(email: String, password: String): Result<Unit, DataError>
    suspend fun signUpWithGoogle(): Result<Unit, DataError>
    suspend fun signUpWithFacebook(): Result<Unit, DataError>
    suspend fun resendVerificationEmailLink(): Result<Unit, DataError>
    // Reloads the user and reports whether the email is now verified (status re-check
    // after the user clicks the web verification link).
    suspend fun refreshVerificationStatus(): Result<Boolean, DataError>
    suspend fun getUserInfo(): Result<User, DataError>
    suspend fun checkIfEmailExists(email: String): Result<Unit, DataError>
    suspend fun checkIfPhoneNumberExists(phoneNumber: String): Result<Unit, DataError>
    // Flags the current user's phone as verified (after a successful authenticated link).
    suspend fun markPhoneNumberVerified(): Result<Unit, DataError>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit, DataError>
    // Two outcomes (code-sent / auto-verified) modeled as a declarative event stream
    // instead of callbacks, keeping the domain contract framework-shape-free.
    fun sendOTPCode(
        countryCode: String,
        phoneNumber: String,
        // When true, auto-verification links the phone to the current user instead of signing in.
        link: Boolean
    ): Flow<SendOtpEvent>
    suspend fun verifyOTPCode(code: String): Result<Unit, DataError>
    // Links the verified phone credential onto the currently signed-in account (sign-up flow).
    suspend fun linkPhoneNumber(code: String): Result<Unit, DataError>
    // Updates the password of the currently signed-in user (reached after OTP sign-in
    // in the reset-password flow).
    suspend fun updatePassword(newPassword: String): Result<Unit, DataError>
}