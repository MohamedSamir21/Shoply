package com.shopping.shoply.authentication.data.datasource

import android.app.Activity
import com.shopping.shoply.authentication.domain.model.User
import com.shopping.shoply.common.domain.DataError
import com.shopping.shoply.common.domain.Result

interface IAuthDataSource {
    suspend fun signUp(user: User): Result<Unit, DataError>
    suspend fun signIn(user: User): Result<Unit, DataError>
    suspend fun resendVerificationEmailLink(): Result<Unit, DataError>
    suspend fun verifyEmail(deepLink: String): Result<Unit, DataError>
    suspend fun getUserInfo(): Result<User, DataError>
    suspend fun checkIfEmailExists(
        email: String,
        onComplete: (Result<Unit, DataError>) -> Unit
    )
    suspend fun checkIfPhoneNumberExists(
        phoneNumber: String,
        onComplete: (Result<Unit, DataError>) -> Unit
    )
    suspend fun sendPasswordRestEmail(
        email: String,
        onComplete: (Result<Unit, DataError>) -> Unit
    )
    suspend fun sendOTPCode(
        context: Activity,
        countryCode: String,
        phoneNumber: String,
        onSent: () -> Unit,
        onAutoVerification: (Result<Unit, DataError>) -> Unit
    )
    suspend fun verifyOTPCode(
        code: String,
        onComplete: (Result<Unit, DataError>) -> Unit
    )
}